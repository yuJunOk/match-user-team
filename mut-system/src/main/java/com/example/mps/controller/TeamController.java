package com.example.mps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mps.common.ResponseCode;
import com.example.mps.common.ResponseEntity;
import com.example.mps.exception.BusinessException;
import com.example.mps.pojo.domain.TeamDo;
import com.example.mps.pojo.domain.UserTeamDo;
import com.example.mps.pojo.dto.*;
import com.example.mps.pojo.vo.UserTeamVo;
import com.example.mps.pojo.vo.UserVo;
import com.example.mps.service.TeamService;
import com.example.mps.service.UserService;
import com.example.mps.service.UserTeamService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author pengYuJun
 */
@Slf4j
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public ResponseEntity<Long> addTeam(@RequestBody TeamAddDto teamAddDto, HttpServletRequest request){
        if (teamAddDto == null){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        UserVo loginUser = userService.getCurrentUser(request);
        TeamDo team = new TeamDo();
        BeanUtils.copyProperties(teamAddDto, team);
        Long teamId = teamService.addTeam(team, loginUser);
        return ResponseEntity.success(teamId);
    }

    @PostMapping("/delete")
    public ResponseEntity<Boolean> deleteTeam(@RequestBody TeamDeleteDto teamDeleteDto, HttpServletRequest request){
        if (teamDeleteDto == null || teamDeleteDto.getId() <= 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        boolean result = teamService.deleteTeam(teamDeleteDto.getId(), request);
        if (!result) {
            throw new BusinessException(ResponseCode.ERROR, "删除失败");
        }
        return ResponseEntity.success(true);
    }

    @PostMapping("/update")
    public ResponseEntity<Boolean> updateTeam(@RequestBody TeamUpdateDto teamUpdateDto, HttpServletRequest request){
        if (teamUpdateDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateTeam(teamUpdateDto,request);
        if (!result) {
            throw new BusinessException(ResponseCode.ERROR, "更新失败");
        }
        return ResponseEntity.success(true);
    }

    @GetMapping("/get")
    public ResponseEntity<TeamDo> getTeamById(long id){
        if (id <= 0){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        TeamDo teamDo = teamService.getById(id);
        return ResponseEntity.success(teamDo);
    }


    @GetMapping("/list")
    public ResponseEntity<List<UserTeamVo>> listTeams(TeamDto teamDto, HttpServletRequest request) {
        if (teamDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        List<UserTeamVo> teamList = teamService.listTeams(teamDto, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(UserTeamVo::getId).collect(Collectors.toList());
        // 2、判断当前用户是否已加入队伍
        LambdaQueryWrapper<UserTeamDo> userTeamQueryWrapper = new LambdaQueryWrapper<>();
        try {
            UserVo loginUser = userService.getCurrentUser(request);
            userTeamQueryWrapper.eq(UserTeamDo::getUserId, loginUser.getId());
            userTeamQueryWrapper.in(!teamIdList.isEmpty(), UserTeamDo::getTeamId, teamIdList);
            List<UserTeamDo> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeamDo::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception ignored) {}
        // 3、查询已加入队伍的人数
        LambdaQueryWrapper<UserTeamDo> userTeamJoinQueryWrapper = new LambdaQueryWrapper<>();
        userTeamJoinQueryWrapper.in(!teamIdList.isEmpty(), UserTeamDo::getTeamId, teamIdList);
        List<UserTeamDo> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeamDo>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeamDo::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResponseEntity.success(teamList);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<TeamDo>> listPageTeams(TeamDto teamDto, PageDto pageDto) {
        if (teamDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        TeamDo teamDo = new TeamDo();
        BeanUtils.copyProperties(teamDto, teamDo);
        Page<TeamDo> page = new Page<>(pageDto.getCurrent(),pageDto.getPageSize());
        QueryWrapper<TeamDo> queryWrapper = new QueryWrapper<>(teamDo);
        Page<TeamDo> resultPage = teamService.page(page,queryWrapper);
        return ResponseEntity.success(resultPage);
    }

    @PostMapping("/join")
    public ResponseEntity<Boolean> joinTeam(@RequestBody TeamJoinDto teamJoinDto, HttpServletRequest request){
        if (teamJoinDto==null){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        boolean result = teamService.joinTeam(teamJoinDto, request);
        return ResponseEntity.success(result);
    }

    @PostMapping("/quit")
    public ResponseEntity<Boolean> quitTeam(@RequestBody TeamQuitDto teamQuitDto, HttpServletRequest request){
        if (teamQuitDto == null){
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        boolean result = teamService.quitTeam(teamQuitDto, request);
        return ResponseEntity.success(result);
    }

    /**
     * 获取我创建的队伍
     *
     * @param teamDto
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public ResponseEntity<List<UserTeamVo>> listMyCreateTeams(TeamDto teamDto, HttpServletRequest request) {
        if (teamDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        UserVo loginUser = userService.getCurrentUser(request);
        teamDto.setUserId(loginUser.getId());
        List<UserTeamVo> teamList = teamService.listTeams(teamDto, true);
        return ResponseEntity.success(teamList);
    }

    /**
     * 获取我加入的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public ResponseEntity<List<UserTeamVo>> listMyJoinTeams(TeamDto teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        UserVo loginUser = userService.getCurrentUser(request);
        LambdaQueryWrapper<UserTeamDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeamDo::getUserId, loginUser.getId());
        List<UserTeamDo> userTeamList = userTeamService.list(queryWrapper);
        // 取出不重复的队伍 id
        // teamId userId
        Map<Long, List<UserTeamDo>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeamDo::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<UserTeamVo> teamList = teamService.listTeams(teamQuery, true);
        return ResponseEntity.success(teamList);
    }
}

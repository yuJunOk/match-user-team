package com.example.mps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mps.common.ResponseCode;
import com.example.mps.exception.BusinessException;
import com.example.mps.pojo.domain.TeamDo;
import com.example.mps.pojo.domain.UserDo;
import com.example.mps.pojo.domain.UserTeamDo;
import com.example.mps.pojo.dto.TeamDto;
import com.example.mps.pojo.dto.TeamJoinDto;
import com.example.mps.pojo.dto.TeamQuitDto;
import com.example.mps.pojo.dto.TeamUpdateDto;
import com.example.mps.pojo.enums.TeamStatusEnum;
import com.example.mps.pojo.vo.UserTeamVo;
import com.example.mps.pojo.vo.UserVo;
import com.example.mps.service.TeamService;
import com.example.mps.mapper.TeamMapper;
import com.example.mps.service.UserService;
import com.example.mps.service.UserTeamService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author pengYuJun
* @description 针对表【tb_team(队伍)】的数据库操作Service实现
* @createDate 2025-04-17 18:34:14
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, TeamDo>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addTeam(TeamDo team, UserVo loginUser) {
        //1. 请求参数是否为空？
        if (team == null){
            throw  new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw  new BusinessException(ResponseCode.FORBIDDEN);
        }
        final long userId = loginUser.getId();
        //3. 校验信息
        //  a. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum >20){
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"队伍人数不符合要求");
        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (!StringUtils.hasText(name) || name.length() > 20) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.hasText(description) && description.length() > 512) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (!StringUtils.hasText(password) || password.length() > 32) {
                throw new BusinessException(ResponseCode.ERROR, "密码设置不正确");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ResponseCode.ERROR, "超时时间 > 当前时间");
        }
        //  g. 校验用户最多创建 5 个队伍
        LambdaQueryWrapper<TeamDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeamDo::getUserId, userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ResponseCode.ERROR, "用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ResponseCode.ERROR, "创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeamDo userTeam = new UserTeamDo();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ResponseCode.ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<UserTeamVo> listTeams(TeamDto teamDto, boolean isAdmin) {
        LambdaQueryWrapper<TeamDo> queryWrapper = new LambdaQueryWrapper<>();
        // 组合查询条件
        Long id = teamDto.getId();
        queryWrapper.eq(id != null && id > 0, TeamDo::getId, id);
        //
        List<Long> idList = teamDto.getIdList();
        queryWrapper.in(CollectionUtils.isNotEmpty(idList), TeamDo::getId, idList);
        //
        String searchText = teamDto.getSearchText();
        if (StringUtils.hasText(searchText)) {
            queryWrapper.and(qw -> qw.like(TeamDo::getName, searchText).or().like(TeamDo::getDescription, searchText));
        }
        //
        queryWrapper.like(StringUtils.hasText(teamDto.getName()), TeamDo::getName, teamDto.getName());
        //
        String description = teamDto.getDescription();
        queryWrapper.like(StringUtils.hasText(description), TeamDo::getDescription, description);
        // 查询最大人数相等的
        Integer maxNum = teamDto.getMaxNum();
        queryWrapper.eq(maxNum != null && maxNum > 0, TeamDo::getMaxNum, maxNum);
        // 根据创建人来查询
        Long userId = teamDto.getUserId();
        queryWrapper.eq(userId != null && userId > 0, TeamDo::getUserId, userId);
        // 根据状态来查询
        Integer status = teamDto.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            statusEnum = TeamStatusEnum.PUBLIC;
        }
        if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
            throw new BusinessException(ResponseCode.FORBIDDEN);
        }
        queryWrapper.eq(TeamDo::getStatus, statusEnum.getValue());
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt(TeamDo::getExpireTime, new Date()).or().isNull(TeamDo::getExpireTime));
        List<TeamDo> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //
        List<UserTeamVo> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (TeamDo team : teamList) {
            if (team.getUserId() == null) {
                continue;
            }
            UserDo user = userService.getById(team.getUserId());
            UserTeamVo userTeamVO = new UserTeamVo();
            BeanUtils.copyProperties(team, userTeamVO);
            // 脱敏用户信息
            if (user != null) {
                userTeamVO.setCreateUser(new UserVo(user));
            }
            teamUserVOList.add(userTeamVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateDto teamUpdateDto, HttpServletRequest request) {
        if (teamUpdateDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        Long id = teamUpdateDto.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        TeamDo oldTeam = this.getById(id);
        if (oldTeam==null){
            throw new BusinessException(ResponseCode.ERROR, "队伍不存在");
        }
        UserVo loginUser = userService.getCurrentUser(request);
        //只有管理员或者队伍的创建者才可以修改
        if (!oldTeam.getUserId().equals(loginUser.getId()) &&!userService.isAdmin(request)){
            throw new BusinessException(ResponseCode.FORBIDDEN);
        }
        //如果队伍状态改为加密，必须要有密码
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamUpdateDto.getStatus());
        if (teamStatusEnum.equals(TeamStatusEnum.PRIVATE) && !StringUtils.hasText(teamUpdateDto.getPassword())) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "加密队伍必须要设置密码");
        }
        TeamDo updateTeam = new TeamDo();
        BeanUtils.copyProperties(teamUpdateDto, updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinDto teamJoinDto, HttpServletRequest request) {
        if (teamJoinDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinDto.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        TeamDo team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "队伍不存在");
        }
        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinDto.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if (!StringUtils.hasText(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ResponseCode.PARAMS_ERROR, "密码错误");
            }
        }
        //该用户已加入的队伍数量 数据库查询所以放到下面，减少查询时间
        UserVo loginUser = userService.getCurrentUser(request);
        Long userId = loginUser.getId();
        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("mps:join_team:"+teamJoinDto.getTeamId());
        try {
            while (true){
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    LambdaQueryWrapper<UserTeamDo> userTeamQueryWrapper = new LambdaQueryWrapper<>();
                    userTeamQueryWrapper.eq(UserTeamDo::getUserId, userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ResponseCode.PARAMS_ERROR, "最多创建和加入5个队伍");
                    }
                    //不能重复加入已加入的队伍
                    userTeamQueryWrapper.clear();
                    userTeamQueryWrapper.eq(UserTeamDo::getUserId, userId);
                    userTeamQueryWrapper.eq(UserTeamDo::getTeamId, teamId);
                    long hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BusinessException(ResponseCode.PARAMS_ERROR, "用户已加入该队伍");
                    }
                    //已加入队伍的人数
                    userTeamQueryWrapper.clear();
                    userTeamQueryWrapper.eq(UserTeamDo::getTeamId, teamId);
                    long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ResponseCode.PARAMS_ERROR, "队伍已满");
                    }
                    //修改队伍信息
                    UserTeamDo userTeam = new UserTeamDo();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        }catch (Exception e){
            log.error("doCacheRecommendUser error", e);
            return false;
        }finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Override
    public boolean quitTeam(TeamQuitDto teamQuitDto, HttpServletRequest request) {
        if (teamQuitDto == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitDto.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        TeamDo team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ResponseCode.ERROR, "队伍不存在");
        }
        UserVo loginUser = userService.getCurrentUser(request);
        long userId = loginUser.getId();
        UserTeamDo queryUserTeam = new UserTeamDo();
        queryUserTeam.setTeamId(teamId);
        queryUserTeam.setUserId(userId);
        LambdaQueryWrapper<UserTeamDo> queryWrapper = new LambdaQueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        //队伍只剩下一个人，解散
        if (teamHasJoinNum == 1) {
            //删除队伍
            this.removeById(teamId);
        } else {
            //队伍至少还剩下两人
            //是队长
            if (team.getUserId() == userId) {
                //把队伍转移给最早加入的用户
                //1.查询已加入队伍的所有用户和加入时间
                LambdaQueryWrapper<UserTeamDo> userTeamQueryWrapper = new LambdaQueryWrapper<>();
                userTeamQueryWrapper.eq(UserTeamDo::getTeamId, teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeamDo> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ResponseCode.ERROR);
                }
                UserTeamDo nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                TeamDo updateTeam = new TeamDo();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ResponseCode.ERROR, "更新队伍队长失败");
                }
            }
        }
        //移除关系
        return userTeamService.remove(queryWrapper);
    }

    @Override
    public boolean deleteTeam(long id, HttpServletRequest request) {
        // 校验队伍是否存在
        TeamDo team = getTeamById(id);
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        UserVo loginUser = userService.getCurrentUser(request);
        if (!team.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ResponseCode.FORBIDDEN, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        LambdaQueryWrapper<UserTeamDo> userTeamQueryWrapper = new LambdaQueryWrapper<>();
        userTeamQueryWrapper.eq(UserTeamDo::getTeamId, teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result){
            throw new BusinessException(ResponseCode.ERROR,"删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        LambdaQueryWrapper<UserTeamDo> userTeamQueryWrapper = new LambdaQueryWrapper<>();
        userTeamQueryWrapper.eq(UserTeamDo::getTeamId, teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId
     * @return
     */
    private TeamDo getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        TeamDo team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ResponseCode.ERROR, "队伍不存在");
        }
        return team;
    }


}





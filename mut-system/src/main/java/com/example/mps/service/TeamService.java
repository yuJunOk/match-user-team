package com.example.mps.service;

import com.example.mps.pojo.domain.TeamDo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mps.pojo.dto.TeamDto;
import com.example.mps.pojo.dto.TeamJoinDto;
import com.example.mps.pojo.dto.TeamQuitDto;
import com.example.mps.pojo.dto.TeamUpdateDto;
import com.example.mps.pojo.vo.UserTeamVo;
import com.example.mps.pojo.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author pengYuJun
* @description 针对表【tb_team(队伍)】的数据库操作Service
* @createDate 2025-04-17 18:34:14
*/
public interface TeamService extends IService<TeamDo> {

    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    Long addTeam(TeamDo team, UserVo loginUser);

    /**
     * 查询列表
     *
     * @param teamDto
     * @param isAdmin
     * @return
     */
    List<UserTeamVo> listTeams(TeamDto teamDto, boolean isAdmin);

    /**
     * 更新组队信息
     * @param teamUpdateDto
     * @param request
     * @return
     */
    boolean updateTeam(TeamUpdateDto teamUpdateDto, HttpServletRequest request);

    /**
     * 加入队伍
     * @param teamJoinDto
     * @param request
     * @return
     */
    boolean joinTeam(TeamJoinDto teamJoinDto, HttpServletRequest request);

    /**
     * 退出队伍
     * @param teamQuitDto
     * @param request
     * @return
     */
    boolean quitTeam(TeamQuitDto teamQuitDto, HttpServletRequest request);

    /**
     * 删除队伍
     * @param id
     * @param request
     * @return
     */
    boolean deleteTeam(long id, HttpServletRequest request);
}

package com.example.mps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mps.mapper.UserTeamMapper;
import com.example.mps.pojo.domain.UserTeamDo;
import com.example.mps.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author pengYuJun
* @description 针对表【tb_user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-04-17 19:22:26
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeamDo>
    implements UserTeamService{

}





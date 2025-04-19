package com.example.mps.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.example.mps.pojo.domain.UserDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mps.pojo.vo.UserVo;
import org.apache.ibatis.annotations.Param;

/**
* @author pengYuJun
* @description 针对表【tb_user(用户表)】的数据库操作Mapper
* @createDate 2025-03-21 10:46:02
* @Entity com.example.mps.model.domain.User
*/
public interface UserMapper extends BaseMapper<UserDo> {

    /**
     * 分页查询
     * @param page
     * @param queryWrapper
     * @return
     */
    IPage<UserVo> selectUserVoPage(IPage<UserVo> page, @Param(Constants.WRAPPER) Wrapper<UserDo> queryWrapper);

}





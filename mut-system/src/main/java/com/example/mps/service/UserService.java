package com.example.mps.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.mps.pojo.domain.UserDo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mps.pojo.dto.PageDto;
import com.example.mps.pojo.dto.UserDto;
import com.example.mps.pojo.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author pengYuJun
* @description 针对表【tb_user(用户表)】的数据库操作Service
* @createDate 2025-03-21 10:46:02
*/
public interface UserService extends IService<UserDo> {

    /**
     * 用户注册
     *
     * @param loginName 用户账户
     * @param loginPwd 用户密码
     * @param checkPwd 校验密码
     * @return 新用户id
     */
    long userRegister(String loginName, String loginPwd, String checkPwd, String email, String captcha, HttpServletRequest request);

    /**
     * 用户登录
     *
     * @param loginName 用户账号
     * @param loginPwd 用户密码
     * @return 用户信息
     */
    UserVo userLogin(String loginName, String loginPwd, HttpServletRequest request);

    /**
     * 用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 分页查询
     * @param userDto
     * @param pageDto
     * @return
     */
    IPage<UserVo> searchUser(UserDto userDto, PageDto pageDto);

    /**
     *
     * @param email
     * @param request
     */
    String getMailCaptcha(String email, HttpServletRequest request);

    /**
     * 添加用户
     * @param userDo
     * @return
     */
    boolean addUser(UserDo userDo);

    /**
     * 根据标签搜索用户
     *
     * @param tagList
     * @return
     */
    List<UserVo> searchUsersByTags(List<String> tagList);

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    UserVo getCurrentUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 获取最匹配的用户
     * @param num
     * @param user
     * @return
     */
    List<UserVo> matchUsers(long num, UserVo user);
}

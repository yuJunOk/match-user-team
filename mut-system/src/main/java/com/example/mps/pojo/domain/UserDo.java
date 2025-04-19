package com.example.mps.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.Data;

/**
 * 用户表
 * @author pengYuJun
 * @TableName tb_user
 */
@TableName(value ="tb_user")
@Data
public class UserDo {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 头像链接
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 账号
     */
    private String loginName;

    /**
     * 密码
     */
    private String loginPwd;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 角色值
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
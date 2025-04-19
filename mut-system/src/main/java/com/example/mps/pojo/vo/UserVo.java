package com.example.mps.pojo.vo;

import com.example.mps.pojo.domain.UserDo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author pengYuJun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = -8460210827254062525L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 账号
     */
    private String loginName;

    /**
     * 头像链接
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 标签列表
     */
    private String tags;

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
     * 是否删除
     */
    private Integer deleted;

    /**
     * 根据User构建UserVo
     * @param userDo 表用户类
     */
    public UserVo(UserDo userDo) {
        this.id = userDo.getId();
        this.userName = userDo.getUserName();
        this.loginName = userDo.getLoginName();
        this.avatarUrl = userDo.getAvatarUrl();
        this.gender = userDo.getGender();
        this.tags = userDo.getTags();
        this.profile = userDo.getProfile();
        this.phone = userDo.getPhone();
        this.email = userDo.getEmail();
        this.status = userDo.getStatus();
        this.userRole = userDo.getUserRole();
        this.createTime = userDo.getCreateTime();
    }
}

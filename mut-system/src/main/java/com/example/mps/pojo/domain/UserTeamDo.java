package com.example.mps.pojo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户队伍关系
 * @author pengYuJun
 * @TableName tb_user_team
 */
@TableName("tb_user_team")
@Data
public class UserTeamDo {
    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 加入时间
     */
    private Date joinTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer deleted;
}
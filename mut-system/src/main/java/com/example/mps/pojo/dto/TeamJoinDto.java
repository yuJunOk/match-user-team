package com.example.mps.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户加入队伍请求体
 *
 * @author shaosao
 */
@Data
public class TeamJoinDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -24663018187059425L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
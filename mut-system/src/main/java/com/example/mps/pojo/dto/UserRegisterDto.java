package com.example.mps.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author pengYuJun
 */
@Data
public class UserRegisterDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 5436008848252433340L;

    private String loginName;

    private String loginPwd;

    private String checkPwd;

    private String email;

    private String captcha;
}

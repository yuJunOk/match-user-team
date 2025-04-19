package com.example.mps.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用删除请求
 *
 * @author yupi
 */
@Data
public class TeamDeleteDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1787902631969457554L;

    private long id;
}
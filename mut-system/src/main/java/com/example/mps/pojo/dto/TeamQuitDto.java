package com.example.mps.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author pengYuJun
 */
@Data
public class TeamQuitDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -2038884913144640407L;
    /**
     * id
     */
    private Long teamId;
}
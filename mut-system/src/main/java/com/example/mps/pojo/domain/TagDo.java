package com.example.mps.pojo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 标签表
 * @author pengYuJun
 * @TableName tb_tag
 */
@TableName(value ="tb_tag")
@Data
public class TagDo {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 是否父标签，1是，0否
     */
    private Integer parentFlag;

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
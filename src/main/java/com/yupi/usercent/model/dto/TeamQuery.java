package com.yupi.usercent.model.dto;


import com.yupi.usercent.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author lintai
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQuery  extends PageRequest {
    /**
     * id
     */
    private Long id;


    /**
     * 搜索关键词（同时对id和description搜索）
     * 给前端提供多一种搜索方式
     */
    private String searchText;
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开， 1 - 私有. 2 - 加密
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}

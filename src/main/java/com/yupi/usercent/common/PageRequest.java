package com.yupi.usercent.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lintai
 * @version 1.0
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页面大小
     */
    protected int pageSize=10;

    /**
     * 当前是第几页
     */
    protected int pageNum=1;
}

package com.yupi.usercent.once;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lintai
 * @version 1.0
 */
@Data
@EqualsAndHashCode
public class XingQiuUserInfo {
    /**
     *用户id
     */
    @ExcelProperty("成员编号")
    private String planetCode;

    /**
     *用户名
     */
    @ExcelProperty("成员昵称")
    private String username;
}


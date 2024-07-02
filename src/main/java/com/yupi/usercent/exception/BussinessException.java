package com.yupi.usercent.exception;

import com.yupi.usercent.common.ErrorCode;
import lombok.Data;

/**
 * 自定义异常类
 * @author lintai
 * @version 1.0
 */
@Data
public class BussinessException extends RuntimeException {

    private int code;
    private String description;

    public BussinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BussinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BussinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
}

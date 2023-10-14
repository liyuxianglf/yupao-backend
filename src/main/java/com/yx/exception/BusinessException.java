package com.yx.exception;

import com.yx.common.ErrorCode;

public class BusinessException extends RuntimeException{

    /**
     * 异常码
     */
    private final int code;

    /**
     * 描述
     */
    private final String description;
    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public int getCode() {
        return code;
    }



    public String getDescription() {
        return description;
    }

}

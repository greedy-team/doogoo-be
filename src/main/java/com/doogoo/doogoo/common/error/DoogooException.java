package com.doogoo.doogoo.common.error;

public class DoogooException extends RuntimeException {

    private final ErrorCode errorCode;

    public DoogooException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DoogooException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

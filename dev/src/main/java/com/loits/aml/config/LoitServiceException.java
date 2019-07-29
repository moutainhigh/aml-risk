package com.loits.aml.config;

public class LoitServiceException extends Exception {
    private String message;
    private String errCode;
    private String debugMessage;


    public LoitServiceException(String message, String errCode,
                                String debugMessage) {
        this.message = message;
        this.errCode = errCode;
        this.debugMessage = debugMessage;
    }

    public LoitServiceException(String message, String errCode) {
        this.message = message;
        this.errCode = errCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public void setDebugMessage(String debugMessage) {
        this.debugMessage = debugMessage;
    }
}

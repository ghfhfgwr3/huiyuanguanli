package com.csh.web.controller.wai.controller.utils;

public class BaseResponse1 {

    private Integer code;
    private String msg;
    public BaseResponse1(StatusCode statusCode) {
        this.code=statusCode.getCode();
        this.msg=statusCode.getMsg();
    }

    public BaseResponse1(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

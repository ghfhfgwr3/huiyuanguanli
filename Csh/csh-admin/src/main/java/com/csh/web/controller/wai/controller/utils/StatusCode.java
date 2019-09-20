package com.csh.web.controller.wai.controller.utils;

/**
 * 定义状态码类
 */
public enum StatusCode {

    Success(0,"消费成功"),
    Fail(-1,"消费失败"),
    Normal(0,"设备正常"),
    Service(1,"设备维修"),
    Stop(2,"设备停用"),
    Failure(-1,"验签失败"),
    Ff(-1,"无此设备信息"),
    Gg(0,"数据更新成功") ,
    Mn(-1,"数据更新失败"),
    Hh(0,"数据添加成功"),
    Jj(-1,"数据添加失败");


    StatusCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }



    private Integer code;
    private String msg;


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

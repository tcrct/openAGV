package com.makerwit.numes;

/**
 * 功能指令枚举
 *
 * @author Laotang
 */
public enum CmdKeyEnum {

    /*********GET**********/
    GETAC("getac", "查询RFID卡"),
    GETERR("geterr", "查询状态信息"),
    GETMAG("getmag", "查询磁导传感器导通状态"),
    GETSPD("getspd", "查询速度值"),
    GETMT("getmt", "查询物料状态"),


    /*********SET**********/
    SETSPD("setspd", "设置速度值"),
    SETROUT("setrout", "下发路径指令"),
    SETVMOT("setvmot", "设置小车动作"),
    SETOUT("setout", "车载动作/工站动作"),
    SETMOV("setmov", "设置小车动作"),
    SETAVOID("setavoid", "设置音乐"),
    SETMUSIC("setmusic", "设置音乐"),
    SETRTP("setrtp","预停车到位指令"),
    SETSTP("setstp", "立即停车"),
    SETTRA("settra", ""),






    /***********RPT**********/
    RPTAC("rptac", "上报RFID号"),
    RPTERR("rpterr", "上报异常信息"),
    RPTMT("rptmt", "上报物料状态"),
    RPTRTP("rptrtp", "上报到达站点(预停车)"),
    RPTVMOT("rptvmot", "上报动作到位信息"),
    RPTGO("rptgo", "小车请求继续执行当前任务"),
    RPTMAG("rptmag", "上报磁导状态"),
    RPTSTART("rptstart", "小车上电"),
    RPTMODE("rptmode","上报小车手动/自动模式"),

    /**opanAGV专用，与公司的请求指令不相关*/
//    VMR("VMR", "[vehicleMoveRequest]车辆移动请求"),
    VM_REQ("VM_REQ", "[vehicleMoveRequest]车辆移动请求")
    ;

    private String value;
    private String desc;
    private CmdKeyEnum(String value, String desc) {
        this.value = value;
        this.desc =desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}

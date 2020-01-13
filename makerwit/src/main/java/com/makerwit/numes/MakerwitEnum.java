package com.makerwit.numes;

/**
 * 枚举对象
 */
public enum MakerwitEnum {

    PARAMLINK("::", "参数连接符"),
    SEPARATOR(",,", "分隔符"),
    FRAME_HEAD("##", "帧头标识"),
    FRAME_END("ZZ", "帧尾标识"),
    UP_LINK("s", "上行标识符"),
    DOWN_LINK("r", "下行标识符"),
    FORWARD("f", "前进"),
    BACK("b", "后退"),
    STAIGHT_LINE("m", "直行"),
    LEFT("l", "左转"),
    RIGHT("r", "右转"),
    STOP("e", "停车"),
    PRE_STOP("s", "预停车"),
    PRE_STOP_UP("S", "预减速,专用于顶升AGV,因s会将后续路径截断.故添加一个"),
    CLOCKWISE_ROTATION("c", "顺时针旋转"),
    COUNTERCLOCKWISE_ROTATION("a", "逆时针旋转"),
    REVERSE_CLOCKWISE_ROTATION("d", "反向顺时针旋转"),
    REVERSE_COUNTERCLOCKWISE_ROTATION("g", "反向逆时针旋转"),
    LEFT_SIDESWAY("L", "左横移"),
    RIGHT_SIDESWAY("R", "右横移"),

    ;

    private String value;
    private String desc;
    private MakerwitEnum(String value, String desc) {
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

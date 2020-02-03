package com.robot.mvc.core.exceptions;


import com.robot.mvc.core.interfaces.IException;

/**
 * 异常枚举
 * code非0值时，均代表抛出异常
 *
 * @author Laotang
 * @date 2020/02/03
 * @since 1.0
 */
public enum ExceptionEnums implements IException {

    SUCCESS(0, "成功"),
    ERROR(1, "错误"),

    TOKEN_EXPIRE(2, "会话过期,请重新登录"),        // 手机TOKENID过期
    AUTH_FAIL(3, "帐号或密码错误,请重新登录"),
    NET_EXCEPTION(4, "网络异常"),
    INVALID_ACCESS_TOKENID(5, "AccessTokenId不存在"),
    RESULT_NOT_FIND(6, "抱歉!没有查到符合条件的数据"),
    INVALID_SERVER_ERROR(7, "服务器内部错误"),
    IMAGE_DETECT_FAILED(8, "图片格式可能不支持"),
    TRANSFORMEXCEPTION(9, "转换异常"),
    MATURITY(10, "使用期限到期"),
    REQUEST_TIMEOUT(11, "请求超时"),
    SIGNATURE_DOES_NOTMATCH(12, "签名错误"),
    PARAM_ERROR(13, "传递的参数有误!"),
    SERVER_BUSY(14, "服务器正忙，请稍后再试"),
    API_REQUEST_LIMIT_REACHED(15, "接口调用次数已达到设定的上限"),
    UNAUTHORIZED_CLIENT_IP(16, "请求来自未经授权的IP地址"),
    TOO_MANY_PARAM(17, "请求参数过多"),
    MONGODB_ERROR(18, "执行MongoDB时出错"),
    MVC_ERROR(19, "Mvc组件出错"),
    NETTY_STARTUP_ERROR(20, "启动netty服务出错"),
    MYSQL_ERROR(21, "执行MySQL时出错"),
    SECURITY_ERROR(22, "安全验证时出错"),
    SECURITY_ACCESS_DENIED(23, "没有权限访问该操作"),
    PARAM_NULL(24, "参数对象为NULL"),
    SIGN_ERROR(25, "签名异常"),
    RESPONSE_RAW_NULL(26, "响应对象内容不能为空"),

    ACCESS_DENIED(403, "拒绝访问"),
    DATABASE_ERROR(550, "数据库错误"),;


    private final int code;
    private final String message;

    /**
     * Constructor.
     */
    private ExceptionEnums(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the value.
     *
     * @return the value
     */

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

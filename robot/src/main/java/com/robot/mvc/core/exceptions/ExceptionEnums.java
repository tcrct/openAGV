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
    DATABASE_ERROR(550, "数据库错误"),


    AUTH_EXPIRE_TIME_ERROR(1020, "贵司授权已过期，请联系系统管理员处理"),
    NOT_DUANGID_ERROR(1021, "id格式错误"),
    RELATION_FIELD_NAME_IS_EMPTY(1022, "获取用户关联关系时，属性名为空"),
    RELATION_KEY_IS_EMPTY(1023, "获取用户关联关系时，KEY为空"),
    RELATION_VALUE_IS_EMPTY(1024, "获取用户关联关系时，VALUE为空"),
    MODIFY_ERROR(1025,"不允许修改"),
    DEPT_NAME_ALREADY_EXIST(1026, "部门名称已经存在"),
    FIND_BY_ID_ERROR(1027, "通过id查询信息失败"),
    SAVE_ERROR(1028, "保存信息失败"),
    FIND_BY_KEY_ERROR(1029, "根据关键字查询信息失败"),
    DELETE_BY_ID_ERROR(1030, "根据ID删除失败"),
    DELETE_BY_KEY_ERROR(1031, "根据条件删除失败"),
    PARAM_EMPTY_ERROR(1032, "参数不能为空"),
    SEARCH_ERROR(1033, "根据条件搜索失败"),
    VERIFY_ERROR(1034, "信息校验失败，请检查数据是否正确"),
    DTO_CREATE_ERROR(1035, "创建DTO失败"),
    SEAL_USE_TOTAL_ERRROR(1036, "公司用印次数不能小于0"),
    SEAL_USED_COUNT_ERRROR(1037, "公司已经用印次数不能小于0"),
    DEPT_PARENT_SELF_ERROR(1038, "部门的上一级部门不能是本身"),
    DEPT_PARENT_CHILD_ERROR(1039, "部门的上一级部门不能是子部门"),
    EXIST_ERROR(1040, "已经存在，无需再次修改"),
    RELOAD_ERROR(1050, "重载失败"),
    AUTHORITY_DISTRIBUTE(1051, "权限分配失败"),
    GET_TOKEN_ERROR(1052, "获取token失败"),
    ADD_TOKEN_ERROR(1053, "添加token失败"),
    REMOVE_TOKEN_ERROR(1054, "删除token失败"),
    PWD_REPWD_NOT_SAME_ERROR(1055, "密码和确认密码不一致"),
    VERIFY_CODE_ERROR(1056, "手机验证码错误"),
    PWD_OLDPWD_NOT_SAME_ERROR(1057, "旧密码错误"),
    CHANGE_PWD_ERROR(1057, "修改密码失败"),
    SEAL_NOT_BIND_FLOW_ERROR(1058, "印章没有绑定"),
    COMPANY_CODE_NOT_UNIQUE_ERROR(1059, "公司代号已经存在"),
    USER_NOT_EXIST_OR_INPUT_ERROR(1060, "用户不存在或用户账号、手机号错误"),
    COMPANY_FIND_USER_KEY_ERROR(1061, "根据用户关键字查询公司信息失败，请再次尝试或手动输入"),
    PHONE_ERROR(1062, "手机号码错误，请确认是否输入正确"),
    DEPTID_EMPTY_ERROR(1063, "部门id不能为空"),
    DEPT_EMPTY_ERROR(1064, "部门为空"),
    DEPT_NOT_FOUND_ERROR(1065, "根据id查询不到部门"),
    DEPT_NOT_USER_ERROR(1066, "部门没有用户"),
    ADMIN_ROLE_ADD_ERROR(1067, "不允许添加admin角色"),
    ADMIN_ROLE_NAME_CHANGE_ERROR(1068, "不允许将角色名设置为admin"),
    ROOT_ROLE_ADD_ERROR(1067, "不允许添加root角色"),
    ROOT_ROLE_NAME_CHANGE_ERROR(1068, "不允许将角色名设置为root"),
    ADMIN_ROLE_NOT_FOUND(1069, "未找到管理员角色"),
    ADMIN_USER_NOT_FOUND(1070, "管理员角色没有用户"),
    REFUSE_SMS_SEND_ERROR(1071, "拒绝申请的通知信息发送失败"),
    CANCEL_SMS_SEND_ERROR(1072, "取消申请的通知信息发送失败"),
    SMS_SEND_ERROR(1073, "通知信息发送失败"),
    PHONE_OR_EMAIL_EMPTY_ERROR(1074, "手机号码或邮箱不能为空"),
    DEPT_FIND_BY_LEADER_USER_ID_ERROR(1075, "根据负责人用户id查询部门失败"),
    ACCOUNT_OR_PHONE_NOT_UNIQUE_ERROR(1076, "用户名或手机号不唯一"),
    FILE_TOO_LARGER_ERROR(1077, "上传的文件太大"),
    FILE_TYPE_NOT_SUPPORT_ERROR(1078, "不支持上传此文件类型"),
    REPLY_DELETE_ACCESS_DENIED(1079, "没有权限删除此信息"),
    POST_IS_USING_ERROR(1080, "岗位正在使用中，无法删除"),
    DEPT_IS_USING_ERROR(1081, "部门正在使用中，无法删除"),
    ROLE_IS_USING_ERROR(1082, "角色正在使用中，无法删除"),
    ADMIN_CAN_NOT_DELETE(1083, "admin不允许删除"),
    SAVE_SAME_ERROR(1084, "请勿保存重复数据"),
    STATUS_ERROR(1085, "状态错误"),

    ;


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

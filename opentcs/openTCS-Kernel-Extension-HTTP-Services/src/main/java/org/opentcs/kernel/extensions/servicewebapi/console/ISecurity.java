package org.opentcs.kernel.extensions.servicewebapi.console;

/**
 * 安全验证接口
 *
 * @author Laotang
 * @date 2020-03-20
 */
public interface ISecurity {

    /**
     * 是否允许访问
     *
     * @return 返回true则代表允许访问
     */
    boolean isAllowAccess(String uri);

    /**
     * 取安全用户，即登录成功的用户
     *
     * @return ISecurityUser
     */
    ISecurityUser getSecurityUser();

}

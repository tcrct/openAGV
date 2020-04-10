package org.opentcs.kernel.extensions.servicewebapi.console.interfaces;

import java.io.Serializable;
import java.util.Map;

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
    boolean isAllowAccess(String uri, Map<String, String> headerMap);

    /**
     * 取安全用户，即登录成功的用户
     *
     * @param userId 用户ID
     * @return ISecurityUser
     */
    ISecurityUser getSecurityUser(String userId);

    /**
     * 登录
     *
     * @param serializable 登录Dto
     * @return
     */
    ISecurityUser login(Serializable serializable);

    /**
     * 退出登录
     *
     * @param userId 用户ID
     * @return
     */
    boolean logout(String userId);

}

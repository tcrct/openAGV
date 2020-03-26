/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import com.google.common.base.Strings;
import org.opentcs.kernel.extensions.servicewebapi.console.interfaces.ISecurity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import javax.inject.Inject;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Authenticates incoming requests.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Authenticator {

  /**
   * This class'security logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Authenticator.class);
  /**
   * Defines the required access rules.
   */
  private final ServiceWebApiConfiguration configuration;
  /**
   * access key
   */
  private static String authAccessKey;

    /**
     * 访问验证
     */
    private static ISecurity security;

  // TODO 访问验证码设置，需要在app.setting里设置该key值
  public static final String ID = "robot.webapi.accessKey";
    // TODO 访问验证类，请在系统启动时设置 System.setProperty(SECURITY_CLASS_NAME, 类名);
    public static final String SECURITY_CLASS_NAME = "robot.security.className";
  /**
   * Creates a new instance.
   *
   * @param configuration Defines the required access rules.
   */
  @Inject
  public Authenticator(ServiceWebApiConfiguration configuration) {
    this.configuration = requireNonNull(configuration, "configuration");
    init();
  }

  private void init() {
      if (null == configuration) {
          return;
      }
      try {
          if (null != security) {
              return;
          }
          Class<?> securityClass = Class.forName(System.getProperty(SECURITY_CLASS_NAME));
          if (null != securityClass) {
              security = (ISecurity) securityClass.newInstance();
          }
      } catch (Exception e) {
          LOG.error("创建安全验证类时出错: " + e.getMessage(), e);
      }
  }

  /**
   * Checks whether authentication is required and the given request is authenticated.
   *
   * @param request The request to be checked.
   * @return <code>true</code> if, and only if, authentication is required and the given request is
   * authenticated.
   */
  public boolean isAuthenticated(Request request) {
      requireNonNull(request, "request");

      if (Strings.isNullOrEmpty(authAccessKey)) {
          authAccessKey = System.getProperty(ID, configuration.accessKey());
      }

      String requestAccessKey = request.headers(HttpConstants.HEADER_NAME_ACCESS_KEY.toLowerCase());
      if (null == requestAccessKey) {
          requestAccessKey = request.headers(HttpConstants.HEADER_NAME_ACCESS_KEY);
      }
      LOG.debug("Provided access key in header is '{}', required value is '{}'",
              requestAccessKey,
              authAccessKey);

      // Any empty access key indicates authentication is not required.
      if (Strings.isNullOrEmpty(authAccessKey)) {
          LOG.debug("No access key, authentication not required.");
          return true;
      }

      boolean isAuth = Objects.equals(requestAccessKey, authAccessKey);
      // 开启URI权限验证
      if (isAuth && null != security) {
          isAuth = security.isAllowAccess(request.uri());
      }
      return isAuth;
  }

    public static ISecurity getSecurity() {
        return security;
    }
}

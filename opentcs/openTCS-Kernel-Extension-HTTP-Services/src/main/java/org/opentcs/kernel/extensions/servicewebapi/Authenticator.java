/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import com.google.common.base.Strings;
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
   * This class's logger.
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

  // TODO 访问验证码设置，需要在app.setting里设置该key值
  public static final String ID = "robot.webapi.accessKey";
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
    authAccessKey = System.getProperty(ID, configuration.accessKey());
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

    String requestAccessKey = request.headers(HttpConstants.HEADER_NAME_ACCESS_KEY);
    LOG.debug("Provided access key in header is '{}', required value is '{}'",
              requestAccessKey,
            authAccessKey);

    // Any empty access key indicates authentication is not required.
    if (Strings.isNullOrEmpty(authAccessKey)) {
      LOG.debug("No access key, authentication not required.");
      return true;
    }

    return Objects.equals(requestAccessKey, authAccessKey);
  }

}

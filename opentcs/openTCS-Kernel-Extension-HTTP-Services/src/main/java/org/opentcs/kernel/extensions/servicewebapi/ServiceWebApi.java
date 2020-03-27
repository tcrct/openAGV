/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SslParameterSet;
import org.opentcs.components.kernel.KernelExtension;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.kernel.extensions.servicewebapi.console.RobotRequestHandler;
import org.opentcs.kernel.extensions.servicewebapi.console.SparkMappingFactory;
import org.opentcs.kernel.extensions.servicewebapi.console.interfaces.IWebSocket;
import org.opentcs.kernel.extensions.servicewebapi.v1.V1RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Provides an HTTP interface for basic administration needs.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ServiceWebApi
        implements KernelExtension {

    /**
     * This class's logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceWebApi.class);
    /**
     * The interface configuration.
     */
    private final ServiceWebApiConfiguration configuration;
    /**
     * Authenticates incoming requests.
     */
    private final Authenticator authenticator;
    /**
     * Handles requests for API version 1.
     */
    private final V1RequestHandler v1RequestHandler;
    /**
     * Maps between objects and their JSON representations.
     */
    private final ObjectMapper objectMapper
            = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    /**
     * The connection encryption configuration.
     */
    private final SslParameterSet sslParamSet;
    /**
     * The actual HTTP service.
     */
    private Service service;
    /**
     * Whether this kernel extension is initialized.
     */
    private boolean initialized;
    /**
     * TODO 控制中心(web)端请求处理器
     */
    private final RobotRequestHandler robotRequestHandler;

    /**
     * Creates a new instance.
     *
     * @param configuration The interface configuration.
     * @param sslParamSet The SSL parameter set.
     * @param authenticator Authenticates incoming requests.
     * @param v1RequestHandler Handles requests for API version 1.
     */
    @Inject
    public ServiceWebApi(ServiceWebApiConfiguration configuration,
                         SslParameterSet sslParamSet,
                         Authenticator authenticator,
                         V1RequestHandler v1RequestHandler,
                         RobotRequestHandler robotRequestHandler) {
        this.configuration = requireNonNull(configuration, "configuration");
        this.authenticator = requireNonNull(authenticator, "authenticator");
        this.v1RequestHandler = requireNonNull(v1RequestHandler, "v1RequestHandler");
        this.sslParamSet = requireNonNull(sslParamSet, "sslParamSet");
        this.robotRequestHandler = requireNonNull(robotRequestHandler, "robotRequestHandler");
    }

    @Override
    public void initialize() {
        if (isInitialized()) {
            return;
        }


        v1RequestHandler.initialize();
        robotRequestHandler.initialize();

        service = Service.ignite()
                .ipAddress(configuration.bindAddress())
                .port(configuration.bindPort());

        if (!SparkMappingFactory.getWebSocketMap().isEmpty()) {
            for (Iterator<Map.Entry<String, Class<? extends IWebSocket>>> iterator = SparkMappingFactory.getWebSocketMap().entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Class<? extends IWebSocket>> entry = iterator.next();
                service.webSocket(entry.getKey(), entry.getValue());
            }
        }

        if (configuration.useSsl()) {
            service.secure(sslParamSet.getKeystoreFile().getAbsolutePath(),
                    sslParamSet.getKeystorePassword(),
                    null,
                    null);
        } else {
            LOG.warn("Encryption disabled, connections will not be secured!");
        }

        service.before((request, response) -> {
            /*
            // 开启了安全认证，并且不是options请求及websocket握手请求
            if (!authenticator.isAuthenticated(request) &&
                    !request.requestMethod().equalsIgnoreCase(HttpMethod.options.toString()) &&
                    !HttpHeaderValues.WEBSOCKET.toString().equalsIgnoreCase(request.headers(HttpHeader.UPGRADE.toString()))) {
                // Delay the response a bit to slow down brute force attacks.
                Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);

                Map<String, Map<String, String>> returnMap = new HashMap<String, Map<String, String>>() {{
                    this.put("head", new HashMap<String, String>() {{
                        this.put("code", "403");
                        this.put("message", "Not authenticated");
                        this.put("uri", request.uri());
                        this.put("clientIp", request.ip());
                        this.put("timestamp", String.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    }});
                }};
                response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
                service.halt(403, objectMapper.writeValueAsString(returnMap));
            }
            */

            // Add a CORS header to allow cross-origin requests from all hosts.
            // This also makes using the "try it out" buttons in the Swagger UI documentation possible.
            response.header("Access-Control-Allow-Origin", "*");
        });

        // Reflect that we allow cross-origin requests for any headers and methods.
        service.options(
                "/*",
                (request, response) -> {
                    response.header("access-control-allow-credentials", "true");
                    String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
                    }

                    return "OK";
                });

        // Register routes for API versions here.
        service.path("/v1", () -> v1RequestHandler.addRoutes(service));
        //  其它所有的请求都转到业务模板处理
        service.path("/*", () -> robotRequestHandler.addRoutes(service));
        service.exception(IllegalArgumentException.class, (exception, request, response) -> {
            response.status(400);
            response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
            response.body(toJson(exception.getMessage()));
        });
        service.exception(ObjectUnknownException.class, (exception, request, response) -> {
            response.status(404);
            response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
            response.body(toJson(exception.getMessage()));
        });
        service.exception(ObjectExistsException.class, (exception, request, response) -> {
            response.status(409);
            response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
            response.body(toJson(exception.getMessage()));
        });
        service.exception(KernelRuntimeException.class, (exception, request, response) -> {
            response.status(500);
            response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
            response.body(toJson(exception.getMessage()));
        });
        service.exception(IllegalStateException.class, (exception, request, response) -> {
            response.status(500);
            response.type(HttpConstants.CONTENT_TYPE_APPLICATION_JSON_UTF8);
            response.body(toJson(exception.getMessage()));
        });

        initialized = true;
    }

    @Override
    public void terminate() {
        if (!isInitialized()) {
            return;
        }

        v1RequestHandler.terminate();
        robotRequestHandler.terminate();
        service.stop();

        initialized = false;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    private String toJson(String exceptionMessage)
            throws IllegalStateException {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(objectMapper.createArrayNode().add(exceptionMessage));
        } catch (JsonProcessingException exc) {
            throw new IllegalStateException("Could not produce JSON output", exc);
        }
    }
}

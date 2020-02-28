package org.opentcs.kernel.extensions.servicewebapi.console;

import spark.Request;
import spark.Response;

public interface IController {

    void init(Request request, Response response);

}

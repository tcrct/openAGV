package com.openagv.handlers;

import cn.hutool.core.util.ReflectUtil;
import com.openagv.core.AppContext;
import com.openagv.mvc.BaseController;
import com.openagv.core.interfaces.IRequest;
import com.openagv.core.interfaces.IResponse;
import com.openagv.route.Route;
import com.openagv.route.RouteHelper;
import com.openagv.tools.SettingUtils;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.log4j.Logger;

/**
 * 访问处理器
 */
public class AccountHandler {

    private final static Logger logger = Logger.getLogger(AccountHandler.class);

    private static final Object[] NULL_ARGS = new Object[0];		// 默认参数

    private static class AccountHandlerHolder {
        private static final AccountHandler INSTANCE = new AccountHandler();
    }
    private AccountHandler() {
    }
    public static final AccountHandler duang() {
        return AccountHandlerHolder.INSTANCE;
    }

    public void doHandler(String target, IRequest request, IResponse response) throws Exception{
        target = target.toUpperCase();
        Route route = RouteHelper.getRoutes().get(target);
        java.util.Objects.requireNonNull(route, "根据["+target+"]找不到对应路由映射");
        Object object = route.getInjectObject();
        java.util.Objects.requireNonNull(route, "根据["+target+"]找不到对应处理类对象");
        if(ToolsKit.SERVICE_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
            Object resultObj = ReflectUtil.invoke(object, target, request, response);
            logger.info("逻辑处理后返回报文："+ resultObj);
            response.write(resultObj);
        }
        else if(ToolsKit.CONTROLLER_FIELD.equalsIgnoreCase(AppContext.getInvokeClassType())) {
            BaseController controllerObj = (BaseController) route.getInjectObject();
            controllerObj.init(request, response);
            Object resultObj = ReflectUtil.invoke(controllerObj, target, request);
            logger.info("openAGV返回报文："+ resultObj);
            controllerObj.getRender(resultObj).setContext(request, response).render();
        }
    }

}

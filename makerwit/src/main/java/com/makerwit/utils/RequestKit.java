package com.makerwit.utils;

import cn.hutool.core.util.IdUtil;
import com.makerwit.core.protocol.Protocol;
import com.openagv.AgvContext;
import com.openagv.mvc.core.interfaces.IProtocol;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.core.interfaces.ISender;
import com.openagv.mvc.core.telegram.ActionRequest;
import com.openagv.mvc.core.telegram.BaseResponse;
import com.openagv.mvc.core.telegram.MoveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/***
 * 该类主要服务于在Service里，发起一个新的请求到另一设备时使用，用在控制业务逻辑。
 * 场景：
 * 可能在处理某一业务逻辑时，需要查询一下车辆或设备的当前状态时，可以使用。类似于http请求
 *
 * @author Laotang
 */
public class RequestKit {

    private static final Logger LOG = LoggerFactory.getLogger(RequestKit.class);

    /**发送接口**/
    private static ISender sender;
    private IRequest request;
    private Protocol protocol;

    private RequestKit() {
        sender = null;
    }

    public static RequestKit duang() {
        return new RequestKit();
    }

    public RequestKit request(ActionRequest request) {
        protocol(request.getProtocol());
        return this;
    }

    public RequestKit protocol(IProtocol protocol) {
        this.protocol = (Protocol)protocol;
        return this;
    }

    public IResponse execute() throws Exception {
        // 发送请求
        sendRequest();
        //等待响应回复
        return getResult();
    }

    /**
     * 发送请求
     */
    private void sendRequest() {
        if (null != request) {
            if (request instanceof MoveRequest) {
//            DispatchFactory.dispatch(request);
            }
        }
        else if (null != protocol) {
//            sender.send(protocol);
        }
    }

    /**
     * 取响应结果
     * @return
     */
    private IResponse getResult() {
        // 构建握手响应的验证码
        String code = ProtocolUtil.builderHandshakeCode(protocol);
        // 放置到Map集合中，阻塞线程，最多等待3秒响应
        AgvContext.getResponseProtocolMap().put(code, new LinkedBlockingQueue<IProtocol>(1));
        IProtocol protocol = getResponseProtocol(code, 3000);
        if (null == protocol) {
            return null;
        }
        IResponse response = new BaseResponse(IdUtil.objectId());
        response.write(protocol);
        return response;
    }

    /**
     * 取返回对象
     * @param key               请求ID
     * @param timeout       等待结果超时时间数
     * @return
     */
    private IProtocol getResponseProtocol(String key, long timeout) {
        try {
            if (AgvContext.getResponseProtocolMap().containsKey(key)) {
                return AgvContext.getResponseProtocolMap().get(key).poll(timeout, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            LOG.warn("等待请求响应时出错: " + e.getMessage(), e);
        }
        return null;
    }

}

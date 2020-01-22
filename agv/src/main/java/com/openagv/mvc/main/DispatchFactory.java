package com.openagv.mvc.main;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpStatus;
import com.openagv.AgvContext;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.interfaces.*;
import com.openagv.mvc.core.telegram.ActionRequest;
import com.openagv.mvc.core.telegram.BusinessRequest;
import com.openagv.mvc.core.telegram.MoveRequest;
import com.openagv.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 调度分发工厂
 * 根据协议指令中的车辆ID及指令动作，将协议分发到对应的service里的method。
 * 所以在Service里必须要实现对应指令动作的方法。
 *
 * @author Laotang
 */
public class DispatchFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DispatchFactory.class);

    /**协议解码器*/
    private static IProtocolDecode protocolDecode;
    /**重复发送对象*/
    private static IRepeatSend repeatSend;
    /**操作超时时长*/
    private static int TIME_OUT = 3000;
    /**发送接口**/
    private static ISender sender;

    static {
        initComponents();
    }

    /**
     * 系统接收到车辆或工作站发起的业务协议字符串
     * @param message 协议内容
     */
    public static void onIncomingTelegram(String message) {
        try {
            IProtocol protocol = protocolDecode.decode(message);
            // 如果返回的code在Map集合里存在，则视为由RequestKit发送请求的响应，将响应协议对象设置到对应的Map集合里，并退出
            if (AgvContext.getResponseProtocolMap().containsKey(protocol.getCode())) {
                LinkedBlockingQueue<IProtocol> protocolQueue = AgvContext.getResponseProtocolMap().get(protocol.getCode());
                protocolQueue.add(protocol);
                AgvContext.getResponseProtocolMap().put(protocol.getCode(), protocolQueue);
                return;
            }
            dispatchHandler(new BusinessRequest(message, protocol));
        } catch (Exception e) {
            LOG.error("分发处理接收到的业务协议字符串时出错: {}, {}", e.getMessage(), e);
            return;
        }
    }

    /**
     * 分发处理接收到的工站动作任务请求
     * 调度系统发起的请求
     *
     * @param request ActionRequest
     */
    public static void dispatch(ActionRequest request) {
        dispatchHandler(request);
    }

    /**
     * 分发处理接收到的移动请求
     * 调度系统发起的请求
     *
     * @param request MoveRequest
     */
    public static void dispatch(MoveRequest request) {
        dispatchHandler(request);
    }

    private static void dispatchHandler(IRequest request) {
        FutureTask<IResponse> futureTask = (FutureTask<IResponse>)ThreadUtil.execAsync(new RequestTask(request));
        try {
            IResponse response = futureTask.get(TIME_OUT, TimeUnit.MILLISECONDS);
            if (response.getStatus() == HttpStatus.HTTP_OK) {
                // 将Response对象放入重发队列，确保消息发送到车辆
                if (response.isResponseTo(request)) {
                    repeatSend.add(response);
//                    RepeatSendHandler.duang().add(response);
                }
                sender.send(response);
            }
        } catch (Exception e) {
            throw new AgvException(e.getMessage(), e);
        }
    }

    /**
     * 初始化协议解析对象
     */
    private static void initComponents() {

        IComponents agvComponents = AgvContext.getOpenAgvComponents();
        if (ToolsKit.isEmpty(agvComponents)) {
            throw new AgvException("OpenAGV组件对象不能为空,请先实现IComponents接口，并在Duang.java里设置setComponents方法");
        }

        protocolDecode = agvComponents.getProtocolDecode();
        if (ToolsKit.isEmpty(protocolDecode)) {
            throw new AgvException("协议解码器不能为空，请先实现IComponents接口里的getProtocolDecode方法");
        }

        repeatSend = agvComponents.getRepeatSend();
        if (ToolsKit.isEmpty(repeatSend)) {
            throw new AgvException("重复发送不能为空，请先实现IComponents接口里的getRepeatSend方法");
        }

    }
}

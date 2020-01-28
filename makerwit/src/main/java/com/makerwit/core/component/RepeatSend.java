package com.makerwit.core.component;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.*;
import com.robot.mvc.utils.SettingUtils;
import com.robot.mvc.utils.ToolsKit;


import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 重复发送响应处理器
 * 队列集合以deviceId作为key，以作区分，
 * 并且该队列里的元素会以指定的间隔时间执行一次发送
 *
 * @author Laotang
 * @since 1.0
 */
public class RepeatSend implements IRepeatSend {

    private static final Log LOG = LogFactory.get();

    /**是否需要重复发送, true为需要重复发送**/
    private static boolean isNeedRepeatSend;
    /**重复发送集合，key为车辆或设备ID标识字段*/
    private static final Map<String, Queue<IResponse>> REPEAT_SEND_MAP = new ConcurrentHashMap<>();

    private static class RepeatSendHandlerHolder {
        private static final RepeatSend INSTANCE = new RepeatSend();
    }
    private RepeatSend() {
        isNeedRepeatSend();
    }

    public static final RepeatSend duang() {
        return RepeatSend.RepeatSendHandlerHolder.INSTANCE;
    }


    private void isNeedRepeatSend() {
        isNeedRepeatSend = SettingUtils.getBoolean("repeat.send", false);
    }

    /**
     * 将需要重复发送的响应添加到队列
     *
     * @param response 响应对象
     */
    @Override
    public void add(IResponse response) throws RobotException {
        if (!isNeedRepeatSend) {
            LOG.info("系统默认为不需要重复发送，如需要重复发送，请在配置文件中设置[repeat.send=true]");
            return;
        }

        if (ToolsKit.isEmpty(response)) {
            throw new RobotException("响应对象不能为空");
        }

        String deviceId = response.getDeviceId();
        if (ToolsKit.isEmpty(deviceId)) {
            throw new RobotException("车辆或设备唯一标识不能为空");
        }

        Queue<IResponse> responseQueue = REPEAT_SEND_MAP.get(deviceId);
        if (ToolsKit.isEmpty(responseQueue)) {
            responseQueue = new LinkedBlockingQueue<>();
        }
        responseQueue.add(response);
        REPEAT_SEND_MAP.put(deviceId,responseQueue);
        LOG.info("添加响应对象[{}]到重复发送队列[{}]成功", response.getRawContent(),deviceId);
    }

    /**
     * 根据请求对象，删除重复发送队列里的响应元素
     * @param request 请求对象
     */
    @Override
    public void remove(IRequest request) throws RobotException {
        if (!isNeedRepeatSend) {
            LOG.info("系统默认为不需要重复发送，如需要重复发送，请在配置文件中设置[repeat.send=true]");
            return;
        }
        if (ToolsKit.isEmpty(request)) {
            throw new RobotException("请求对象不能为空");
        }

        IProtocol protocol = request.getProtocol();
        if (ToolsKit.isEmpty(protocol)) {
            throw new RobotException("请求协议对象不能为空");
        }

        String deviceId = protocol.getDeviceId();
        if (ToolsKit.isEmpty(deviceId)) {
            throw new RobotException("车辆或设备唯一标识不能为空");
        }

        String code = protocol.getCode();
        if (ToolsKit.isEmpty(code)) {
            throw new RobotException("验证码不能为空");
        }

        Queue<IResponse> responseQueue = REPEAT_SEND_MAP.get(deviceId);
        if (ToolsKit.isEmpty(responseQueue)) {
            throw new RobotException("车辆或设备[{}]没有需要重复发送的对象，退出方法");
        }
        IResponse response = responseQueue.peek();
        if (ToolsKit.isEmpty(request)) {
            throw new RobotException("[{" + deviceId + "}]需要重复发送的第1位对象不能为空");
        }

        // 如果验证码不是一致的，则抛出异常
        if (!code.equals(response.getHandshakeCode())) {
            throw new RobotException("移除重发队列元素时，提交上来的验证码[{" + code + "}]与重发队列[{" + deviceId + "}]中的[{" + response.getHandshakeCode() + "}]不符！退出删除");
        }
        // 如果验证码不是一致的，则将对应队列里的第1位元素移除
        responseQueue.remove();
        LOG.info("移除重发队列元素时，提交上来的验证码[{}]与重发队列[{}]中的[{}]相符！删除成功", code, deviceId, response.getHandshakeCode());
    }

    /***
     * 定时器触发，发送全部缓存于重发队列里的报文
     */
    public void sendAll(ISender sender) {
        if (REPEAT_SEND_MAP.isEmpty() || ToolsKit.isEmpty(sender)) {
            return;
        }
        for (Iterator<Map.Entry<String,Queue<IResponse>>> iterator = REPEAT_SEND_MAP.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String,Queue<IResponse>> entry = iterator.next();
            Queue<IResponse> responseQueue = entry.getValue();
            if (ToolsKit.isNotEmpty(responseQueue)) {
                for (IResponse response : responseQueue) {
                    if (ToolsKit.isNotEmpty(response)) {
                        sender.send(response);
                    }
                }
            }
        }
    }
}

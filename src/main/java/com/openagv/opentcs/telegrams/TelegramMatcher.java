package com.openagv.opentcs.telegrams;

import com.openagv.core.AppContext;
import com.openagv.core.handshake.HandshakeTelegramDto;
import com.openagv.core.handshake.HandshakeTelegramQueue;
import com.openagv.core.interfaces.IResponse;
import com.openagv.core.interfaces.ITelegramSender;
import com.openagv.dto.PathStepDto;
import com.openagv.tools.ToolsKit;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;

/**
 * 请求响应匹配器
 *
 * @author Laotang
 */
public class TelegramMatcher {

    private static final Logger logger = Logger.getLogger(TelegramMatcher.class);

    /**请求队列*/
    private final Queue<IResponse> requests = new LinkedList<>();
    /**所有经过的点的队列*/
    private final Map<String, LinkedBlockingQueue<String>> nextPointMap = new ConcurrentHashMap<>();

    /** 电报发送接口*/
    private ITelegramSender telegramSender;
    /**握手对象*/
    private HandshakeTelegramQueue handshakeTelegramQueue;

    public TelegramMatcher(ITelegramSender telegramSender) {
        this.telegramSender = requireNonNull(telegramSender, "telegramSender");
        handshakeTelegramQueue = AppContext.getAgvConfigure().getHandshakeTelegramQueue();
    }

    public ITelegramSender getTelegramSender() {
        return telegramSender;
    }

    /**
     *  将请求电报加入队列中
     * @param responseTelegram   请求电报,经过业务逻辑处理返回的IResponse，其实就是逻辑处理完成后返回的请求报文信息
     */
    public void enqueueRequestTelegram(@Nonnull IResponse responseTelegram) {
        requireNonNull(responseTelegram, "requestTelegram");

        /*
        boolean emptyQueueBeforeEnqueue = requests.isEmpty();
        requests.add(requestTelegram);
        logger.info("添加到队列成功: "+ requestTelegram.toString());
        if (emptyQueueBeforeEnqueue) {
            checkForSendingNextRequest();
        }
        */
        // 将所有经过的点(不包括起始点)放入队列中
        nextPointMap.put(responseTelegram.getDeviceId(), new LinkedBlockingQueue<>(responseTelegram.getNextPointNames()));
        // 将所有执行步骤放入队列
        List<PathStepDto> stepDtoList = (List<PathStepDto>) responseTelegram.getParams().get(IResponse.PARAM_POINT_STEP);
        if(ToolsKit.isNotEmpty(stepDtoList)) {
            AppContext.getPathStepMap().put(responseTelegram.getDeviceId(), stepDtoList);
        }
        if(AppContext.isHandshakeListener()) {
            handshakeTelegramQueue.add(new HandshakeTelegramDto(responseTelegram));
            logger.info("添加到握手队列["+responseTelegram.getDeviceId()+"]成功: "+ responseTelegram.getHandshakeKey());
        }
        telegramSender.sendTelegram(responseTelegram);
    }

    /**检查是否发送下一个请求*/
    public void checkForSendingNextRequest() {
        logger.info("检查是否发送下一个请求.");
        if (peekCurrentRequest().isPresent()) {
            IResponse response = peekCurrentRequest().get();
            if(AppContext.isHandshakeListener()) {
                handshakeTelegramQueue.add(new HandshakeTelegramDto(response));
                logger.info("添加到握手队列["+response.getDeviceId()+"]成功: "+ response.getHandshakeKey());
            }
            telegramSender.sendTelegram(response);
        }
        else {
            logger.info("没有请求消息发送");
        }
    }

    /**
     * add        增加一个元索                     如果队列已满，则抛出一个IIIegaISlabEepeplian异常
     * remove   移除并返回队列头部的元素    如果队列为空，则抛出一个NoSuchElementException异常
     * element  返回队列头部的元素             如果队列为空，则抛出一个NoSuchElementException异常
     * offer       添加一个元素并返回true       如果队列已满，则返回false
     * poll         移除并返问队列头部的元素    如果队列为空，则返回null
     * peek       返回队列头部的元素             如果队列为空，则返回null
     * put         添加一个元素                      如果队列满，则阻塞
     * take        移除并返回队列头部的元素     如果队列为空，则阻塞
     * @return
     */
    public Optional<IResponse> peekCurrentRequest() {
        return Optional.ofNullable(requests.peek());
    }

    /**
     * 如果与队列中的第一个请求匹配则返回true,并将该请求在队列中移除
     *
     * @param responseTelegram 要匹配的响应电报
     * @return  如果响应与队列中的第一个请求匹配，则返回true
     */
    public boolean tryMatchWithCurrentRequestTelegram(@Nonnull IResponse responseTelegram) {

        java.util.Objects.requireNonNull(responseTelegram, "responseTelegram");

        String cmdKey = responseTelegram.getCmdKey();
        boolean isVehicleArrivalCmd = AppContext.getVehicleArrivalCmdKey().equals(cmdKey);
        if(isVehicleArrivalCmd) {
            List<String> currentPositionList = responseTelegram.getNextPointNames();
            String postNextPoint = ToolsKit.isNotEmpty(currentPositionList ) ? currentPositionList.get(0) : "";
            String deviceId = responseTelegram.getDeviceId();
            return checkForVehiclePosition(deviceId, postNextPoint);
        }
//        //取出队列中的第一位的请求，该请求视为当前请求,放在队列里的是逻辑处理后返回的IResponse
//        IResponse currentRequestTelegram = requests.peek();
//        // 判断该回复里的请求到达点与队列里的是否一致，如果一致，则返回true
//        if (ToolsKit.isNotEmpty(currentRequestTelegram) &&
//                AppContext.getStateRequestCmdKey().equals(cmdKey) &&
//                responseTelegram.getHandshakeKey().equals(currentRequestTelegram.getHandshakeKey())) {
////                responseTelegram.getNextPointNames().equals(currentRequestTelegram.getNextPointNames())) {
//            // 在队列中移除第一位的
//            requests.remove();
//            logger.info("匹配成功，在队列中移除车辆行驶路径记录");
//            return true;
//        }

        return false;
    }

    /**
     * 检查下一个点是否存在列表中
     * @param deviceId  设备ID
     * @param postNextPoint 提交上来的下一个点名称
     * @return  如果存在则返回true
     */
    private boolean checkForVehiclePosition(String deviceId, String postNextPoint) {
        if (ToolsKit.isNotEmpty(deviceId) && ToolsKit.isNotEmpty(postNextPoint)) {
            LinkedBlockingQueue<String> nextPointQueue = nextPointMap.get(deviceId);
            String pointName = nextPointQueue.peek();
            if(ToolsKit.isNotEmpty(postNextPoint) && postNextPoint.equals(pointName)) {
                // 移除点
                nextPointQueue.remove();
                // 将路径步骤对应的点对象标识为已经执行，如需要重发未执行的路径时，
                // 可以遍历对应的List取到每个PathStepDto对象，根据isExceute属性确定是否已经执行。值为true时为已经执行。
                List<PathStepDto> stepDtoList = AppContext.getPathStepMap().get(deviceId);
                if(ToolsKit.isNotEmpty(stepDtoList)) {
                    for(PathStepDto stepDto : stepDtoList) {
                        if(stepDto.getPointName().equals(pointName)){
                            // 标识为已经执行
                            stepDto.setExecuteToTrue();
                            break;
                        }
                    }
                }
                return true;
            } else {
                logger.warn("车辆上报的点["+postNextPoint+"]，在系统列表不存在或已经上报处理或该点是起始点");
                return false;
            }
        } else {
            logger.warn("提交上来的车辆移动点不能为空，请确保在response.setNextPointNames()设置了接收到的到达点名称");
        }
        return false;
    }
}
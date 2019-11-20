/**
 * Copyright (c) The openTCS Authors.
 * <p>
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package com.openagv.opentcs.commands;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
//import com.makerwit.command.common.BaseRequest;
//import com.makerwit.command.common.BaseResponse;
//import com.makerwit.command.common.Protocol;
//import com.makerwit.command.common.VehicleMoveResponse;
//import com.makerwit.enums.DirectionEnum;
//import com.makerwit.utils.MakerwitUtil;
import com.openagv.core.AppContext;
import com.openagv.dto.PathStepDto;
import com.openagv.exceptions.AgvException;
import com.openagv.opentcs.adapter.CommAdapter;
import com.openagv.tools.ToolsKit;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.drivers.vehicle.AdapterCommand;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 重发车辆调度命令，即车辆故障后，在当前位置上继续执行余下的路径指令
 *
 * @author Laotang
 */
public class ContinueVehicleMoveCommand implements AdapterCommand {

    private static final Logger logger = LoggerFactory.getLogger(ContinueVehicleMoveCommand.class);
    //取消正在执行任务的车辆
    private static final String VEHICLE_WITHDRAWAL_URL = "/vehicles/${deviceId}/withdrawal?immediate=true&disableVehicle=false";
    // 创建新的订单
    private static final String NEW_TRANSPORT_ORDER_URL = "/transportOrders/TOrder-${orderId}";

    @Override
    public void execute(VehicleCommAdapter adapter) {
        if (!(adapter instanceof CommAdapter)) {
            return;
        }
        // 车辆ID
        String deviceId = adapter.getProcessModel().getVehicleReference().getName();
        String host = ToolsKit.getWebEndPoint();
        String withdrawalUrl = host + VEHICLE_WITHDRAWAL_URL.replace("${deviceId}", deviceId);
        String transportUrl = host + NEW_TRANSPORT_ORDER_URL.replace("${orderId}", UUID.randomUUID().toString());
        ThreadUtil.execAsync(new Runnable() {
            @Override
            public void run() {
                doExecute(adapter, deviceId, withdrawalUrl, transportUrl);
            }
        });
    }

    private void doExecute(VehicleCommAdapter adapter, String deviceId, String withdrawalUrl, String transportUrl) {
        logger.info("取消正在执行任务的车辆URL： " + withdrawalUrl);
        String startLocationName = getStartLocationName(adapter);
        String endLocationName = getEndLocationName(adapter);
        String finalOperation = getEndOperation(adapter);
        HttpResponse httpResponse = HttpRequest.post(withdrawalUrl).header(HttpHeaderNames.ACCEPT.toString(), "*/*").timeout(3000).execute();
        if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
            logger.error("重发车辆调度命令时失败，取消正在执行任务的车辆时出错: " + httpResponse.body());
            return;
        }
//    List<PathStepDto> stepDtoList = AppContext.getPathStepMap().get(deviceId);
        logger.info("创建新的订单： " + transportUrl);
        // 取消成功后，重新车辆指令(创建新的订单)
        Transport transport = new Transport();
//        ZonedDateTime dateTime = Instant.now().atZone(ZoneId.systemDefault());
        // 一小时后过期
        Instant now = Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8));
        now = now.plusSeconds(TimeUnit.SECONDS.toSeconds(3600));
        transport.setDeadline(now);
        transport.setIntendedVehicle(deviceId);
        // 起始工站位置
        Destination destinationStart = new Destination();
        destinationStart.setLocationName(startLocationName);
        // TODO 是否需要确定工站操作？？
        destinationStart.setOperation("NOP");
        destinationStart.setProperties(new ArrayList<>());
        // 终止工站位置
        Destination destinationEnd = new Destination();
        destinationEnd.setLocationName(endLocationName);
        destinationEnd.setOperation(finalOperation);
        destinationEnd.setProperties(new ArrayList<>());

        transport.getDestinations().add(destinationStart);
        transport.getDestinations().add(destinationEnd);
        Map map = BeanUtil.beanToMap(transport);
        map.put("deadline", transport.getDeadline().toString());
        String body = ToolsKit.toJsonString(map);
        logger.info("创建新指令URL: " + transportUrl + "     body: " + body);
        httpResponse = HttpUtil.createPost(transportUrl).body(body).header(HttpHeaderNames.ACCEPT.toString(), "*/*").timeout(3000).execute();
        if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
            logger.error("重发车辆调度命令时失败，创建新的指令时出错: " + httpResponse.body());
        } else {
            logger.info("创建新的订单成功");
        }
        return;
    }

    private String getStartLocationName(VehicleCommAdapter adapter) {
        MovementCommand cmd = adapter.getSentQueue().peek();
        return getLocationName(cmd);
    }

    private String getEndLocationName(VehicleCommAdapter adapter) {
        MovementCommand cmd = adapter.getSentQueue().peek();
        return cmd.getFinalDestinationLocation().getName();
    }

    private String getEndOperation(VehicleCommAdapter adapter) {
        MovementCommand cmd = adapter.getSentQueue().peek();
        return cmd.getFinalOperation();
    }

    private String getLocationName(MovementCommand cmd) {
        Point currentPoint = cmd.getStep().getSourcePoint();
        Set<Location.Link> linkSet = currentPoint.getAttachedLinks();
        if (ToolsKit.isEmpty(linkSet)) {
            throw new AgvException("起始点没有设置工作站点位置");
        }

        for (Iterator<Location.Link> iterator = linkSet.iterator(); iterator.hasNext(); ) {
            Location.Link link = iterator.next();
            return link.getLocation().getName();
        }
        return "";
    }


  /*
  @Deprecated
  private void doExecute(String deviceId) {
    // 当前车辆余下的路径指令
    List<PathStepDto> stepDtoList = AppContext.getPathStepMap().get(deviceId);
    StringBuilder moveCommand = new StringBuilder();
    if(ToolsKit.isNotEmpty(stepDtoList)) {
      for(PathStepDto stepDto : stepDtoList) {
        // 未执行的路径指令
        if(!stepDto.isExecute()){
          moveCommand.append(stepDto.getPointAction()).append(DirectionEnum.PARAMLINK.getValue());
        }
      }
    }
    if (moveCommand.length()>1) {
      moveCommand.deleteCharAt(moveCommand.length() - DirectionEnum.PARAMLINK.getValue().length());
    }
//    Protocol protocol = MakerwitUtil.buildProtocolFromParamString(moveCommand.toString());
    Response response = null;
    try {
      VehicleMoveResponse vehicleMoveResponse = new VehicleMoveResponse(deviceId, moveCommand.toString());
      Protocol protocol = (Protocol)(vehicleMoveResponse.toRequest()).getProtocol();
      // 更改为下行标识，发送到车辆/设备
      protocol.setDirection(DirectionEnum.DOWN_LINK);
      response = Response.build(new DuangId().toString());
      String protocolString = MakerwitUtil.buildProtocolString(protocol);
      response.write(protocolString);
      // TODO 是否需要写到数据库?
      // save()??
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }

    if (response != null) {
      // 发送新订单
      AppContext.getCommAdapter().getTelegramMatcher().getTelegramSender().sendTelegram(response);
    }
  }
  */
}

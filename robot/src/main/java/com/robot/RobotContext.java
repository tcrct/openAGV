package com.robot;

import com.robot.adapter.RobotCommAdapter;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IComponents;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.kernel.services.TransportOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by laotang on 2020/1/12.
 */
public class RobotContext {

    private static final Logger LOG = LoggerFactory.getLogger(RobotContext.class);

    // OpenAgv需要的组件接口
    private static IComponents components;
    // 车辆适配器集合
    private static Map<String, RobotCommAdapter> ADAPTER_MAP = new ConcurrentHashMap<>();

    /**
     * 取车辆适配器集合
     *
     * @return
     */
    public static Map<String, RobotCommAdapter> getAdapterMap() {
        return ADAPTER_MAP;
    }

    /***
     * 根据key取出Adapter
     * @param key 车辆标识关键字
     */
    public static RobotCommAdapter getAdapter(String key) {
        return ADAPTER_MAP.get(key);
    }



    /**
     * 设置OpenAGV需要使用的第三方组件实例接口
     *
     * @param components 第三方组件实例接口
     */
    public static void setRobotComponents(IComponents components) {
        RobotContext.components = components;
    }

    /**
     * 取第三方组件实例接口
     *
     * @return 第三方组件实例接口
     */
    public static IComponents getRobotComponents() {
        if (null == components) {
            throw new RobotException("第三方组件IComponents接口对象不能为null，请先实现并且在Duang.java里实现components方法");
        }
        return components;
    }

    /**
     * 大杀器
     */
    private static TCSObjectService tcsObjectService;

    public static void setTCSObjectService(TCSObjectService objectService) {
        if (null == tcsObjectService) {
            tcsObjectService = objectService;
        }
    }
    public static TCSObjectService getTCSObjectService() {
        return RobotContext.tcsObjectService;
    }


    /**
     * 订单服务
     */
    private static TransportOrderService transportOrderService;

    public static TransportOrderService getTransportOrderService() {
        return transportOrderService;
    }

    public static void setTransportOrderService(TransportOrderService transportOrderService) {
        RobotContext.transportOrderService = transportOrderService;
    }

    /**
     * 分发任务服务
     */
    private static DispatcherService dispatcherService;

    public static DispatcherService getDispatcherService() {
        return dispatcherService;
    }

    public static void setDispatcherService(DispatcherService dispatcherService) {
        RobotContext.dispatcherService = dispatcherService;
    }

    /**
     * 内核服务
     */
    private static KernelServicePortal kernelServicePortal;

    public static void setKernelServicePortal(KernelServicePortal servicePortal) {
        kernelServicePortal = servicePortal;
    }

    public static KernelServicePortal getKernelServicePortal() {
        return kernelServicePortal;
    }

}



package com.openagv.tools;

import com.openagv.core.AppContext;
import com.openagv.core.annotations.Controller;
import com.openagv.core.annotations.Service;
import com.openagv.core.command.SendCommand;
import com.openagv.core.interfaces.IDecomposeTelegram;
import com.openagv.core.interfaces.IRequest;
import com.openagv.exceptions.AgvException;
import com.openagv.opentcs.telegrams.OrderRequest;
import com.openagv.opentcs.telegrams.StateRequest;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ToolsKit {

    public static final String CONTROLLER_FIELD = "Controller";
    public static final String  SERVICE_FIELD = "Service";
    private static final String  SERVICE_IMPL_FIELD = "ServiceImpl";
    private static IDecomposeTelegram decomposeTelegram;

    /***
     * 判断传入的对象是否为空
     *
     * @param obj
     *            待检查的对象
     * @return 返回的布尔值,为空或等于0时返回true
     */
    public static boolean isEmpty(Object obj) {
        return checkObjectIsEmpty(obj, true);
    }

    /***
     * 判断传入的对象是否不为空
     *
     * @param obj
     *            待检查的对象
     * @return 返回的布尔值,不为空或不等于0时返回true
     */
    public static boolean isNotEmpty(Object obj) {
        return checkObjectIsEmpty(obj, false);
    }

    @SuppressWarnings("rawtypes")
    private static boolean checkObjectIsEmpty(Object obj, boolean bool) {
        if (null == obj) {
            return bool;
        }
        else if (obj == "" || "".equals(obj) || "null".equalsIgnoreCase(String.valueOf(obj))) {
            return bool;
        }
        else if (obj instanceof Integer || obj instanceof Long || obj instanceof Double) {
            try {
                Double.parseDouble(obj + "");
            } catch (Exception e) {
                return bool;
            }
        } else if (obj instanceof String) {
            if (((String) obj).length() <= 0) {
                return bool;
            }
            if ("null".equalsIgnoreCase(obj+"")) {
                return bool;
            }
        } else if (obj instanceof Map) {
            if (((Map) obj).size() == 0) {
                return bool;
            }
        } else if (obj instanceof Collection) {
            if (((Collection) obj).size() == 0) {
                return bool;
            }
        } else if (obj instanceof Object[]) {
            if (((Object[]) obj).length == 0) {
                return bool;
            }
        }
        return !bool;
    }

    /**
     * 合并数组
     *
     * @param firstArray
     *            第一个数组
     * @param secondArray
     *            第二个数组
     * @return 合并后的数组
     */
    public static byte[] concat(byte[] firstArray, byte[] secondArray) {
        if (firstArray == null || secondArray == null) {
            return null;
        }
        byte[] bytes = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, bytes, 0, firstArray.length);
        System.arraycopy(secondArray, 0, bytes, firstArray.length, secondArray.length);
        return bytes;
    }

    /**
     * 取车辆host名称
     * @return
     */
    public static String getVehicleHostName() {
        return  SettingUtils.getStringByGroup("host.name", "vehicle", "host");
    }

    /**
     * 取车辆port名称
     * @return
     */
    public static String getVehiclePortName() {
        return SettingUtils.getStringByGroup("port.name", "vehicle", "port");
    }

    /**
     * 端口最小值
     * @return
     */
    public static int getMinPort(){
        return SettingUtils.getInt("min.port", "vehicle", 5050);
    }

    /**
     * 端口最大值
     * @return
     */
    public static int getMaxPort(){
        return SettingUtils.getInt("max.port", "vehicle", 6060);
    }

    /**
     * 取Controller类名，去掉Controller结尾部分
     * 如果@Controller注解有指定value，则优先取value值
     *
     * @param clazz 类
     * @return 类名
     */
    public static String getInjectClassName(Class<?> annotation, Class<?> clazz) {
        java.util.Objects.requireNonNull(clazz, "Controller类不能为空");
        java.util.Objects.requireNonNull(annotation, "["+clazz.getName()+"]注解不能为空");
        String className = "";
        if(Controller.class.equals(annotation.getClass())) {
            Controller classAnnon = clazz.getAnnotation(Controller.class);
            className = classAnnon.value();
        }
        if(Service.class.equals(annotation.getClass())) {
            Service classAnnon = clazz.getAnnotation(Service.class);
            className = classAnnon.value();
        }
        if(ToolsKit.isEmpty(className)) {
            className = clazz.getSimpleName();
            className = className.replace(CONTROLLER_FIELD, "");
            className = className.replace(SERVICE_IMPL_FIELD, "");
            className = className.replace(SERVICE_FIELD, "");
        }
        return className;
    }

    public static <T> T sendCommand(List<IRequest> requestList) {
        return SendCommand.duang().execute(requestList);
    }

    public static <T> T sendCommand(StateRequest request) {
        return SendCommand.duang().execute(request);
    }

    /**
     * 将原始报文分析成IRequest对象
     * @param telegram
     * @return
     */
    public static List<IRequest> telegramToRequestList(String telegram) {
        if(null == decomposeTelegram) {
            decomposeTelegram = AppContext.getAgvConfigure().getDecomposeTelegram();
            java.util.Objects.requireNonNull(decomposeTelegram, "请先实现OpenAgvConfigure类里的getDecomposeTelegram方法");
        }
        List<IRequest> requestList = decomposeTelegram.handle(new OrderRequest(telegram));
        if(ToolsKit.isEmpty(requestList)) {
            throw new AgvException("返回的转换结果集不能为空");
        }
        return requestList;
    }

    public static boolean isInjectServiceClass(Class<?> clazz) {
        return null != clazz && clazz.isAnnotationPresent(Service.class) && clazz.getInterfaces().length >= 1;
    }

    public static boolean isInjectControllerClass(Class<?> clazz) {
        return null != clazz && clazz.isAnnotationPresent(Controller.class);
    }

    public static boolean isInjectClass(Class<?> clazz) {
        return null != clazz && (
                clazz.isAnnotationPresent(Controller.class) ||
                        clazz.isAnnotationPresent(Service.class));
    }


    /***
     * 根据点名称取openTCS线路图上的点
     */
    public static Point getPoint(String pointName){
        java.util.Objects.requireNonNull(pointName, "点名称不能为空");
        return AppContext.getOpenTcsObjectService().fetchObject(Point.class, pointName);
    }

    /***
     * 根据线名称取openTCS线路图上的线
     */
    public static Path getPath(String pathName){
        java.util.Objects.requireNonNull(pathName, "线名称不能为空");
        return AppContext.getOpenTcsObjectService().fetchObject(Path.class, pathName);
    }

    /***
     * 根据线名称取openTCS线路图上的车辆
     */
    public static Vehicle getVehicle(String vehicleName){
        java.util.Objects.requireNonNull(vehicleName, "车辆名称不能为空");
        return AppContext.getOpenTcsObjectService().fetchObject(Vehicle.class, vehicleName);
    }
}

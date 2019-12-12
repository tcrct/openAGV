package com.openagv.tools;

import com.duangframework.db.annotation.Entity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openagv.core.AppContext;
import com.openagv.core.annotations.Controller;
import com.openagv.core.annotations.Service;
import com.openagv.core.command.SendCommand;
import com.openagv.core.interfaces.IDecomposeTelegram;
import com.openagv.core.interfaces.IRequest;
import com.openagv.exceptions.AgvException;
import com.openagv.opentcs.model.ProcessModel;
import com.openagv.opentcs.telegrams.OrderRequest;
import com.openagv.opentcs.telegrams.StateRequest;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.opentcs.guing.util.I18nPlantOverview.MENU_PATH;

public class ToolsKit {

    private static final Logger logger = LoggerFactory.getLogger(ToolsKit.class);

    public static final String CONTROLLER_FIELD = "Controller";
    public static final String  SERVICE_FIELD = "Service";
    private static final String  SERVICE_IMPL_FIELD = "ServiceImpl";
    private static IDecomposeTelegram decomposeTelegram;

    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        /**过滤对象的null属性*/
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        /**过滤map中的null key*/
        objectMapper.getSerializerProvider().setNullKeySerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
                generator.writeFieldName("");
            }
        });
        /**过滤map中的null值*/
        objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
                generator.writeString("");
            }
        });
    }

    /**
     * json字符串转换为对象
     * @param jsonStr   json格式的字符串
     * @param clazz 待转换的对象
     * @param <T>  返回泛型值
     * @return
     * @throws Exception
     */
    public static <T> T jsonParseObject(String jsonStr, Class<T> clazz)  {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            throw new AgvException(e.getMessage() ,e);
        }
    }

    public static <T> List<T> jsonParseArray(String jsonStr, TypeReference<T> typeReference)  {
        try {
            return (List<T>)objectMapper.readValue(jsonStr, typeReference);
        } catch (Exception e) {
            throw new AgvException(e.getMessage(), e);
        }
    }


    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new AgvException(e.getMessage(), e);
        }
    }

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
        return decomposeTelegram.handle(new OrderRequest(telegram));
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
                        clazz.isAnnotationPresent(Service.class)) ||
                        clazz.isAnnotationPresent(Entity.class);
    }


    /***
     * 根据点名称取openTCS线路图上的点
     */
    public static Point getPoint(String pointName){
        java.util.Objects.requireNonNull(pointName, "点名称不能为空");
        return AppContext.getOpenTcsObjectService().fetchObject(Point.class, pointName);
    }

    /**
     * 是否停止点，默认点
     * @param point 点对象
     * @return 是停车点返回true
     */
    public static boolean isHaltPoint(Point point) {
        return Point.Type.HALT_POSITION.name().equals(point.getType().name());
    }

    /**
     * 是否报告点
     * @param point
     * @return
     */
    public static boolean isReportPoint(Point point) {
        return Point.Type.REPORT_POSITION.name().equals(point.getType().name());
    }

    /**
     * 是否停车点
     * @param point
     * @return
     */
    public static boolean isParkPoint(Point point) {
        return Point.Type.PARK_POSITION.name().equals(point.getType().name());
    }

    public static boolean isParkPoint(Point.Type pointType) {
        return Point.Type.PARK_POSITION.name().equals(pointType.name());
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

//    private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(MENU_PATH);
    public static String getWebEndPoint() {

        return "http://127.0.0.1:55200/v1";
    }


    /**
     * 是否矩形地图，则顶升AGV
     * @param processModel
     * @return
     */
    public static boolean isMatrix(ProcessModel processModel) {
        String matrixVehice = SettingUtils.getStringByGroup("vehicle", "matrix", "");
        boolean isMatrix =processModel.getVehicle().getName().equals(matrixVehice);
        return isMatrix;
    }


    public static boolean isTrafficControl(ProcessModel processModel) {
//        String matrixVehice = SettingUtils.getStringByGroup("vehicle", "matrix", "");
//        boolean isMatrix =processModel.getVehicle().getName().equals(matrixVehice);

        String vehicleName = processModel.getName();
        return ("A001".equalsIgnoreCase(vehicleName) || "A002".equalsIgnoreCase(vehicleName)) ? true : false;
    }


    /**
     * 此通信适配器的命令队列接受的命令数。必须至少为1。
     * @return
     */
    public static int getCommandQueueCapacity(){
//            return 100;
            return 3;
//            return isTrafficControl(AppContext.getCommAdapter().getProcessModel()) ? 3 :100;
    }

    /**
     * 要发送给车辆的最大订单数。
     * @return
     */
    public static int getSentQueueCapacity() {
//            return 100;
            return 2;
//            return isTrafficControl(AppContext.getCommAdapter().getProcessModel()) ? 2 :100;
    }

}

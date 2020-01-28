package com.makerwit.core.responses;

import com.makerwit.numes.MakerwitEnum;
import com.robot.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 代码：Sensor sensor = new Sensor.Builder().element(1,"1")
 * .element(2,"0")  //有多个时
 * .element(3,"1")  //有多个时
 * .element(4,"0")  //有多个时
 * .build();
 * <p>
 * 将参数： 1::0::1::0  转换为  0_1|1_0|2_1|3_0
 * 第1位的值为1，第2位的值为0，第3位的值为1，如此类推
 *
 * @author laotang
 */
public class Sensor implements java.io.Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Sensor.class);

    private static final String LINK_CHARACTER = "_";
    private static final String SQLIT_CHARACTER = "|";

    //传感器集合,key为车辆/设备ID，value为传感器对象
    private static Map<String, LinkedList<Sensor>> SENSOR_MAP = new HashMap<>();

    private Map<Integer, String> map = new TreeMap<>();

    private String code;

    private Sensor(Map<Integer, String> map) {
        this.map.putAll(map);
    }

    public Sensor(String paramStr) {
        String[] paramsArray = paramStr.split(SQLIT_CHARACTER);
        for (String paramItem : paramsArray) {
            String[] paramItemArray = paramItem.split(LINK_CHARACTER);
            map.put(Integer.parseInt(paramItemArray[0]), paramItemArray[1]);
        }
    }

    public boolean isWith(String params) {
        return isWith(params.split(MakerwitEnum.PARAMLINK.getValue()));
    }

    public boolean isWith(String[] params) {
        List<Boolean> booleanList = new ArrayList<>(map.size());
        for (Iterator<Integer> iterator = map.keySet().iterator(); iterator.hasNext(); ) {
            Integer index = iterator.next();
            if (params[index].equals(map.get(index))) {
                booleanList.add(true);
            }
        }
        // 个数不等
        if (booleanList.size() != map.size()) {
            LOG.info("个数不等，返回false");
            return false;
        }
        for (Boolean b : booleanList) {
            if (!b) {
                LOG.info("传感器提交上来与指定位置的数值不一致，返回false");
                return false;
            }
        }
        LOG.info("传感器提交上来与指定位置的数值一致，返回true");
        return true;
    }

    public static class Builder {
        private Map<Integer, String> map = new TreeMap<>();

        /**
         * 传感器参数
         *
         * @param index 索引位置，对于程序来讲0是第1位，对应参数的第1位
         * @param value 值
         * @return
         */
        public Builder element(Integer index, String value) {
            this.map.put(index, value);
            return this;
        }

        public Sensor build() {
            return new Sensor(map);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, String> entry = iterator.next();
            sb.append(entry.getKey()).append(LINK_CHARACTER).append(entry.getValue()).append(SQLIT_CHARACTER);
        }
        String resultString = "";
        if (sb.length() > 1) {
            resultString = sb.substring(0, sb.length() - 1);
        }
        return resultString;
    }

    public static Sensor getSensor(String deviceId) {
        LinkedList<Sensor> sensors = SENSOR_MAP.get(deviceId);
        if (Optional.ofNullable(sensors).isPresent() && !sensors.isEmpty()) {
            return sensors.peek(); //取出顶部位置
        }
        return null;
    }

    public static void removeSensor(String deviceId) {
        LinkedList<Sensor> sensors = SENSOR_MAP.get(deviceId);
        if (Optional.ofNullable(sensors).isPresent() && !sensors.isEmpty()) {
            Sensor sensor = sensors.remove(); //移除顶部位置
            LOG.info("移除设备[{}]顶部传感器缓存[{}]成功", deviceId, sensor.toString());
        }
    }

    public static void setSensor(String deviceId, Sensor sensor) {
        LinkedList<Sensor> sensors = SENSOR_MAP.get(deviceId);
        if (ToolsKit.isEmpty(sensors)) {
            sensors = new LinkedList<>();
        }
        sensors.add(sensor);
        SENSOR_MAP.put(deviceId, sensors);
        LOG.info("车辆/设备[{}]添加到传感器缓存成功, {}", deviceId, sensor.toString());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

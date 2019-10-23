package com.openagv.core.handshake;

import java.util.*;

/**
 * 代码：Sensor sensor = new Sensor.Builder().equals(1,"1")
 *                                                                          .equals(2,"0")  //有多个时
 *                                                                         .equals(3,"1")  //有多个时
 *                                                                         .equals(4,"0")  //有多个时
 *                                                                         .build();
 *
 * 将参数： 1::0::1::0  转换为  0_1|1_0|2_1|3_0
 * 第1位的值为1，第2位的值为0，第3位的值为1，如此类推
 *
 * @author laotang
 */
public class Sensor implements java.io.Serializable {

    private static final String LINK_CHARACTER = "_";
    private static final String SQLIT_CHARACTER = "|";

    private Map<Integer, String> map;

    private Sensor(Map<Integer, String> map) {
        this.map = map;
    }

    public Sensor(String paramStr) {
        String[] paramsArray = paramStr.split(SQLIT_CHARACTER);
        map = new TreeMap();
        for(String paramItem : paramsArray) {
            String[] paramItemArray = paramItem.split(LINK_CHARACTER);
            map.put(Integer.parseInt(paramItemArray[0]), paramItemArray[1]);
        }
    }

    public boolean isWith(String[] params) {
        List<Boolean> booleanList = new ArrayList<>(map.size());
        for(Iterator<Integer> iterator = map.keySet().iterator(); iterator.hasNext();) {
            Integer index = iterator.next();
            if(params[index].equals(map.get(index))) {
                booleanList.add(true);
            }
        }
        // 个数不等
        if(booleanList.size() != map.size()) {
            return false;
        }
        for(Boolean b : booleanList) {
            if(!b) {
                return false;
            }
        }

        return true;
    }

    public static class Builder {
        private Map<Integer, String> map = new TreeMap<>();
        private List<Integer> indexs = new ArrayList<>();
        private List<String> values = new ArrayList<>();

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
        for(Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<Integer, String> entry = iterator.next();
            sb.append(entry.getKey()).append(LINK_CHARACTER).append(entry.getValue()).append(SQLIT_CHARACTER);
        }
        String resultString = "";
        if(sb.length() > 1) {
            resultString = sb.substring(0, sb.length()-1);
        }
        return resultString;
    }
}

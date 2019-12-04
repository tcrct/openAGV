package com.openagv.core.interfaces;

import com.openagv.dto.PathStepDto;

import java.util.List;
import java.util.Map;

/**
 * Created by laotang on 2019/9/25.
 */
public interface IResponse extends java.io.Serializable{

    String TARGET_POINT_NAME = "OA_NPN";
    String HANDSHAKE_NAME = "OA_HS_KEY";
    String PARAM_POINT_STEP = "OA_PPS";
    String PARAM_STRING = "OA_PS";

    /**
     * 设置请求ID
     * @param requestId
     * @return
     */
    void setRequestId(String requestId);

    /**
     * 请求ID
     * @return 返回请求ID
     */
    String getRequestId();

    /**
     * 设置返回主体内容
     * @param returnObj     返回主体对象
     */
    void write(Object returnObj);

    /**
     * 取返回状态标识
     * @return
     */
    int getStatus();

    /**
     * 添加返回扩展信息
     * @param key           名称
     * @param value         值
     */
    void setParams(String key, String value);

    /**
     * 取返回扩展信息
     * @return
     */
    Map<String,Object> getParams();

    /**
     * 设置返回状态标识
     * @return  状态标识
     */
    void setStatus(int status);

    /**
     * 返回的内容
     * @return
     */
    String toString();

    /**
     * 设置所有下一个目标点名称的集合
     * 切记不包括起始点，起始点除外，即1为起始点，2为1的下一个点，则List集合里只包含2。
     * @return 目标点名称集合
     */
    void setNextPointNames(List<String> pointNameList);

    /**
     * 取目标位置点名称
     * @return
     */
    List<String> getNextPointNames();

    /**
     * 设置请求指令
     * @param key 指令关键字
     */
    void setCmdKey(String key);

    /**
     * 取请求指令
     * @return
     */
    String getCmdKey();

    /**
     * 取设备ID
     * @return
     */
    String getDeviceId();

    /**
     * 设置设备ID
     * @param deviceId  设备ID
     */
    void setDeviceId(String deviceId);

    /**
     * 设置握手标识字段
     * @param key 标识字段
     */
    void setHandshakeKey(String key);

    /**
     * 取得握手标识字段
     */
    String getHandshakeKey();

    boolean isHandshakeList();
    void setHandshakeListener(boolean isHandshakeList);

    void setPathStepList(List<PathStepDto> stepList);

}

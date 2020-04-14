package com.robot.mvc.core.telegram;

import cn.hutool.core.util.ReflectUtil;
import com.robot.mvc.core.enums.ReqType;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.IActionCommand;
import com.robot.mvc.core.interfaces.IProtocol;
import com.robot.mvc.core.interfaces.IService;
import com.robot.mvc.helpers.BeanHelper;
import com.robot.utils.ToolsKit;

/**
 * 工作站动作请求
 * 当车辆到达指定位置后，对工作站发起的一连串动作指令请求
 * 包括发送请求，等待响应回复
 * <p>
 * Created by laotang on 2020/1/12.
 */
public abstract class ActionRequest extends BaseRequest implements IActionCommand {

    /**
     * 用于ActionsQueue队列中，标记该动作请求的下标元素位置
     */
    private double index;
    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 业务服务类请求Dto对象
     */
    private ActionRequest.ServiceRequestDto serviceRequestDto;

    public ActionRequest(IProtocol protocol) {
        super(ReqType.ACTION, protocol);
        super.setNeedSend(true);
    }

    // 子任务不需要发送，不需要重复发送，不需要进入到adapter
    public ActionRequest(IProtocol protocol, ActionRequest.ServiceRequestDto serviceRequestDto) {
        super(ReqType.ACTION, protocol);
        super.setNeedSend(false);
        super.setNeedAdapterOperation(false);
        super.setNeedRepeatSend(false);
        super.setId("sub_" + this.getId());
        this.serviceRequestDto = serviceRequestDto;
    }

    public abstract String cmd();

    public double getIndex() {
        return index;
    }

    public void setIndex(double index) {
        this.index = index;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public ServiceRequestDto getServiceRequestDto() {
        return serviceRequestDto;
    }

    public static String callServiceMethod(ServiceRequestDto serviceRequestDto) {
        Object service = BeanHelper.duang().getBean(serviceRequestDto.getServiceClass());
        if (ToolsKit.isEmpty(service)) {
            throw new RobotException("根据["+serviceRequestDto.getServiceClass().getName()+"]没有找到实例对象，请检查！");
        }
        try {
            Object resultObj = ReflectUtil.invoke(service, serviceRequestDto.getMethodName(), serviceRequestDto.getParam());
            String result = "";
            if (resultObj instanceof String) {
                result = resultObj.toString();
            } else if (resultObj instanceof IProtocol) {
                result = ((IProtocol) resultObj).getParams();
            }
            return result;
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        }
    }


    public static class ServiceRequestDto {
        private Class<? extends IService> serviceClass;
        private String methodName;
        private Object param;

        public ServiceRequestDto(Class<? extends IService> serviceClass, String methodName) {
            this.serviceClass= serviceClass;
            this.methodName = methodName;
        }

        public ServiceRequestDto(Class<? extends IService> serviceClass, String methodName, Object param) {
            this.serviceClass= serviceClass;
            this.methodName = methodName;
            this.param = param;
        }

        public Class<? extends IService> getServiceClass() {
            return serviceClass;
        }

        public String getMethodName() {
            return methodName;
        }

        public Object getParam() {
            return param;
        }
    }
}

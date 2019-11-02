package com.openagv.opentcs.telegrams;

import com.openagv.core.AppContext;
import com.openagv.opentcs.model.ProcessModel;
import org.opentcs.drivers.vehicle.MovementCommand;

import java.util.Queue;

/**
 *  状态请求对象，该对象由OpenTCS在移动车辆时产生，属于openTCS主动发起的请求
 *
 * @author Laotang
 */
public class StateRequest extends AbsRequest {

    /** 车辆名称*/
    private String vehicleName;
    /**最终目的点，即停车点*/
    private String endPointName;
    /**到达最终停车点后执行的操作*/
    private String finalOperation;
    /**车辆参数模型*/
    private ProcessModel processModel;
    /**车辆移动命令队列*/
    private Queue<MovementCommand> movementCommandQueue;

    private StateRequest() {
        super(TelegramType.STATE);
    }

    @Override
    public String getRequestType() {
        return StateRequest.class.getSimpleName().toLowerCase();
    }

    private StateRequest(String vehicleName, String endPointName, String finalOperation, ProcessModel processModel, Queue<MovementCommand>  commandQueue) {
        super(TelegramType.STATE);
        this.vehicleName = vehicleName;
        this.endPointName = endPointName;
        this.finalOperation = finalOperation;
        this.processModel = processModel;
        this.movementCommandQueue = commandQueue;
    }

    public static class Builder {

        /**车辆参数模型*/
        private ProcessModel processModel;
        /**车辆移动命令*/
        private Queue<MovementCommand> movementCommandQueue;
        /**最后一条指令*/
        private MovementCommand finalCmd;

        public Builder model(ProcessModel processModel) {
            this.processModel = processModel;
            return this;
        }

        public Builder commandQuery(Queue<MovementCommand> movementCommandQueue) {
            this.movementCommandQueue = movementCommandQueue;
            return this;
        }

        public Builder finalCmd(MovementCommand movementCommand) {
            this.finalCmd = movementCommand;
            return this;
        }

        public StateRequest build() {
            StateRequest stateRequest = new StateRequest(
                    processModel.getVehicleReference().getName(),
                    finalCmd.getFinalDestination().getName(),
                    finalCmd.getFinalOperation(),
                    processModel,
                    movementCommandQueue
            );
            stateRequest.setCmdKey(AppContext.getStateRequestCmdKey());
            return stateRequest;
        }
    }

    @Override
    public void setCmdKey(String target) {
        super.target = target;
    }

    /**
     * 最终点名称
     * @return
     */
    public String getEndPointName() {
        return endPointName;
    }

    /**
     * 车辆名称
     * @return
     */
    public String getVehicleName() {
        return vehicleName;
    }

    /**
     * 最终点对应的操作
     * @return
     */
    public String getFinalOperation() {
        return finalOperation;
    }

    /**
     * process mode
     * @return
     */
    public ProcessModel getProcessModel() {
        return processModel;
    }

    /**
     * 移动命令队列集合
     * @return
     */
    public Queue<MovementCommand> getMovementCommandQueue() {
        return movementCommandQueue;
    }
}

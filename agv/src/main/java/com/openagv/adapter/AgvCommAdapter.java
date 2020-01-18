package com.openagv.adapter;

import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import com.google.inject.assistedinject.Assisted;
import com.openagv.config.AgvConfiguration;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.interfaces.IResponse;
import com.openagv.mvc.core.telegram.MoveRequest;
import com.openagv.mvc.core.telegram.ITelegram;
import com.openagv.mvc.core.telegram.ITelegramSender;
import com.openagv.mvc.utils.RequestKit;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.util.ExplainedBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

/**
 * AgvCommAdapter
 *
 * @blame Laotang
 */
public class AgvCommAdapter
        extends BasicVehicleCommAdapter
        implements ConnectionEventListener<ITelegram>,ITelegramSender {

    private static final Logger LOG = LoggerFactory.getLogger(AgvCommAdapter.class);

    /**大杀器*/
    private TCSObjectService tcsObjectService;
    /**执行器*/
    private ExecutorService kernelExecutor;
    /**适配器组件工厂*/
    private AdapterComponentsFactory componentsFactory;
    /**配置文件类*/
    private AgvConfiguration configuration;
    /**车辆*/
    private Vehicle vehicle;
    private List<MovementCommand> commandList = new ArrayList<>();

    @Inject
    public AgvCommAdapter(AdapterComponentsFactory componentsFactory,
                                        TCSObjectService tcsObjectService,
                                        AgvConfiguration configuration,
                                        @Assisted Vehicle vehicle,
                                        @KernelExecutor ExecutorService kernelExecutor) {
        super(new VehicleModel(vehicle),
                configuration.commandQueueCapacity(),
                configuration.sentQueueCapacity(),
                configuration.rechargeOperation());

        this.tcsObjectService = requireNonNull(tcsObjectService, "tcsObjectService");
        this.vehicle = requireNonNull(vehicle, "vehicle");
        this.configuration = requireNonNull(configuration, "configuration");
        this.componentsFactory = requireNonNull(componentsFactory, "componentsFactory");
        this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    }

    /**
     * 发送移动命令
     * @param cmd The command to be sent.
     * @throws IllegalArgumentException
     */
    @Override
    public void sendCommand(MovementCommand cmd) throws IllegalArgumentException {
        cmd = requireNonNull(cmd, "MovementCommand is null");
        commandList.add(cmd);
        // 最终命令时，则生成移动请求
        if (cmd.isFinalMovement()) {
            try {
                MoveRequest moveRequest = new MoveRequest(this, commandList);
                // 将请求发送到业务逻辑处理，自行实现所有的协议内容发送
                IResponse response = RequestKit.duang().request(moveRequest).execute();
                if (response.getStatus() != HttpStatus.HTTP_OK) {
                    LOG.error("车辆[{}]进行业务处理里发生异常，退出处理!", getName());
                } else {
//                    requestResponseMatcher.enqueueRequest(getProcessModel().getName(), response);
                }
            } catch (Exception e) {
                throw new AgvException("创建移动协议指令时出错: "+e.getMessage(), e);
            } finally {
                // 清空集合
                commandList.clear();
            }
        }
    }

    /**
     * 创建适配器面板
     * @return
     */
    @Override
    protected List<VehicleCommAdapterPanel> createAdapterPanels() {
        return null;
    }

    /**连接车辆*/
    @Override
    protected void connectVehicle() {

    }

    /**断开车辆连接*/
    @Override
    protected void disconnectVehicle() {

    }

    /**判断车辆是否已经连接*/
    @Override
    protected boolean isVehicleConnected() {
        return false;
    }

    /**是否执行进程操作，车辆移动命令发送前检查*/
    @Nonnull
    @Override
    public ExplainedBoolean canProcess(@Nonnull List<String> operations) {
        return null;
    }

    /**进程消息*/
    @Override
    public void processMessage(@Nullable Object message) {

    }

    /**
     * 发送报文
     * @param telegram 电报对象
     */
    @Override
    public void sendTelegram(ITelegram telegram) {

    }

    /**
     * 接收到报文信息
     * @param iTelegram 电报对象
     */
    @Override
    public void onIncomingTelegram(ITelegram iTelegram) {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onFailedConnectionAttempt() {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onIdle() {

    }
}

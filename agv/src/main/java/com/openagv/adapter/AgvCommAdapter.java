package com.openagv.adapter;

import com.google.inject.assistedinject.Assisted;
import com.openagv.config.AgvConfiguration;
import com.openagv.telegram.ITelegram;
import com.openagv.telegram.ITelegramSender;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.contrib.tcp.netty.ConnectionEventListener;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.BasicVehicleCommAdapter;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapterPanel;
import org.opentcs.util.ExplainedBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
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
}

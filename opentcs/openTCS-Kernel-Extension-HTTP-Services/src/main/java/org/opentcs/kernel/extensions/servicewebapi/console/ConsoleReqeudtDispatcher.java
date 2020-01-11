package org.opentcs.kernel.extensions.servicewebapi.console;

import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.customizations.kernel.KernelExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.requireNonNull;

public class ConsoleReqeudtDispatcher {

    private static Logger logger = LoggerFactory.getLogger(ConsoleReqeudtDispatcher.class);

//    private final CallWrapper callWrapper;
    private final VehicleService vehicleService;
    private final ExecutorService kernelExecutor;

    @Inject
    public ConsoleReqeudtDispatcher(VehicleService vehicleService,
                                    @KernelExecutor ExecutorService kernelExecutor) {
//        this.callWrapper = requireNonNull(callWrapper, "callWrapper");
        this.vehicleService = requireNonNull(vehicleService, "vehicleService");
        this.kernelExecutor = requireNonNull(kernelExecutor, "kernelExecutor");
    }

//    private TCSObjectReference<Vehicle> getVehicleReference(String vehicleName)
//            throws Exception {
//        return callWrapper.call(() -> vehicleService.fetchObject(Vehicle.class, vehicleName)).getReference();
//    }
//
//    private void sendCommAdapterCommand(AdapterCommand command) {
//        try {
//            TCSObjectReference<Vehicle> vehicleRef = getVehicleReference("A030");
//            callWrapper.call(() -> vehicleService.sendCommAdapterCommand(vehicleRef, command));
//        }
//        catch (Exception ex) {
//            logger.warn("Error sending comm adapter command '{}'", command, ex);
//        }
//    }

    public String position(String vehicleName, String position) {
        String str =  "vehicleName: " + vehicleName + "          position: " + position;
        System.out.println("ConsoleReqeudtDispatcher################:" + str);
        return str;
    }
}

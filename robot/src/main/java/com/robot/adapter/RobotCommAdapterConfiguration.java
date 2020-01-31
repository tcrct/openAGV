/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.adapter;

import org.opentcs.configuration.ConfigurationEntry;
import org.opentcs.configuration.ConfigurationPrefix;

/**
 * Provides methods to configure the {@link RobotCommAdapter}.
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
@ConfigurationPrefix(RobotCommAdapterConfiguration.PREFIX)
public interface RobotCommAdapterConfiguration {

    /**
     * This configuration's prefix.
     */
    String PREFIX = "robot.comm.adapter";

    @ConfigurationEntry(
            type = "Boolean",
            description = "Whether to register/enable the example communication adapter.",
            orderKey = "0_enable")
    boolean enable();

}

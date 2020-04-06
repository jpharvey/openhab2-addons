/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.cbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.slf4j.Logger;

import com.daveoxley.cbus.Application;
import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Group;
import com.daveoxley.cbus.Network;

/**
 * The {@link CBusGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */

@NonNullByDefault
public abstract class CBusGroupHandler extends BaseThingHandler {

    protected final Logger logger;
    protected @Nullable CBusNetworkHandler cBusNetworkHandler = null;
    protected @Nullable Group group = null;
    protected int applicationId = -1;
    protected int groupId = -1;

    public CBusGroupHandler(Thing thing, int applicationId, Logger logger) {
        super(thing);
        this.applicationId = applicationId;
        this.logger = logger;
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @SuppressWarnings({ "null", "unused" })
    @Override
    public void initialize() {
        /*
         * Group Id has moved from config to property. Copy for backward compatibility
         */
        if (getThing().getProperties().get(CBusBindingConstants.PROPERTY_GROUP_ID) == null) {
            if (getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID) != null) {
                updateProperty(CBusBindingConstants.PROPERTY_GROUP_ID,
                        getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID).toString());
            }
        }
        if (getThing().getProperties().get(CBusBindingConstants.PROPERTY_GROUP_NAME) == null) {
            if (getConfig().get(CBusBindingConstants.CONFIG_NAME) != null) {
                updateProperty(CBusBindingConstants.PROPERTY_GROUP_NAME,
                        getConfig().get(CBusBindingConstants.CONFIG_NAME).toString());
            }
        }
        groupId = Integer.parseInt(getThing().getProperties().get(CBusBindingConstants.PROPERTY_GROUP_ID));
        cBusNetworkHandler = getCBusNetworkHandler();
        if (cBusNetworkHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        updateStatus();
    }

    @SuppressWarnings({ "null" })
    public void updateStatus() {
        try {
            logger.debug("updateStatus {}", getThing().getProperties().get(CBusBindingConstants.PROPERTY_GROUP_ID));
            CBusNetworkHandler networkHandler = cBusNetworkHandler;
            if (networkHandler == null || !networkHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                Group group = this.group;
                if (group == null) {
                    @Nullable
                    Object groupId = getThing().getProperties().get(CBusBindingConstants.PROPERTY_GROUP_ID);
                    if (groupId != null) {
                        group = getGroup(Integer.parseInt(groupId.toString()));
                        this.group = group;
                    }
                }
                if (group == null) {
                    logger.debug("Set state to configuration error -no group");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                } else if (group.getNetwork().isOnline()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Network is not reporting online");
                }
            }
        } catch (CGateException e) {
            logger.warn("Problem checking network state for network {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    public void updateGroup(int application, int group, String value) {
    }

    private @Nullable Group getGroup(int groupID) {
        try {
            CBusNetworkHandler networkHandler = cBusNetworkHandler;
            if (networkHandler == null) {
                return null;
            }
            Network network = networkHandler.getNetwork();
            if (network != null) {
                Application application = network.getApplication(applicationId);
                logger.debug("GetGroup for id {}", groupID);
                return application.getGroup(groupID);
            }
        } catch (CGateException e) {
            logger.debug("GetGroup for id {} faile {}", groupID, e.getMessage());
        }
        return null;
    }

    private synchronized @Nullable CBusNetworkHandler getCBusNetworkHandler() {
        CBusNetworkHandler bridgeHandler = null;
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device .");
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof CBusNetworkHandler) {
            bridgeHandler = (CBusNetworkHandler) handler;
        } else {
            logger.debug("No available bridge handler found for bridge: {} .", bridge.getUID());
        }
        return bridgeHandler;
    }
}

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
import org.slf4j.LoggerFactory;

import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Group;

/**
 * The {@link CBusGroupHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */

@NonNullByDefault
public abstract class CBusGroupHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected @Nullable CBusNetworkHandler cBusNetworkHandler = null;
    protected @Nullable Group group = null;

    public CBusGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);

    @Override
    public void initialize() {
        cBusNetworkHandler = getCBusNetworkHandler();

        try {
            this.group = getGroup(Integer.parseInt(getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID).toString()));
            if (this.group == null)
                logger.debug("cannot create group {} ",
                        getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID).toString());
        } catch (Exception e) {
            logger.warn("Cannot create group {} ", getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID).toString(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        updateStatus();
    }

    public void updateStatus() {
        try {
            CBusNetworkHandler networkHandler = cBusNetworkHandler;
            if (networkHandler == null || !networkHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                Group group = this.group;
                if (group == null) {
                    this.group = getGroup(
                            Integer.parseInt(getConfig().get(CBusBindingConstants.CONFIG_GROUP_ID).toString()));
                    group = this.group;
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

    protected abstract @Nullable Group getGroup(int groupID) throws CGateException;

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

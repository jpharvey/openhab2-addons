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
package org.openhab.binding.cbus.internal;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.handler.CBusCGateHandler;
import org.openhab.binding.cbus.handler.CBusDaliHandler;
import org.openhab.binding.cbus.handler.CBusLightHandler;
import org.openhab.binding.cbus.handler.CBusNetworkHandler;
import org.openhab.binding.cbus.handler.CBusTemperatureHandler;
import org.openhab.binding.cbus.handler.CBusTriggerHandler;
import org.openhab.binding.cbus.internal.discovery.CBusGroupDiscovery;
import org.openhab.binding.cbus.internal.discovery.CBusNetworkDiscovery;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link CBusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Scott Linton - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.cbus")
public class CBusHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return CBusBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(CBusBindingConstants.BRIDGE_TYPE_CGATE)) {
            CBusCGateHandler handler = new CBusCGateHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }

        if (thingTypeUID.equals(CBusBindingConstants.BRIDGE_TYPE_NETWORK)) {
            CBusNetworkHandler handler = new CBusNetworkHandler((Bridge) thing);
            registerDeviceDiscoveryService(handler);
            return handler;
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_LIGHT)) {
            return new CBusLightHandler(thing);
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_TEMPERATURE)) {
            return new CBusTemperatureHandler(thing);
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_TRIGGER)) {
            return new CBusTriggerHandler(thing);
        }

        if (thingTypeUID.equals(CBusBindingConstants.THING_TYPE_DALI)) {
            return new CBusDaliHandler(thing);
        }

        return null;
    }

    private void registerDeviceDiscoveryService(CBusCGateHandler cbusCgateHandler) {
        CBusNetworkDiscovery discoveryService = new CBusNetworkDiscovery(cbusCgateHandler);
        super.bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    private void registerDeviceDiscoveryService(CBusNetworkHandler cbusNetworkHandler) {
        CBusGroupDiscovery discoveryService = new CBusGroupDiscovery(cbusNetworkHandler);
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }
}

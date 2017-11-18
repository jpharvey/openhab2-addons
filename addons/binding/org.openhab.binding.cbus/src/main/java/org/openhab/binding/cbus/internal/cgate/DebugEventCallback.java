/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.internal.cgate;

import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class DebugEventCallback extends EventCallback {
    private Logger logger = LoggerFactory.getLogger(DebugEventCallback.class);

    @Override
    public boolean acceptEvent(int event_code) {
        return logger.isDebugEnabled(); // Accept all events if debug enabled
    }

    @Override
    public void processEvent(CGateSession cgate_session, int event_code, GregorianCalendar event_time, String event) {
        // logger.debug("event_code: " + event_code + ", event_time: " + event_time.toString() + ", event: " + event);
    }

}

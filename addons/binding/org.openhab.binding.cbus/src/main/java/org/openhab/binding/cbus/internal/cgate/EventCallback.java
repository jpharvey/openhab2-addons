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

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public abstract class EventCallback {
    /**
     *
     * @param event_code
     * @return
     */
    public abstract boolean acceptEvent(int event_code);

    /**
     *
     * @param cgate_session
     * @param event_time
     * @param event_code
     * @param event
     */
    public abstract void processEvent(CGateSession cgate_session, int event_code, GregorianCalendar event_time,
            String event);
}

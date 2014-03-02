/*
 * Copyright (c) 2006-2014 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.parser_xmpp;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

/**
 * An extension of XMPPConnection that hacks around the problems caused by Smack's ridiculous use of
 * static initialisers (and the dependencies between them). See
 * http://issues.igniterealtime.org/browse/SMACK-315 for info.
 */
public class FixedXmppConnection extends XMPPConnection
        implements ConnectionCreationListener {

    /** A set of listeners that were registered with the XMPP Parser. */
    private final Set<ConnectionCreationListener> oldListeners = new HashSet<>();

    /**
     * Creates a new fixed XMPP connection with the specified config.
     *
     * @param config The config to pass on to the underlying connection
     *
     * @see XMPPConnection#XMPPConnection(org.jivesoftware.smack.ConnectionConfiguration)
     */
    public FixedXmppConnection(final ConnectionConfiguration config) {
        super(config);
    }

    @Override
    public void connect() throws XMPPException {
        // We're fiddling with a static array
        synchronized (FixedXmppConnection.class) {
            final Set<ConnectionCreationListener> rawListeners = getListeners();

            // Copy them so we can clear and restore later
            oldListeners.addAll(rawListeners);

            // Get rid of everything and add ourselves.
            rawListeners.clear();
            rawListeners.add(this);

            try {
                super.connect();
            } finally {
                // Restore to the previous state
                rawListeners.remove(this);
                rawListeners.addAll(oldListeners);
                oldListeners.clear();
            }
        }
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void connectionCreated(final Connection xmppc) {
        // Creating this shoves itself into a static map. Other listeners
        // depend on this entry existing in the map before they're called.
        new ServiceDiscoveryManager(xmppc);

        // Now execute our real listeners
        for (ConnectionCreationListener listener : oldListeners) {
            // We've already created a discovery manager, don't make another...
            if (!ServiceDiscoveryManager.class.equals(listener.getClass().getEnclosingClass())) {
                listener.connectionCreated(xmppc);
            }
        }
    }

    /**
     * Retrieves the set of listeners that have been registered statically with the XMPPConnection
     * class.
     *
     * @return The raw listener set.
     */
    @SuppressWarnings("unchecked")
    private Set<ConnectionCreationListener> getListeners() {
        try {
            final Field field = XMPPConnection.class.getDeclaredField(
                    "connectionEstablishedListeners");
            field.setAccessible(true);
            return (Set<ConnectionCreationListener>) field.get(null);
        } catch (ReflectiveOperationException ex) {
            // Throws a bunch of exceptions... Try to carry on anyway, the
            // horrible concurrency bugs might not bite.
            return new HashSet<>();
        }
    }

}

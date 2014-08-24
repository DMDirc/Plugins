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

package com.dmdirc.addons.identd;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ServerConnectErrorEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerConnectingEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

public class IdentdManager {

    /** List of all the connections that need ident replies. */
    private final List<Connection> connections;
    /** Global config. */
    private final AggregateConfigProvider config;
    /** This plugin's settings domain. */
    private final String domain;
    /** Ident server. */
    private final IdentdServer server;
    /** Event bus to subscribe to events on. */
    private final DMDircMBassador eventBus;

    @Inject
    public IdentdManager(@GlobalConfig final AggregateConfigProvider config,
            @PluginDomain(IdentdPlugin.class) final String domain,
            final IdentdServer server, final DMDircMBassador eventBus) {
        connections = new ArrayList<>();
        this.config = config;
        this.domain = domain;
        this.server = server;
        this.eventBus = eventBus;
    }

    /**
     * Called when the plugin is loaded.
     */
    public void onLoad() {
        // Add action hooks
        eventBus.subscribe(this);

        if (config.getOptionBool(domain, "advanced.alwaysOn")) {
            server.startServer();
        }
    }

    /**
     * Called when this plugin is unloaded.
     */
    public void onUnload() {
        eventBus.unsubscribe(this);
        server.stopServer();
        connections.clear();
    }

    @Handler
    public void handleServerConnecting(final ServerConnectingEvent event) {
        synchronized (connections) {
                if (connections.isEmpty()) {
                    server.startServer();
                }
                connections.add(event.getConnection());
            }
    }

    @Handler
    public void handleServerConnected(final ServerConnectedEvent event) {
        handleServerRemoved(event.getConnection());
    }

    @Handler
    public void handleServerConnectError(final ServerConnectErrorEvent event) {
        handleServerRemoved(event.getConnection());
    }

    private void handleServerRemoved(final Connection connection) {
        synchronized (connections) {
                connections.remove(connection);

                if (connections.isEmpty() && !config.getOptionBool(domain, "advanced.alwaysOn")) {
                    server.stopServer();
                }
            }
    }

}

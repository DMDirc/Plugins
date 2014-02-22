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
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class IdentdManager implements ActionListener {

    /** List of all the connections that need ident replies. */
    private final List<Connection> connections;
    /** Global config. */
    private final AggregateConfigProvider config;
    /** This plugin's settings domain. */
    private final String domain;
    /** Ident server. */
    private final IdentdServer server;
    /** Action controller to register action listeners with. */
    private final ActionController actionController;

    @Inject
    public IdentdManager(@GlobalConfig final AggregateConfigProvider config,
            @PluginDomain(IdentdPlugin.class) final String domain,
            final IdentdServer server, final ActionController actionController) {
        connections = new ArrayList<>();
        this.config = config;
        this.domain = domain;
        this.server = server;
        this.actionController = actionController;
    }

    /**
     * Called when the plugin is loaded.
     */
    public void onLoad() {
        // Add action hooks
        actionController.registerListener(this,
                CoreActionType.SERVER_CONNECTED,
                CoreActionType.SERVER_CONNECTING,
                CoreActionType.SERVER_CONNECTERROR);

        if (config.getOptionBool(domain, "advanced.alwaysOn")) {
            server.startServer();
        }
    }

    /**
     * Called when this plugin is unloaded.
     */
    public void onUnload() {
        actionController.unregisterListener(this);
        server.stopServer();
        connections.clear();
    }

    /**
     * Process an event of the specified type.
     *
     * @param type      The type of the event to process
     * @param format    Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.SERVER_CONNECTING) {
            synchronized (connections) {
                if (connections.isEmpty()) {
                    server.startServer();
                }
                connections.add((Connection) arguments[0]);
            }
        } else if (type == CoreActionType.SERVER_CONNECTED
                || type == CoreActionType.SERVER_CONNECTERROR) {
            synchronized (connections) {
                connections.remove((Connection) arguments[0]);

                if (connections.isEmpty() && !config.getOptionBool(domain, "advanced.alwaysOn")) {
                    server.stopServer();
                }
            }
        }
    }

}

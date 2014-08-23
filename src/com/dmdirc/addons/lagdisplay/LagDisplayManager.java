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

package com.dmdirc.addons.lagdisplay;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.events.ServerDisconnectedEvent;
import com.dmdirc.events.ServerGotpingEvent;
import com.dmdirc.events.ServerNopingEvent;
import com.dmdirc.events.ServerNumericEvent;
import com.dmdirc.events.ServerPingsentEvent;
import com.dmdirc.events.StatusBarComponentAddedEvent;
import com.dmdirc.events.StatusBarComponentRemovedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.util.collections.RollingList;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Manages the lifecycle of the lag display plugin.
 */
@Singleton
public class LagDisplayManager implements ConfigChangeListener, SelectionListener {

    /** Event bus to receive events on. */
    private final EventBus eventBus;
    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** Status bar to add panels to. */
    private final SwingStatusBar statusBar;
    private final Provider<LagDisplayPanel> panelProvider;
    /** The settings domain to use. */
    private final String domain;
    /** Config to read global settings from. */
    private final AggregateConfigProvider globalConfig;
    /** A cache of ping times. */
    private final Map<Connection, String> pings = new WeakHashMap<>();
    /** Ping history. */
    private final Map<Connection, RollingList<Long>> history = new HashMap<>();
    /** Whether or not to show a graph in the info popup. */
    private boolean showGraph = true;
    /** Whether or not to show labels on that graph. */
    private boolean showLabels = true;
    /** The length of history to keep per-server. */
    private int historySize = 100;
    /** The panel currently in use. Null before {@link #load()} or after {@link #unload()}. */
    private LagDisplayPanel panel;

    @Inject
    public LagDisplayManager(
            final EventBus eventBus,
            final ActiveFrameManager activeFrameManager,
            final SwingStatusBar statusBar,
            final Provider<LagDisplayPanel> panelProvider,
            @PluginDomain(LagDisplayPlugin.class) final String domain,
            @GlobalConfig final AggregateConfigProvider globalConfig) {
        this.eventBus = eventBus;
        this.activeFrameManager = activeFrameManager;
        this.statusBar = statusBar;
        this.panelProvider = panelProvider;
        this.domain = domain;
        this.globalConfig = globalConfig;
    }

    public void load() {
        panel = panelProvider.get();
        eventBus.post(new StatusBarComponentAddedEvent(panel));
        activeFrameManager.addSelectionListener(this);
        globalConfig.addChangeListener(domain, this);
        readConfig();
        eventBus.register(this);
    }

    public void unload() {
        eventBus.post(new StatusBarComponentRemovedEvent(panel));
        activeFrameManager.removeSelectionListener(this);
        globalConfig.removeListener(this);
        eventBus.unregister(this);
        panel = null;
    }

    /** Reads the plugin's global configuration settings. */
    private void readConfig() {
        showGraph = globalConfig.getOptionBool(domain, "graph");
        showLabels = globalConfig.getOptionBool(domain, "labels");
        historySize = globalConfig.getOptionInt(domain, "history");
    }

    /**
     * Retrieves the history of the specified server. If there is no history, a new list is added to
     * the history map and returned.
     *
     * @param connection The connection whose history is being requested
     *
     * @return The history for the specified server
     */
    protected RollingList<Long> getHistory(final Connection connection) {
        if (!history.containsKey(connection)) {
            history.put(connection, new RollingList<Long>(historySize));
        }
        return history.get(connection);
    }

    /**
     * Determines if the {@link ServerInfoDialog} should show a graph of the ping time for the
     * current server.
     *
     * @return True if a graph should be shown, false otherwise
     */
    public boolean shouldShowGraph() {
        return showGraph;
    }

    /**
     * Determines if the {@link PingHistoryPanel} should show labels on selected points.
     *
     * @return True if labels should be shown, false otherwise
     */
    public boolean shouldShowLabels() {
        return showLabels;
    }

    @Override
    public void selectionChanged(final TextFrame window) {
        final FrameContainer source = window.getContainer();
        if (source == null || source.getConnection() == null) {
            panel.getComponent().setText("Unknown");
        } else if (source.getConnection().getState() != ServerState.CONNECTED) {
            panel.getComponent().setText("Not connected");
        } else {
            panel.getComponent().setText(getTime(source.getConnection()));
        }
        panel.refreshDialog();
    }

    @Subscribe
    public void handleServerNumeric(final ServerNumericEvent event) {
        if (event.getNumeric() != 421) {
            return;
        }
        boolean useAlternate = ((Server) event.getConnection()).getConfigManager()
                .getOptionBool(domain, "usealternate");
        final TextFrame activeFrame = activeFrameManager.getActiveFrame();
        final FrameContainer active = activeFrame == null ? null : activeFrame.getContainer();
        final boolean isActive = active != null && event.getConnection().equals(active.
                getConnection());
        final String[] args = event.getArgs();
        if (useAlternate && args[3].startsWith("LAGCHECK_")) {
            try {
                final long sent = Long.parseLong(args[3].substring(9));
                final Long duration = new Date().getTime() - sent;
                final String value = formatTime(duration);
                pings.put(event.getConnection(), value);
                getHistory(event.getConnection()).add(duration);
                if (isActive) {
                    panel.getComponent().setText(value);
                }
            } catch (NumberFormatException ex) {
                pings.remove(event.getConnection());
            }
            event.setDisplayFormat("");

            panel.refreshDialog();
        }
    }

    @Subscribe
    public void handleServerDisconnected(final ServerDisconnectedEvent event) {
        final TextFrame activeFrame = activeFrameManager.getActiveFrame();
        final FrameContainer active = activeFrame == null ? null : activeFrame.getContainer();
        final boolean isActive = active != null && event.getConnection().equals(active.
                getConnection());
        if (isActive) {
                panel.getComponent().setText("Not connected");
                pings.remove(event.getConnection());
            }

            panel.refreshDialog();
    }

    @Subscribe
    public void handleServerGotPing(final ServerGotpingEvent event) {
        if (event.getConnection().getWindowModel().getConfigManager().
                getOptionBool(domain, "usealternate")) {
            return;
        }
        final TextFrame activeFrame = activeFrameManager.getActiveFrame();
        final FrameContainer active = activeFrame == null ? null : activeFrame.getContainer();
        final boolean isActive = active != null && event.getConnection().equals(active.
                getConnection());
        final String value = formatTime(event.getPing());

        getHistory(event.getConnection()).add(event.getPing());
        pings.put((event.getConnection()), value);

        if (isActive) {
            panel.getComponent().setText(value);
        }

        panel.refreshDialog();
    }

    @Subscribe
    public void handleServerNoPing(final ServerNopingEvent event) {
        if (event.getConnection().getWindowModel().getConfigManager().
                getOptionBool(domain, "usealternate")) {
            return;
        }
        final TextFrame activeFrame = activeFrameManager.getActiveFrame();
        final FrameContainer active = activeFrame == null ? null : activeFrame.getContainer();
        final boolean isActive = active != null && event.getConnection().equals(active.
                getConnection());
        final String value = formatTime(event.getPing()) + "+";

        pings.put(event.getConnection(), value);

        if (isActive) {
            panel.getComponent().setText(value);
        }

        panel.refreshDialog();
    }

    @Subscribe
    public void HandleServerPingSent(final ServerPingsentEvent event) {
        if (!event.getConnection().getWindowModel().getConfigManager().
                getOptionBool(domain, "usealternate")) {
            return;
        }
        event.getConnection().getParser().sendRawMessage("LAGCHECK_" + new Date().getTime());
    }

    /**
     * Retrieves the ping time for the specified connection.
     *
     * @param connection The connection whose ping time is being requested
     *
     * @return A String representation of the current lag, or "Unknown"
     */
    public String getTime(final Connection connection) {
        return pings.get(connection) == null ? "Unknown" : pings.get(connection);
    }

    /**
     * Formats the specified time so it's a nice size to display in the label.
     *
     * @param object An uncast Long representing the time to be formatted
     *
     * @return Formatted time string
     */
    protected String formatTime(final Object object) {
        final Long time = (Long) object;

        if (time >= 10000) {
            return Math.round(time / 1000.0) + "s";
        } else {
            return time + "ms";
        }
    }

    @Override
    public void configChanged(final String domain, final String key) {
        readConfig();
    }

}

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

import com.dmdirc.FrameContainer;
import com.dmdirc.ServerState;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BasePlugin;
import com.dmdirc.util.collections.RollingList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import dagger.ObjectGraph;

/**
 * Displays the current server's lag in the status bar.
 */
public final class LagDisplayPlugin extends BasePlugin implements
        ActionListener, ConfigChangeListener, SelectionListener {

    /** The panel we use in the status bar. */
    private LagDisplayPanel panel;
    /** A cache of ping times. */
    private final Map<Connection, String> pings = new WeakHashMap<>();
    /** Ping history. */
    private final Map<Connection, RollingList<Long>> history = new HashMap<>();
    /** Parent Swing UI. */
    private final SwingController controller;
    /** Whether or not to show a graph in the info popup. */
    private boolean showGraph = true;
    /** Whether or not to show labels on that graph. */
    private boolean showLabels = true;
    /** The length of history to keep per-server. */
    private int historySize = 100;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** Global config. */
    private AggregateConfigProvider config;

    /**
     * Creates a new LagDisplayPlugin.
     *
     * @param controller The controller to add components to
     * @param pluginInfo This plugin's plugin info
     */
    public LagDisplayPlugin(final SwingController controller,
            final PluginInfo pluginInfo) {
        super();
        this.controller = controller;
        this.pluginInfo = pluginInfo;
        config = controller.getGlobalConfig();
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new LagDisplayModule(this)));
        panel = getObjectGraph().get(LagDisplayPanel.class);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        controller.getSwingStatusBar().addComponent(panel);
        controller.getMainFrame().addSelectionListener(this);
        config.addChangeListener(getDomain(), this);
        readConfig();
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.SERVER_GOTPING, CoreActionType.SERVER_NOPING,
                CoreActionType.SERVER_DISCONNECTED,
                CoreActionType.SERVER_PINGSENT, CoreActionType.SERVER_NUMERIC);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        controller.getMainFrame().removeSelectionListener(this);
        controller.getSwingStatusBar().removeComponent(panel);
        config.removeListener(this);
        ActionManager.getActionManager().unregisterListener(this);
    }

    /** Reads the plugin's global configuration settings. */
    private void readConfig() {
        showGraph = config.getOptionBool(getDomain(), "graph");
        showLabels = config.getOptionBool(getDomain(), "labels");
        historySize = config.getOptionInt(getDomain(), "history");
    }

    /**
     * Retrieves the history of the specified server. If there is no history,
     * a new list is added to the history map and returned.
     *
     * @param connection The connection whose history is being requested
     * @return The history for the specified server
     */
    protected RollingList<Long> getHistory(final Connection connection) {
        if (!history.containsKey(connection)) {
            history.put(connection, new RollingList<Long>(historySize));
        }
        return history.get(connection);
    }

    /**
     * Determines if the {@link ServerInfoDialog} should show a graph of the
     * ping time for the current server.
     *
     * @return True if a graph should be shown, false otherwise
     */
    public boolean shouldShowGraph() {
        return showGraph;
    }

    /**
     * Determines if the {@link PingHistoryPanel} should show labels on
     * selected points.
     *
     * @return True if labels should be shown, false otherwise
     */
    public boolean shouldShowLabels() {
        return showLabels;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        boolean useAlternate = false;

        for (Object obj : arguments) {
            if (obj instanceof FrameContainer
                    && ((FrameContainer) obj).getConfigManager() != null) {
                useAlternate = ((FrameContainer) obj).getConfigManager()
                        .getOptionBool(getDomain(), "usealternate");
                break;
            }
        }

        final TextFrame activeFrame = controller.getMainFrame().getActiveFrame();
        final FrameContainer active = activeFrame == null ? null
                : activeFrame.getContainer();
        final boolean isActive = active != null
                && arguments[0] instanceof Connection
                && ((Connection) arguments[0]).equals(active.getConnection());

        if (!useAlternate && type.equals(CoreActionType.SERVER_GOTPING)) {
            final String value = formatTime(arguments[1]);

            getHistory(((Connection) arguments[0])).add((Long) arguments[1]);
            pings.put(((Connection) arguments[0]), value);

            if (isActive) {
                panel.getComponent().setText(value);
            }

            panel.refreshDialog();
        } else if (!useAlternate && type.equals(CoreActionType.SERVER_NOPING)) {
            final String value = formatTime(arguments[1]) + "+";

            pings.put(((Connection) arguments[0]), value);

            if (isActive) {
                panel.getComponent().setText(value);
            }

            panel.refreshDialog();
        } else if (type.equals(CoreActionType.SERVER_DISCONNECTED)) {
            if (isActive) {
                panel.getComponent().setText("Not connected");
                pings.remove(arguments[0]);
            }

            panel.refreshDialog();
        } else if (useAlternate && type.equals(CoreActionType.SERVER_PINGSENT)) {
            ((Connection) arguments[0]).getParser().sendRawMessage("LAGCHECK_" + new Date().getTime());
        } else if (useAlternate && type.equals(CoreActionType.SERVER_NUMERIC)
                && ((Integer) arguments[1]) == 421
                && ((String[]) arguments[2])[3].startsWith("LAGCHECK_")) {
            try {
                final long sent = Long.parseLong(((String[]) arguments[2])[3].substring(9));
                final Long duration = new Date().getTime() - sent;
                final String value = formatTime(duration);

                pings.put((Connection) arguments[0], value);
                getHistory(((Connection) arguments[0])).add(duration);

                if (isActive) {
                    panel.getComponent().setText(value);
                }
            } catch (NumberFormatException ex) {
                pings.remove(arguments[0]);
            }

            if (format != null) {
                format.delete(0, format.length());
            }

            panel.refreshDialog();
        }
    }

    /**
     * Retrieves the ping time for the specified connection.
     *
     * @param connection The connection whose ping time is being requested
     * @return A String representation of the current lag, or "Unknown"
     */
    public String getTime(final Connection connection) {
        return pings.get(connection) == null ? "Unknown" : pings.get(connection);
    }

    /**
     * Formats the specified time so it's a nice size to display in the label.
     * @param object An uncast Long representing the time to be formatted
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

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory cat = new PluginPreferencesCategory(
                pluginInfo, "Lag display plugin", "");
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "usealternate",
                "Alternate method", "Use an alternate method of determining "
                + "lag which bypasses bouncers or proxies that may reply?",
                manager.getConfigManager(), manager.getIdentity()));
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "graph", "Show graph", "Show a graph of ping times " +
                "for the current server in the information popup?",
                manager.getConfigManager(), manager.getIdentity()));
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "labels", "Show labels", "Show labels on selected " +
                "points on the ping graph?",
                manager.getConfigManager(), manager.getIdentity()));
        cat.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "history", "Graph points", "Number of data points " +
                "to plot on the graph, if enabled.",
                manager.getConfigManager(), manager.getIdentity()));
        manager.getCategory("Plugins").addSubCategory(cat);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        readConfig();
    }
}

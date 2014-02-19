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

import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BasePlugin;

import dagger.ObjectGraph;

/**
 * Displays the current server's lag in the status bar.
 */
public final class LagDisplayPlugin extends BasePlugin {

    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** The manager currently in use. */
    private LagDisplayManager manager;

    /**
     * Creates a new LagDisplayPlugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public LagDisplayPlugin(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new LagDisplayModule(this, pluginInfo.getDomain())));
        manager = getObjectGraph().get(LagDisplayManager.class);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        manager.load();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        manager.unload();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory cat = new PluginPreferencesCategory(
                pluginInfo, "Lag display plugin", "");
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "usealternate",
                "Alternate method", "Use an alternate method of determining "
                + "lag which bypasses bouncers or proxies that may reply?",
                manager.getConfigManager(), manager.getIdentity()));
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "graph", "Show graph", "Show a graph of ping times "
                + "for the current server in the information popup?",
                manager.getConfigManager(), manager.getIdentity()));
        cat.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "labels", "Show labels", "Show labels on selected "
                + "points on the ping graph?",
                manager.getConfigManager(), manager.getIdentity()));
        cat.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "history", "Graph points", "Number of data points "
                + "to plot on the graph, if enabled.",
                manager.getConfigManager(), manager.getIdentity()));
        manager.getCategory("Plugins").addSubCategory(cat);
    }

}

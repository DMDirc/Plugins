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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import java.util.concurrent.Callable;

import dagger.ObjectGraph;

/**
 * Plugin that allows users to advertise what they're currently playing or listening to.
 */
public class NowPlayingPlugin extends BaseCommandPlugin {

    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** This plugin's settings domain. */
    private final String domain;
    /** Now playing manager. */
    private NowPlayingManager nowplayingmanager;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public NowPlayingPlugin(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
        this.domain = pluginInfo.getDomain();
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);
        setObjectGraph(graph.plus(new NowPlayingModule(pluginInfo)));
        registerCommand(NowPlayingCommand.class, NowPlayingCommand.INFO);
        nowplayingmanager = getObjectGraph().get(NowPlayingManager.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        nowplayingmanager.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        nowplayingmanager.onUnload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final ConfigPanel configPanel = UIUtilities.invokeAndWait(
                new Callable<ConfigPanel>() {
                    @Override
                    public ConfigPanel call() {
                        return new ConfigPanel(nowplayingmanager, manager.getConfigManager(),
                                manager.getIdentity(), domain,
                                nowplayingmanager.getSettings());
                    }
                });

        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Now Playing",
                "", "category-nowplaying", configPanel);
        manager.getCategory("Plugins").addSubCategory(category);
    }

}

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

package com.dmdirc.addons.notifications;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import java.util.concurrent.Callable;

import dagger.ObjectGraph;

/**
 * Notification Manager plugin, aggregates notification sources exposing them via a single command.
 */
public class NotificationsPlugin extends BaseCommandPlugin {

    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** Notifications manager. */
    private NotificationsManager manager;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public NotificationsPlugin(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;

    }

    @Override
    public void load(PluginInfo pluginInfo, ObjectGraph graph) {
        super.load(pluginInfo, graph);
        setObjectGraph(graph.plus(new NotificationsModule(pluginInfo)));
        registerCommand(NotificationCommand.class, NotificationCommand.INFO);
        manager = getObjectGraph().get(NotificationsManager.class);
    }

    @Override
    public void onLoad() {
        manager.onLoad();
        super.onLoad();
    }

    @Override
    public void onUnload() {
        manager.onUnload();
        super.onUnload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final NotificationConfig configPanel = UIUtilities.invokeAndWait(
                new Callable<NotificationConfig>() {
                    /** {@inheritDoc} */
                    @Override
                    public NotificationConfig call() {
                        return new NotificationConfig(manager.getIdentity(), pluginInfo.getDomain(),
                                manager.getConfigManager().getOptionList(pluginInfo.getDomain(),
                                        "methodOrder"));
                    }
                });

        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Notifications", "", "category-notifications",
                configPanel);
        manager.getCategory("Plugins").addSubCategory(category);
    }

}

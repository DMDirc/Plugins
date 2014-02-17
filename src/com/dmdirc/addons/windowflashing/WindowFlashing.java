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

package com.dmdirc.addons.windowflashing;

import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import dagger.ObjectGraph;

/**
 * Native notification plugin to make DMDirc support windows task bar flashing.
 */
public class WindowFlashing extends BaseCommandPlugin {

    /** Window flashing manager. */
    private WindowFlashingManager manager;
    /** This plugin's plugin info. */
    private PluginInfo pluginInfo;

    public WindowFlashing(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);
        this.pluginInfo = pluginInfo;

        setObjectGraph(graph.plus(new WindowFlashingModule()));
        registerCommand(FlashWindow.class, FlashWindow.INFO);
        manager = getObjectGraph().get(WindowFlashingManager.class);
    }

    /**
     * Flashes an inactive window under windows, used as a showNotifications
     * exported command
     *
     * @param title Unused
     * @param message Unused
     */
    @Exported
    public void flashNotification(final String title, final String message) {
        manager.flashWindow();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        manager.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        manager.onUnload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Window Flashing",
                "General configuration for window flashing plugin.");

        category.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALINTEGER, pluginInfo.getDomain(), "blinkrate",
                "Blink rate", "Specifies the rate at which the taskbar and or "
                + "caption will blink, if unspecified this will be your cursor "
                + "blink rate.",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALINTEGER, pluginInfo.getDomain(), "flashcount",
                "Flash count", "Specifies the number of times to blink, if "
                + "unspecified this will blink indefinitely",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "flashtaskbar", "Flash taskbar",
                "Should the taskbar entry flash?",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "flashcaption", "Flash caption",
                "Should the window caption flash?",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(category);
    }
}

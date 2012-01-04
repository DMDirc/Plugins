/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

package com.dmdirc.addons.nma;

import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.PluginInfo;

/**
 * Plugin to allow pushing notifications to NotifyMyAndroid.
 */
public class NotifyMyAndroidPlugin extends BasePlugin {

    /** The command to register. */
    private final NotifyMyAndroidCommand command = new NotifyMyAndroidCommand();

    /** Our info object. */
    private final PluginInfo pluginInfo;

    /**
     * Creates a new instance of the {@link NotifyMyAndroidPlugin}.
     *
     * @param pluginInfo The plugin info object for this plugin.
     */
    public NotifyMyAndroidPlugin(final PluginInfo pluginInfo) {
        registerCommand(command, NotifyMyAndroidCommand.INFO);

        this.pluginInfo = pluginInfo;
    }

    /** {@inheritDoc} */
    @Override
    public void setDomain(final String newDomain) {
        super.setDomain(newDomain);
        command.setConfigDomain(newDomain);
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Notify My Android",
                "General configuration for Notify My Android plugin.");

        category.addSetting(new PreferencesSetting(
                PreferencesType.TEXT, getDomain(), "apikey",
                "API Key", "Comma-separated list of NotifyMyAndroid API keys"
                + " to be notified when the command is used.",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(
                PreferencesType.TEXT, getDomain(), "application",
                "Application", "Name of the application to report to "
                + "NotifyMyAndroid",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(category);
    }

}

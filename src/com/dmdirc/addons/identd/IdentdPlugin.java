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

import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BasePlugin;
import com.dmdirc.util.validators.PortValidator;

import dagger.ObjectGraph;

/**
 * The Identd plugin answers ident requests from IRC servers.
 */
public class IdentdPlugin extends BasePlugin {

    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** This plugin's settings domain. */
    private final String domain;
    /** Identd Manager. */
    private IdentdManager identdManager;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public IdentdPlugin(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
        domain = pluginInfo.getDomain();
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);
        setObjectGraph(graph.plus(new IdentModule(pluginInfo)));
        identdManager = getObjectGraph().get(IdentdManager.class);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        identdManager.onUnload();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        identdManager.onLoad();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Identd",
                "General Identd Plugin config ('Lower' options take priority "
                + "over those above them)");
        final PreferencesCategory advanced = new PluginPreferencesCategory(
                pluginInfo, "Advanced",
                "Advanced Identd Plugin config - Only edit these if you need "
                + "to/know what you are doing. Editing these could prevent "
                + "access to some servers. ('Lower' options take priority over "
                + "those above them)");

        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "general.useUsername", "Use connection "
                + "username rather than system username", "If this is enabled,"
                + " the username for the connection will be used rather than " + "'" + System.
                getProperty("user.name") + "'",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "general.useNickname", "Use connection "
                + "nickname rather than system username", "If this is enabled, "
                + "the nickname for the connection will be used rather than " + "'" + System.
                getProperty("user.name") + "'",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "general.useCustomName", "Use custom name" + " all the time",
                "If this is enabled, the name specified below" + " will be used all the time",
                manager.getConfigManager(),
                manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                domain, "general.customName", "Custom Name to use",
                "The custom name to use when 'Use Custom Name' is enabled",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "advanced.alwaysOn", "Always have ident " + "port open",
                "By default the identd only runs when there are "
                + "active connection attempts. This overrides that.",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                new PortValidator(), domain, "advanced.port",
                "What port should the identd listen on", "Default port is 113,"
                + " this is probably useless if changed unless you port forward"
                + " ident to a different port", manager.getConfigManager(),
                manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "advanced.useCustomSystem", "Use custom OS",
                "By default the plugin uses 'UNIX' or 'WIN32' as the system "
                + "type, this can be overriden by enabling this.",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                domain, "advanced.customSystem", "Custom OS to use",
                "The custom system to use when 'Use Custom System' is enabled",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "advanced.isHiddenUser", "Respond to ident"
                + " requests with HIDDEN-USER error", "By default the plugin will"
                + " give a USERID response, this can force an 'ERROR :"
                + " HIDDEN-USER' response instead.", manager.getConfigManager(),
                manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "advanced.isNoUser", "Respond to ident"
                + " requests with NO-USER error", "By default the plugin will"
                + " give a USERID response, this can force an 'ERROR : NO-USER'"
                + " response instead. (Overrides HIDDEN-USER)",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(general);
        general.addSubCategory(advanced);
    }

}

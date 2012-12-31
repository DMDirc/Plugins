/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.util.validators.PortValidator;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * The Identd plugin answers ident requests from IRC servers.
 */
public class IdentdPlugin extends BasePlugin implements ActionListener {

    /** Array list to store all the servers in that need ident replies. */
    private final List<Server> servers = new ArrayList<Server>();
    /** The IdentdServer that we use. */
    private IdentdServer myServer;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** The action controller to use. */
    private final ActionController actionController;
    /** Server manager. */
    private final ServerManager serverManager;
    /** Global config. */
    @Getter
    private ConfigManager config;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     * @param actionController The action controller to register listeners with
     * @param identityManager Identity manager to get settings from
     * @param serverManager Server manager to retrieve servers from
     */
    public IdentdPlugin(final PluginInfo pluginInfo,
            final ActionController actionController,
            final IdentityManager identityManager,
            final ServerManager serverManager) {
        super();

        this.pluginInfo = pluginInfo;
        this.actionController = actionController;
        this.serverManager = serverManager;
        config = identityManager.getGlobalConfiguration();
    }

    /**
     * Called when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        // Add action hooks
        actionController.registerListener(this,
                CoreActionType.SERVER_CONNECTED,
                CoreActionType.SERVER_CONNECTING,
                CoreActionType.SERVER_CONNECTERROR);

        myServer = new IdentdServer(this, serverManager);
        if (config.getOptionBool(getDomain(), "advanced.alwaysOn")) {
            myServer.startServer();
        }
    }

    /**
     * Called when this plugin is unloaded.
     */
    @Override
    public void onUnload() {
        myServer.stopServer();
        servers.clear();
        actionController.unregisterListener(this);
    }

    /**
     * Process an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.SERVER_CONNECTING) {
            synchronized (servers) {
                if (servers.isEmpty()) {
                    myServer.startServer();
                }
                servers.add((Server) arguments[0]);
            }
        } else if (type == CoreActionType.SERVER_CONNECTED
                || type == CoreActionType.SERVER_CONNECTERROR) {
            synchronized (servers) {
                servers.remove(arguments[0]);

                if (servers.isEmpty() && !config.getOptionBool(getDomain(),
                        "advanced.alwaysOn")) {
                    myServer.stopServer();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Identd",
                "General Identd Plugin config ('Lower' options take priority " +
                "over those above them)");
        final PreferencesCategory advanced = new PluginPreferencesCategory(
                pluginInfo, "Advanced",
                "Advanced Identd Plugin config - Only edit these if you need " +
                "to/know what you are doing. Editing these could prevent " +
                "access to some servers. ('Lower' options take priority over " +
                "those above them)");

        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "general.useUsername", "Use connection " +
                "username rather than system username", "If this is enabled," +
                " the username for the connection will be used rather than " +
                "'" + System.getProperty("user.name") + "'",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "general.useNickname", "Use connection " +
                "nickname rather than system username", "If this is enabled, " +
                "the nickname for the connection will be used rather than " +
                "'" + System.getProperty("user.name") + "'",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "general.useCustomName", "Use custom name" +
                " all the time", "If this is enabled, the name specified below" +
                " will be used all the time", manager.getConfigManager(),
                manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                getDomain(), "general.customName", "Custom Name to use",
                "The custom name to use when 'Use Custom Name' is enabled",
                manager.getConfigManager(), manager.getIdentity()));

        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "advanced.alwaysOn", "Always have ident " +
                "port open", "By default the identd only runs when there are " +
                "active connection attempts. This overrides that.",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                new PortValidator(), getDomain(), "advanced.port",
                "What port should the identd listen on", "Default port is 113," +
                " this is probably useless if changed unless you port forward" +
                " ident to a different port", manager.getConfigManager(),
                manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "advanced.useCustomSystem", "Use custom OS",
                "By default the plugin uses 'UNIX' or 'WIN32' as the system " +
                "type, this can be overriden by enabling this.",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                getDomain(), "advanced.customSystem", "Custom OS to use",
                "The custom system to use when 'Use Custom System' is enabled",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "advanced.isHiddenUser", "Respond to ident" +
                " requests with HIDDEN-USER error", "By default the plugin will" +
                " give a USERID response, this can force an 'ERROR :" +
                " HIDDEN-USER' response instead.", manager.getConfigManager(),
                manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "advanced.isNoUser", "Respond to ident" +
                " requests with NO-USER error", "By default the plugin will" +
                " give a USERID response, this can force an 'ERROR : NO-USER'" +
                " response instead. (Overrides HIDDEN-USER)",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(general);
        general.addSubCategory(advanced);
    }

}

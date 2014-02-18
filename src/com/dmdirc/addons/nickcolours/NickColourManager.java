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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.Channel;
import com.dmdirc.ChannelClientProperty;
import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides various features related to nickname colouring.
 */
@Singleton
public class NickColourManager implements ActionListener, ConfigChangeListener {

    /** "Random" colours to use to colour nicknames. */
    private String[] randColours = new String[]{
        "E90E7F", "8E55E9", "B30E0E", "18B33C", "58ADB3", "9E54B3", "B39875", "3176B3",};
    private boolean useowncolour;
    private String owncolour;
    private boolean userandomcolour;
    private boolean settext;
    private boolean setnicklist;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** Manager to parse colours with. */
    private final ColourManager colourManager;
    /** Main frame to parent dialogs on. */
    private final MainFrame mainFrame;
    /** Icon manager to retrieve icons from. */
    private final IconManager iconManager;
    /** Config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Config to write settings to. */
    private final ConfigProvider userConfig;
    /** Plugin's setting domain. */
    private final String domain;

    @Inject
    public NickColourManager(final PluginInfo pluginInfo,
            @NickColourModule.NickColourSettingsDomain final String domain,
            final ColourManager colourManager, final MainFrame mainFrame,
            @GlobalConfig final IconManager iconManager,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @UserConfig final ConfigProvider userConfig) {
        this.mainFrame = mainFrame;
        this.domain = domain;
        this.iconManager = iconManager;
        this.pluginInfo = pluginInfo;
        this.globalConfig = globalConfig;
        this.userConfig = userConfig;
        this.colourManager = colourManager;
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type.equals(CoreActionType.CHANNEL_GOTNAMES)) {
            final ChannelInfo chanInfo = ((Channel) arguments[0]).getChannelInfo();
            final String network = ((Channel) arguments[0]).getConnection().getNetwork();

            for (ChannelClientInfo client : chanInfo.getChannelClients()) {
                colourClient(network, client);
            }
        } else if (type.equals(CoreActionType.CHANNEL_JOIN)) {
            final String network = ((Channel) arguments[0]).getConnection().getNetwork();

            colourClient(network, (ChannelClientInfo) arguments[1]);
        }
    }

    /**
     * Colours the specified client according to the user's config.
     *
     * @param network The network to use for the colouring
     * @param client  The client to be coloured
     */
    private void colourClient(final String network,
            final ChannelClientInfo client) {
        final Map<Object, Object> map = client.getMap();
        final ClientInfo myself = client.getClient().getParser().getLocalClient();
        final String nickOption1 = "color:"
                + client.getClient().getParser().getStringConverter().
                toLowerCase(network + ":" + client.getClient().getNickname());
        final String nickOption2 = "color:"
                + client.getClient().getParser().getStringConverter().
                toLowerCase("*:" + client.getClient().getNickname());

        if (useowncolour && client.getClient().equals(myself)) {
            final Colour color = colourManager.getColourFromString(owncolour, null);
            putColour(map, color, color);
        } else if (userandomcolour) {
            putColour(map, getColour(client.getClient().getNickname()), getColour(client.
                    getClient().getNickname()));
        }

        String[] parts = null;

        if (globalConfig.hasOptionString(domain, nickOption1)) {
            parts = getParts(nickOption1);
        } else if (globalConfig.hasOptionString(domain, nickOption2)) {
            parts = getParts(nickOption2);
        }

        if (parts != null) {
            Colour textColor = null;
            Colour nickColor = null;

            if (parts[0] != null) {
                textColor = colourManager.getColourFromString(parts[0], null);
            }
            if (parts[1] != null) {
                nickColor = colourManager.getColourFromString(parts[1], null);
            }

            putColour(map, textColor, nickColor);
        }
    }

    /**
     * Puts the specified colour into the given map. The keys are determined by config settings.
     *
     * @param map        The map to use
     * @param textColour Text colour to be inserted
     * @param nickColour Nick colour to be inserted
     */
    private void putColour(final Map<Object, Object> map, final Colour textColour,
            final Colour nickColour) {
        if (settext && textColour != null) {
            map.put(ChannelClientProperty.TEXT_FOREGROUND, textColour);
        }

        if (setnicklist && nickColour != null) {
            map.put(ChannelClientProperty.NICKLIST_FOREGROUND, nickColour);
        }
    }

    /**
     * Retrieves a pseudo-random colour for the specified nickname.
     *
     * @param nick The nickname of the client whose colour we're determining
     *
     * @return Colour of the specified nickname
     */
    private Colour getColour(final String nick) {
        int count = 0;

        for (int i = 0; i < nick.length(); i++) {
            count += nick.charAt(i);
        }

        count %= randColours.length;

        return colourManager.getColourFromString(randColours[count], null);
    }

    /**
     * Reads the nick colour data from the config.
     *
     * @return A multi-dimensional array of nick colour info.
     */
    public Object[][] getData() {
        final List<Object[]> data = new ArrayList<>();

        for (String key : globalConfig.getOptions(domain).keySet()) {
            if (key.startsWith("color:")) {
                final String network = key.substring(6, key.indexOf(':', 6));
                final String user = key.substring(1 + key.indexOf(':', 6));
                final String[] parts = getParts(key);

                data.add(new Object[]{network, user, parts[0], parts[1]});
            }
        }

        final Object[][] res = new Object[data.size()][4];

        int i = 0;
        for (Object[] row : data) {
            res[i] = row;

            i++;
        }

        return res;
    }

    /**
     * Retrieves the config option with the specified key, and returns an array of the colours that
     * should be used for it.
     *
     * @param key The config key to look up
     *
     * @return The colours specified by the given key
     */
    private String[] getParts(final String key) {
        String[] parts = globalConfig.getOption(domain, key).split(":");

        if (parts.length == 0) {
            parts = new String[]{null, null};
        } else if (parts.length == 1) {
            parts = new String[]{parts[0], null};
        }

        return parts;
    }

    /**
     * Loads this plugin.
     */
    public void onLoad() {
        setCachedSettings();
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.CHANNEL_GOTNAMES, CoreActionType.CHANNEL_JOIN);
    }

    /**
     * Unloads this plugin.
     */
    public void onUnload() {
        ActionManager.getActionManager().unregisterListener(this);
    }

    /**
     * Shows the prefs configuration page for this plugin.
     *
     * @param manager Prefs manager to add settings to
     */
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Nick Colours",
                "General configuration for NickColour plugin.");
        final PreferencesCategory colours = new PluginPreferencesCategory(
                pluginInfo, "Colours",
                "Set colours for specific nicknames.", UIUtilities.invokeAndWait(
                new Callable<NickColourPanel>() {
            /** {@inheritDoc} */
            @Override
            public NickColourPanel call() {
                return new NickColourPanel(mainFrame, iconManager,
                        NickColourManager.this, colourManager, userConfig);
            }
        }));

        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "Show colours in text area",
                "Colour nicknames in main text area?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "Show colours in"
                + " nick list", "Colour nicknames in channel nick lists?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "settext", "Set colours in textarea",
                "Should the plugin set the textarea colour of nicks?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "setnicklist", "Set colours in nick list",
                "Should the plugin set the nick list colour of nicks?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "userandomcolour", "Use random colour",
                "Use a pseudo-random colour for each person?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "useowncolour", "Use colour for own nick",
                "Always use the same colour for our own nickname?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.COLOUR, domain,
                "owncolour", "Colour to use for own nick",
                "Colour used for our own nickname, if above setting is "
                + "enabled.", manager.getConfigManager(), manager.getIdentity()));

        general.addSubCategory(colours);
        manager.getCategory("Plugins").addSubCategory(general);
    }

    /**
     * Updates cached settings.
     */
    private void setCachedSettings() {
        useowncolour = globalConfig.getOptionBool(domain, "useowncolour");
        owncolour = globalConfig.getOption(domain, "owncolour");
        userandomcolour = globalConfig.getOptionBool(domain, "userandomcolour");
        settext = globalConfig.getOptionBool(domain, "settext");
        setnicklist = globalConfig.getOptionBool(domain, "setnicklist");
        if (globalConfig.hasOptionString(domain, "randomcolours")) {
            randColours = globalConfig.getOptionList(domain, "randomcolours").toArray(new String[0]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        setCachedSettings();
    }

    /**
     * Returns this plugin's settings domain.
     *
     * @return Settings domain
     */
    public String getDomain() {
        return domain;
    }

}

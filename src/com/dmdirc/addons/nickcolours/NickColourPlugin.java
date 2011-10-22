/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.ReturnableThread;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides various features related to nickname colouring.
 */
public final class NickColourPlugin extends BasePlugin implements ActionListener,
        ConfigChangeListener {

    /** "Random" colours to use to colour nicknames. */
    private String[] randColours = new String[]{
        "E90E7F", "8E55E9", "B30E0E", "18B33C",
        "58ADB3", "9E54B3", "B39875", "3176B3", };
    private boolean useowncolour;
    private String owncolour;
    private boolean userandomcolour;
    private boolean settext;
    private boolean setnicklist;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public NickColourPlugin(final PluginInfo pluginInfo) {
        super();
        this.pluginInfo = pluginInfo;
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type.equals(CoreActionType.CHANNEL_GOTNAMES)) {
            final ChannelInfo chanInfo =
                    ((Channel) arguments[0]).getChannelInfo();
            final String network = ((Channel) arguments[0]).getServer().
                    getNetwork();

            for (ChannelClientInfo client : chanInfo.getChannelClients()) {
                colourClient(network, client);
            }
        } else if (type.equals(CoreActionType.CHANNEL_JOIN)) {
            final String network = ((Channel) arguments[0]).getServer().
                    getNetwork();

            colourClient(network, (ChannelClientInfo) arguments[1]);
        }
    }

    /**
     * Colours the specified client according to the user's config.
     *
     * @param network The network to use for the colouring
     * @param client The client to be coloured
     */
    private void colourClient(final String network,
            final ChannelClientInfo client) {
        final Map<Object, Object> map = client.getMap();
        final ClientInfo myself =
                client.getClient().getParser().getLocalClient();
        final String nickOption1 = "color:"
                + client.getClient().getParser().getStringConverter().
                toLowerCase(network + ":" + client.getClient().getNickname());
        final String nickOption2 = "color:"
                + client.getClient().getParser().getStringConverter().
                toLowerCase("*:" + client.getClient().getNickname());

        if (useowncolour && client.getClient().equals(myself)) {
            final Color color = UIUtilities.convertColour(ColourManager.parseColour(owncolour));
            putColour(map, color, color);
        } else if (userandomcolour) {
            putColour(map, getColour(client.getClient().getNickname()), getColour(client.
                    getClient().getNickname()));
        }

        String[] parts = null;

        if (IdentityManager.getGlobalConfig().hasOptionString(getDomain(),
                nickOption1)) {
            parts = getParts(nickOption1);
        } else if (IdentityManager.getGlobalConfig().hasOptionString(
                getDomain(), nickOption2)) {
            parts = getParts(nickOption2);
        }

        if (parts != null) {
            Color textColor = null;
            Color nickColor = null;

            if (parts[0] != null) {
                textColor = UIUtilities.convertColour(ColourManager.parseColour(parts[0], null));
            }
            if (parts[1] != null) {
                nickColor = UIUtilities.convertColour(ColourManager.parseColour(parts[1], null));
            }

            putColour(map, textColor, nickColor);
        }
    }

    /**
     * Puts the specified colour into the given map. The keys are determined
     * by config settings.
     *
     * @param map The map to use
     * @param textColour Text colour to be inserted
     * @param nickColour Nick colour to be inserted
     */
    private void putColour(final Map<Object, Object> map, final Color textColour,
            final Color nickColour) {
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
     * @return Colour of the specified nickname
     */
    private Color getColour(final String nick) {
        int count = 0;

        for (int i = 0; i < nick.length(); i++) {
            count += nick.charAt(i);
        }

        count %= randColours.length;

        return UIUtilities.convertColour(ColourManager.parseColour(randColours[count]));
    }

    /**
     * Reads the nick colour data from the config.
     *
     * @return A multi-dimensional array of nick colour info.
     */
    public Object[][] getData() {
        final List<Object[]> data = new ArrayList<Object[]>();

        for (String key : IdentityManager.getGlobalConfig().getOptions(
                getDomain()).keySet()) {
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
     * Retrieves the config option with the specified key, and returns an
     * array of the colours that should be used for it.
     *
     * @param key The config key to look up
     * @return The colours specified by the given key
     */
    private String[] getParts(final String key) {
        String[] parts = IdentityManager.getGlobalConfig().getOption(
                getDomain(), key).split(":");

        if (parts.length == 0) {
            parts = new String[]{null, null};
        } else if (parts.length == 1) {
            parts = new String[]{parts[0], null};
        }

        return parts;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        setCachedSettings();
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.CHANNEL_GOTNAMES, CoreActionType.CHANNEL_JOIN);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        ActionManager.getActionManager().unregisterListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Nick Colours",
                "General configuration for NickColour plugin.");
        final PreferencesCategory colours = new PluginPreferencesCategory(
                pluginInfo, "Colours",
                "Set colours for specific nicknames.", UIUtilities.invokeAndWait(
                new ReturnableThread<NickColourPanel>() {

                    /** {@inheritDoc} */
                    @Override
                    public void run() {
                        setObject(new NickColourPanel(
                                (SwingController) PluginManager.getPluginManager()
                                .getPluginInfoByName("ui_swing").getPlugin(),
                                NickColourPlugin.this));
                    }
                }));

        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "Show colours in text area",
                "Colour nicknames in main text area?"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "Show colours in"
                + " nick list", "Colour nicknames in channel nick lists?"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "settext", "Set colours in textarea",
                "Should the plugin set the textarea colour of nicks?"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "setnicklist", "Set colours in nick list",
                "Should the plugin set the nick list colour of nicks?"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "userandomcolour", "Use random colour",
                "Use a pseudo-random colour for each person?"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "useowncolour", "Use colour for own nick",
                "Always use the same colour for our own nickname?"));
        general.addSetting(
                new PreferencesSetting(PreferencesType.COLOUR, getDomain(),
                "owncolour", "Colour to use for own nick",
                "Colour used for our own nickname, if above setting is "
                + "enabled."));

        general.addSubCategory(colours);
        manager.getCategory("Plugins").addSubCategory(general);
    }

    /**
     * Updates cached settings.
     */
    private void setCachedSettings() {
        useowncolour = IdentityManager.getGlobalConfig().getOptionBool(
                getDomain(), "useowncolour");
        owncolour = IdentityManager.getGlobalConfig().getOption(getDomain(),
                "owncolour");
        userandomcolour = IdentityManager.getGlobalConfig().getOptionBool(
                getDomain(), "userandomcolour");
        settext = IdentityManager.getGlobalConfig().getOptionBool(getDomain(),
                "settext");
        setnicklist = IdentityManager.getGlobalConfig().getOptionBool(
                getDomain(), "setnicklist");
        if (IdentityManager.getGlobalConfig().hasOptionString(getDomain(),
                "randomcolours")) {
            randColours = IdentityManager.getGlobalConfig().getOptionList(
                    getDomain(), "randomcolours").toArray(new String[0]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        setCachedSettings();
    }
}

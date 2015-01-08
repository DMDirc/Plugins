/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.events.ChannelGotNamesEvent;
import com.dmdirc.events.ChannelJoinEvent;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
import com.dmdirc.parser.interfaces.StringConverter;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.colours.Colour;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import net.engio.mbassy.listener.Handler;

/**
 * Provides various features related to nickname colouring.
 */
@Singleton
public class NickColourManager {

    /** Manager to parse colours with. */
    private final ColourManager colourManager;
    /** Config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Config binder. */
    private final ConfigBinder configBinder;
    /** Plugin's setting domain. */
    private final String domain;
    private final IconManager iconManager;
    private final Provider<Window> mainWindowProvider;
    /** Event bus to subscribe to events on . */
    private final DMDircMBassador eventBus;
    /** The plugin's info. */
    // TODO: Should probably make this final and introduce a factory to create NickColourManagers.
    private PluginInfo pluginInfo;
    /** "Random" colours to use to colour nicknames. */
    private String[] randColours = {
        "E90E7F", "8E55E9", "B30E0E", "18B33C", "58ADB3", "9E54B3", "B39875", "3176B3",};
    private boolean useowncolour;
    private String owncolour;
    private boolean userandomcolour;

    @Inject
    public NickColourManager(
            @GlobalConfig final ColourManager colourManager,
            @PluginDomain(NickColourPlugin.class) final String domain,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final IconManager iconManager,
            @MainWindow final Provider<Window> mainWindowProvider,
            final DMDircMBassador eventBus) {
        this.domain = domain;
        this.globalConfig = globalConfig;
        this.colourManager = colourManager;
        this.iconManager = iconManager;
        this.mainWindowProvider = mainWindowProvider;
        this.eventBus = eventBus;
        configBinder = globalConfig.getBinder().withDefaultDomain(domain);
    }

    @Handler
    public void handleChannelNames(final ChannelGotNamesEvent event) {
        final String network = event.getChannel().getConnection().get().getNetwork();
        event.getChannel().getUsers().forEach(client -> colourClient(network, client));
    }

    @Handler
    public void handleChannelJoin(final ChannelJoinEvent event) {
        final String network = event.getChannel().getConnection().get().getNetwork();
        colourClient(network, event.getClient());
    }

    /**
     * Colours the specified client according to the user's config.
     *
     * @param network The network to use for the colouring
     * @param client  The client to be coloured
     */
    private void colourClient(final String network, final GroupChatUser client) {
        final StringConverter sc = client.getUser().getConnection().getParser().get()
                .getStringConverter();
        final User myself = client.getUser();
        final String nickOption1 = "color:" + sc.toLowerCase(network + ':' + client.getNickname());
        final String nickOption2 = "color:" + sc.toLowerCase("*:" + client.getNickname());

        if (useowncolour && client.getUser().equals(myself)) {
            final Colour color = colourManager.getColourFromString(owncolour, null);
            putColour(client, color);
        } else if (userandomcolour) {
            putColour(client, getColour(client.getNickname()));
        }

        String[] parts = null;

        if (globalConfig.hasOptionString(domain, nickOption1)) {
            parts = getParts(globalConfig, domain, nickOption1);
        } else if (globalConfig.hasOptionString(domain, nickOption2)) {
            parts = getParts(globalConfig, domain, nickOption2);
        }

        if (parts != null) {
            Colour textColor = null;

            if (parts[0] != null) {
                textColor = colourManager.getColourFromString(parts[0], null);
            }

            putColour(client, textColor);
        }
    }

    /**
     * Puts the specified colour into the given map. The keys are determined by config settings.
     *
     * @param user       The map to colour
     * @param colour     Text colour to be inserted
     */
    private void putColour(final GroupChatUser user, final Colour colour) {
        user.setDisplayProperty(DisplayProperty.FOREGROUND_COLOUR, colour);
    }

    /**
     * Retrieves a pseudo-random colour for the specified nickname.
     *
     * @param nick The nickname of the client whose colour we're determining
     *
     * @return Colour of the specified nickname
     */
    private Colour getColour(final CharSequence nick) {
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
     * @param config Config to read settings from
     * @param domain Config domain
     *
     * @return A multi-dimensional array of nick colour info.
     */
    public static Object[][] getData(final ReadOnlyConfigProvider config, final String domain) {
        final Collection<Object[]> data = new ArrayList<>();

        config.getOptions(domain).keySet().stream().filter(key -> key.startsWith("color:"))
                .forEach(key -> {
                    final String network = key.substring(6, key.indexOf(':', 6));
                    final String user = key.substring(1 + key.indexOf(':', 6));
                    final String[] parts = getParts(config, domain, key);

                    data.add(new Object[]{network, user, parts[0], parts[1]});
                });

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
     * @param config Config to read settings from
     * @param domain Config domain
     * @param key    The config key to look up
     *
     * @return The colours specified by the given key
     */
    private static String[] getParts(final ReadOnlyConfigProvider config, final String domain,
            final String key) {
        String[] parts = config.getOption(domain, key).split(":");

        if (parts.length == 0) {
            parts = new String[]{null, null};
        } else if (parts.length == 1) {
            parts = new String[]{parts[0], null};
        } else if (parts.length == 2) {
            parts = new String[]{parts[0], parts[1]};
        }

        return parts;
    }

    /**
     * Loads this plugin.
     */
    public void onLoad() {
        eventBus.subscribe(this);
        configBinder.bind(this, NickColourManager.class);
    }

    /**
     * Unloads this plugin.
     */
    public void onUnload() {
        eventBus.unsubscribe(this);
        configBinder.unbind(this);
    }

    @ConfigBinding(key = "useowncolour", invocation = EDTInvocation.class)
    public void handleUseOwnColour(final boolean value) {
        useowncolour = value;
    }

    @ConfigBinding(key = "userandomcolour", invocation = EDTInvocation.class)
    public void handleUseRandomColour(final boolean value) {
        userandomcolour = value;
    }

    @ConfigBinding(key = "owncolour", invocation = EDTInvocation.class)
    public void handleOwnColour(final String value) {
        owncolour = value;
    }

    @ConfigBinding(key = "randomcolours", invocation = EDTInvocation.class)
    public void handleRandomColours(final List<String> value) {
        randColours = value.toArray(new String[value.size()]);
    }

    public void setPluginInfo(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Handler
    public void handlePrefsOpened(final ClientPrefsOpenedEvent event) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Nick Colours",
                "General configuration for NickColour plugin.");
        final PreferencesCategory colours = new PluginPreferencesCategory(
                pluginInfo, "Colours",
                "Set colours for specific nicknames.", UIUtilities.invokeAndWait(
                () -> new NickColourPanel(mainWindowProvider.get(), iconManager, colourManager,
                        event.getModel().getIdentity(), event.getModel().getConfigManager(),
                        pluginInfo.getDomain()
                )));

        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "userandomcolour", "Use random colour",
                "Use a pseudo-random colour for each person?",
                event.getModel().getConfigManager(), event.getModel().getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "useowncolour", "Use colour for own nick",
                "Always use the same colour for our own nickname?",
                event.getModel().getConfigManager(), event.getModel().getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.COLOUR, pluginInfo.getDomain(),
                "owncolour", "Colour to use for own nick",
                "Colour used for our own nickname, if above setting is enabled.",
                event.getModel().getConfigManager(), event.getModel().getIdentity()));

        general.addSubCategory(colours);
        event.getModel().getCategory("Plugins").addSubCategory(general);
    }

}

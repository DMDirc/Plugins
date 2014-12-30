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

import com.dmdirc.ChannelClientProperty;
import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ChannelGotnamesEvent;
import com.dmdirc.events.ChannelJoinEvent;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ReadOnlyConfigProvider;
import com.dmdirc.parser.interfaces.StringConverter;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.colours.Colour;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import autovalue.shaded.com.google.common.common.collect.Maps;
import net.engio.mbassy.listener.Handler;

/**
 * Provides various features related to nickname colouring.
 */
@Singleton
public class NickColourManager implements ConfigChangeListener {

    /** Manager to parse colours with. */
    private final ColourManager colourManager;
    /** Config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Plugin's setting domain. */
    private final String domain;
    /** Event bus to subscribe to events on . */
    private final DMDircMBassador eventBus;
    /** "Random" colours to use to colour nicknames. */
    private String[] randColours = {
        "E90E7F", "8E55E9", "B30E0E", "18B33C", "58ADB3", "9E54B3", "B39875", "3176B3",};
    private boolean useowncolour;
    private String owncolour;
    private boolean userandomcolour;
    private boolean settext;
    private boolean setnicklist;

    @Inject
    public NickColourManager(@GlobalConfig final ColourManager colourManager,
            @PluginDomain(NickColourPlugin.class) final String domain,
            @GlobalConfig final AggregateConfigProvider globalConfig, final DMDircMBassador eventBus) {
        this.domain = domain;
        this.globalConfig = globalConfig;
        this.colourManager = colourManager;
        this.eventBus = eventBus;
    }

    @Handler
    public void handleChannelNames(final ChannelGotnamesEvent event) {
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
        // TODO: This needs to use the new setDisplayableProperty on GroupChatUser
        final Map<Object, Object> map = Maps.newHashMap();
        final User myself = client.getUser();
        final String nickOption1 = "color:" + sc.toLowerCase(network + ':' + client.getNickname());
        final String nickOption2 = "color:" + sc.toLowerCase("*:" + client.getNickname());

        if (useowncolour && client.getUser().equals(myself)) {
            final Colour color = colourManager.getColourFromString(owncolour, null);
            putColour(map, color, color);
        } else if (userandomcolour) {
            putColour(map, getColour(client.getNickname()), getColour(client.getNickname()));
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
            Colour nickColor = null;
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
        }

        return parts;
    }

    /**
     * Loads this plugin.
     */
    public void onLoad() {
        setCachedSettings();
        eventBus.subscribe(this);
    }

    /**
     * Unloads this plugin.
     */
    public void onUnload() {
        eventBus.unsubscribe(this);
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
            final List<String> list = globalConfig.getOptionList(domain, "randomcolours");
            randColours = list.toArray(new String[list.size()]);
        }
    }

    @Override
    public void configChanged(final String domain, final String key) {
        setCachedSettings();
    }

}

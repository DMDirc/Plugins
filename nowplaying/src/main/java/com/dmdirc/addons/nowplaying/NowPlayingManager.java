/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.nowplaying;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.PluginLoadedEvent;
import com.dmdirc.events.PluginUnloadedEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import net.engio.mbassy.listener.Handler;

public class NowPlayingManager {

    /** Plugin manager to get plugins from. */
    private final PluginManager pluginManager;
    /** Global configuration to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Event bus to subscribe to events on. */
    private final EventBus eventBus;
    private final PluginInfo pluginInfo;
    /** This plugin's settings domain. */
    private final String domain;
    /** The sources that we know of. */
    private final List<MediaSource> sources = new ArrayList<>();
    /** The managers that we know of. */
    private final Collection<MediaSourceManager> managers = new ArrayList<>();
    /** The user's preferred order for source usage. */
    private List<String> order;

    @Inject
    public NowPlayingManager(final PluginManager pluginManager, final EventBus eventBus,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(NowPlayingPlugin.class) final String domain,
            @PluginDomain(NowPlayingPlugin.class) final PluginInfo pluginInfo) {
        this.pluginManager = pluginManager;
        this.globalConfig = globalConfig;
        this.domain = domain;
        this.eventBus = eventBus;
        this.pluginInfo = pluginInfo;
    }

    /**
     * Loads the plugin.
     */
    public void onLoad() {
        sources.clear();
        managers.clear();
        order = getSettings();
        eventBus.subscribe(this);
        pluginManager.getPluginInfos().stream()
                .filter(PluginInfo::isLoaded)
                .forEach(this::addPlugin);
    }

    /**
     * Unloads the plugin.
     */
    public void onUnload() {
        sources.clear();
        managers.clear();
        eventBus.unsubscribe(this);
    }

    @Handler
    public void handlePluginLoaded(final PluginLoadedEvent event) {
        addPlugin(event.getPlugin());
    }

    @Handler
    public void handlePluginUnloaded(final PluginUnloadedEvent event) {
        removePlugin(event.getPlugin());
    }

    /**
     * Loads the plugins settings.
     *
     * @return A list of sources to be used by the plugin.
     */
    public List<String> getSettings() {
        if (globalConfig.hasOptionString(domain, "sourceOrder")) {
            return globalConfig.getOptionList(domain, "sourceOrder");
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Checks to see if a plugin implements one of the Media Source interfaces and if it does, adds
     * the source(s) to our list.
     *
     * @param target The plugin to be tested
     */
    private void addPlugin(final PluginInfo target) {
        final Plugin targetPlugin = target.getPlugin();
        if (targetPlugin instanceof MediaSource) {
            sources.add((MediaSource) targetPlugin);
            addSourceToOrder((MediaSource) targetPlugin);
        }

        if (targetPlugin instanceof MediaSourceManager) {
            managers.add((MediaSourceManager) targetPlugin);

            if (((MediaSourceManager) targetPlugin).getSources() != null) {
                ((MediaSourceManager) targetPlugin).getSources().forEach(this::addSourceToOrder);
            }
        }
    }

    /**
     * Checks to see if the specified media source needs to be added to our order list, and adds it
     * if necessary.
     *
     * @param source The media source to be tested
     */
    private void addSourceToOrder(final MediaSource source) {
        if (!order.contains(source.getAppName())) {
            order.add(source.getAppName());
        }
    }

    /**
     * Checks to see if a plugin implements one of the Media Source interfaces and if it does,
     * removes the source(s) from our list.
     *
     * @param target The plugin to be tested
     */
    private void removePlugin(final PluginInfo target) {
        final Plugin targetPlugin = target.getPlugin();
        if (targetPlugin instanceof MediaSource) {
            sources.remove(targetPlugin);
        }

        if (targetPlugin instanceof MediaSourceManager) {
            managers.remove(targetPlugin);
        }
    }

    /**
     * Determines if there are any valid sources (paused or not).
     *
     * @return True if there are running sources, false otherwise
     */
    public boolean hasRunningSource() {
        for (final MediaSource source : getSources()) {
            if (source.getState() != MediaSourceState.CLOSED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves the "best" source to use for displaying media information. The best source is
     * defined as the earliest in the list that is running and not paused, or, if no such source
     * exists, the earliest in the list that is running and paused. If neither condition is
     * satisfied returns null.
     *
     * @return The best source to use for media info
     */
    public MediaSource getBestSource() {
        final List<MediaSource> possibleSources = getSources();

        possibleSources.sort(new MediaSourceComparator(order));

        MediaSource paused = null;
        for (final MediaSource source : possibleSources) {
            if (source.getState() != MediaSourceState.CLOSED) {
                if (source.getState() == MediaSourceState.PLAYING) {
                    return source;
                } else if (paused == null) {
                    paused = source;
                }
            }
        }

        return paused;
    }

    /**
     * Substitutes the keywords in the specified format with the values with values from the
     * specified source.
     *
     * @param format The format to be substituted
     * @param source The source whose values should be used
     *
     * @return The substituted string
     */
    public String doSubstitution(final String format, final MediaSource source) {
        final String artist = source.getArtist();
        final String title = source.getTitle();
        final String album = source.getAlbum();
        final String app = source.getAppName();
        final String bitrate = source.getBitrate();
        final String filetype = source.getFormat();
        final String length = source.getLength();
        final String time = source.getTime();
        final String state = source.getState().getNiceName();

        return format.replace("$artist", sanitise(artist))
                .replace("$title", sanitise(title))
                .replace("$album", sanitise(album))
                .replace("$app", sanitise(app))
                .replace("$bitrate", sanitise(bitrate))
                .replace("$format", sanitise(filetype))
                .replace("$length", sanitise(length))
                .replace("$state", sanitise(state))
                .replace("$time", sanitise(time));
    }

    /**
     * Sanitises the specified String so that it may be used as the replacement in a call to
     * String.replaceAll. Namely, at present, this method returns an empty String if it is passed a
     * null value; otherwise the input is returned as-is.
     *
     * @param input The string to be sanitised
     *
     * @return A sanitised version of the String
     *
     * @since 0.6.3
     */
    protected static String sanitise(final String input) {
        return input == null ? "" : input;
    }

    /**
     * Retrieves a source based on its name.
     *
     * @param name The name to search for
     *
     * @return The source with the specified name or null if none were found.
     */
    public MediaSource getSource(final String name) {
        for (final MediaSource source : getSources()) {
            if (source.getAppName().equalsIgnoreCase(name)) {
                return source;
            }
        }

        return null;
    }

    /**
     * Retrieves all the sources registered with this plugin.
     *
     * @return All known media sources
     */
    public List<MediaSource> getSources() {
        final List<MediaSource> res = new ArrayList<>(sources);

        for (MediaSourceManager manager : managers) {
            res.addAll(manager.getSources());
        }

        return res;
    }

    @Handler
    public void showConfig(final ClientPrefsOpenedEvent event) {
        final PreferencesDialogModel manager = event.getModel();
        final ConfigPanel configPanel = UIUtilities.invokeAndWait(
                () -> new ConfigPanel(this, manager.getConfigManager(),
                        manager.getIdentity(), domain, getSettings()));

        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Now Playing",
                "", "category-nowplaying", configPanel);
        manager.getCategory("Plugins").addSubCategory(category);
    }

}

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

package com.dmdirc.addons.nowplaying;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.ReturnableThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Plugin that allows users to advertise what they're currently playing or
 * listening to.
 */
public class NowPlayingPlugin extends BasePlugin implements ActionListener  {

    /** The sources that we know of. */
    private final List<MediaSource> sources = new ArrayList<MediaSource>();
    /** The managers that we know of. */
    private final List<MediaSourceManager> managers = new ArrayList<MediaSourceManager>();
    /** The user's preferred order for source usage. */
    private List<String> order;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public NowPlayingPlugin(final PluginInfo pluginInfo) {
        super();
        this.pluginInfo = pluginInfo;
        registerCommand(new NowPlayingCommand(this), NowPlayingCommand.INFO);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        sources.clear();
        managers.clear();

        loadSettings();

        ActionManager.getActionManager().registerListener(this,
                CoreActionType.PLUGIN_LOADED, CoreActionType.PLUGIN_UNLOADED);

        for (PluginInfo target : PluginManager.getPluginManager().getPluginInfos()) {
            if (target.isLoaded()) {
                addPlugin(target);
            }
        }
        super.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        sources.clear();
        managers.clear();
        ActionManager.getActionManager().unregisterListener(this);
        super.onUnload();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final ConfigPanel configPanel = UIUtilities.invokeAndWait(
                new ReturnableThread<ConfigPanel>() {

            @Override
            public void run() {
                setObject(new ConfigPanel(NowPlayingPlugin.this, order));
            }
        });

        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Now Playing",
                "", "category-nowplaying", configPanel);
        manager.getCategory("Plugins").addSubCategory(category);
    }

    /**
     * Saves the plugins settings.
     *
     * @param newOrder The new order for sources
     */
    protected void saveSettings(final List<String> newOrder) {
        order = newOrder;
        IdentityManager.getConfigIdentity().setOption(getDomain(), "sourceOrder", order);
    }

    /** Loads the plugins settings. */
    private void loadSettings() {
        if (IdentityManager.getGlobalConfig().hasOptionString(getDomain(), "sourceOrder")) {
            order = IdentityManager.getGlobalConfig().getOptionList(getDomain(), "sourceOrder");
        } else {
            order = new ArrayList<String>();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.PLUGIN_LOADED) {
            addPlugin((PluginInfo) arguments[0]);
        } else if (type == CoreActionType.PLUGIN_UNLOADED) {
            removePlugin((PluginInfo) arguments[0]);
        }
    }

    /**
     * Checks to see if a plugin implements one of the Media Source interfaces
     * and if it does, adds the source(s) to our list.
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
                for (MediaSource source : ((MediaSourceManager) targetPlugin).getSources()) {
                    addSourceToOrder(source);
                }
            }
        }
    }

    /**
     * Checks to see if the specified media source needs to be added to our
     * order list, and adds it if neccessary.
     *
     * @param source The media source to be tested
     */
    private void addSourceToOrder(final MediaSource source) {
        if (!order.contains(source.getAppName())) {
            order.add(source.getAppName());
        }
    }

    /**
     * Checks to see if a plugin implements one of the Media Source interfaces
     * and if it does, removes the source(s) from our list.
     *
     * @param target The plugin to be tested
     */
    private void removePlugin(final PluginInfo target) {
        final Plugin targetPlugin = target.getPlugin();
        if (targetPlugin instanceof MediaSource) {
            sources.remove((MediaSource) targetPlugin);
        }

        if (targetPlugin instanceof MediaSourceManager) {
            managers.remove((MediaSourceManager) targetPlugin);
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
     * Retrieves the "best" source to use for displaying media information.
     * The best source is defined as the earliest in the list that is running
     * and not paused, or, if no such source exists, the earliest in the list
     * that is running and paused. If neither condition is satisified returns
     * null.
     *
     * @return The best source to use for media info
     */
    public MediaSource getBestSource() {
        MediaSource paused = null;
        final List<MediaSource> possibleSources = getSources();

        Collections.sort(possibleSources, new MediaSourceComparator(order));

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
     * Substitutes the keywords in the specified format with the values with
     * values from the specified source.
     *
     * @param format The format to be substituted
     * @param source The source whose values should be used
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
     * Sanitises the specified String so that it may be used as the replacement
     * in a call to String.replaceAll. Namely, at present, this method returns
     * an empty String if it is passed a null value; otherwise the input is
     * returned as-is.
     *
     * @param input The string to be sanitised
     * @return A sanitised version of the String
     * @since 0.6.3
     */
    protected static String sanitise(final String input) {
        return input == null ? "" : input;
    }

    /**
     * Retrieves a source based on its name.
     *
     * @param name The name to search for
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
        final List<MediaSource> res = new ArrayList<MediaSource>(sources);

        for (MediaSourceManager manager : managers) {
            res.addAll(manager.getSources());
        }

        return res;
    }
}

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

package com.dmdirc.addons.notifications;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.events.PluginLoadedEvent;
import com.dmdirc.events.PluginUnloadedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.dmdirc.DMDircMBassador;
import net.engio.mbassy.listener.Handler;

public class NotificationsManager {

    /** The notification methods that we know of. */
    private final List<String> methods = new ArrayList<>();
    /** The user's preferred order for method usage. */
    private List<String> order;
    /** This plugin's settings domain. */
    private final String domain;
    /** Global config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Plugin manager. */
    private final PluginManager pluginManager;
    /** Event bus to listen for events on. */
    private final DMDircMBassador eventBus;

    @Inject
    public NotificationsManager(@PluginDomain(NotificationsPlugin.class) final String domain,
            @GlobalConfig final AggregateConfigProvider globalConfig, final DMDircMBassador eventBus,
            final PluginManager pluginManager) {
        this.domain = domain;
        this.globalConfig = globalConfig;
        this.pluginManager = pluginManager;
        this.eventBus = eventBus;
    }

    public void onLoad() {
        methods.clear();
        loadSettings();
        eventBus.subscribe(this);
        for (PluginInfo target : pluginManager.getPluginInfos()) {
            if (target.isLoaded()) {
                addPlugin(target);
            }
        }
    }

    public void onUnload() {
        methods.clear();
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

    /** Loads the plugins settings. */
    private void loadSettings() {
        if (globalConfig.hasOptionString(domain, "methodOrder")) {
            order = globalConfig.getOptionList(domain, "methodOrder");
        } else {
            order = new ArrayList<>();
        }
    }

    /**
     * Checks to see if a plugin implements the notification method interface and if it does, adds
     * the method to our list.
     *
     * @param target The plugin to be tested
     */
    private void addPlugin(final PluginInfo target) {
        if (target.hasExportedService("showNotification")) {
            methods.add(target.getMetaData().getName());
            addMethodToOrder(target);
        }
    }

    /**
     * Checks to see if the specified notification method needs to be added to our order list, and
     * adds it if necessary.
     *
     * @param source The notification method to be tested
     */
    private void addMethodToOrder(final PluginInfo source) {
        if (!order.contains(source.getMetaData().getName())) {
            order.add(source.getMetaData().getName());
        }
    }

    /**
     * Checks to see if a plugin implements the notification method interface and if it does,
     * removes the method from our list.
     *
     * @param target The plugin to be tested
     */
    private void removePlugin(final PluginInfo target) {
        methods.remove(target.getMetaData().getName());
    }

    /**
     * Retrieves a method based on its name.
     *
     * @param name The name to search for
     *
     * @return The method with the specified name or null if none were found.
     */
    public PluginInfo getMethod(final String name) {
        return pluginManager.getPluginInfoByName(name);
    }

    /**
     * Retrieves all the methods registered with this plugin.
     *
     * @return All known notification sources
     */
    public List<PluginInfo> getMethods() {
        final List<PluginInfo> plugins = new ArrayList<>();
        for (String method : methods) {
            plugins.add(pluginManager.getPluginInfoByName(method));
        }
        return plugins;
    }

    /**
     * Does this plugin have any active notification methods?
     *
     * @return true iif active notification methods are registered
     */
    public boolean hasActiveMethod() {
        return !methods.isEmpty();
    }

    /**
     * Returns the user's preferred method if loaded, or null if none loaded.
     *
     * @return Preferred notification method
     */
    public PluginInfo getPreferredMethod() {
        if (methods.isEmpty()) {
            return null;
        }
        for (String method : order) {
            if (methods.contains(method)) {
                return pluginManager.getPluginInfoByName(method);
            }
        }
        return null;
    }

}

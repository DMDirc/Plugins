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

package com.dmdirc.addons.notifications;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.PluginLoadedEvent;
import com.dmdirc.events.PluginUnloadedEvent;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.engio.mbassy.listener.Handler;

public class NotificationsManager {

    /** The notification handlers that we know of. */
    private final Map<String, NotificationHandler> handlers = new HashMap<>();
    /** The user's preferred order for method usage. */
    private List<String> order;
    /** This plugin's settings domain. */
    private final String domain;
    private final PluginInfo pluginInfo;
    /** Global config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Plugin manager. */
    private final PluginManager pluginManager;
    /** Event bus to listen for events on. */
    private final EventBus eventBus;

    @Inject
    public NotificationsManager(@PluginDomain(NotificationsPlugin.class) final String domain,
            @PluginDomain(NotificationsPlugin.class) final PluginInfo pluginInfo,
            @GlobalConfig final AggregateConfigProvider globalConfig, final EventBus eventBus,
            final PluginManager pluginManager) {
        this.domain = domain;
        this.pluginInfo = pluginInfo;
        this.globalConfig = globalConfig;
        this.pluginManager = pluginManager;
        this.eventBus = eventBus;
    }

    public void onLoad() {
        handlers.clear();
        loadSettings();
        eventBus.subscribe(this);
        pluginManager.getPluginInfos().stream()
                .filter(PluginInfo::isLoaded)
                .forEach(this::addPlugin);
    }

    public void onUnload() {
        handlers.clear();
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
            handlers.put(target.getMetaData().getName(), new LegacyNotificationHandler(pluginInfo));
            addHandlerToOrder(target);
        }
    }

    /**
     * Checks to see if the specified notification method needs to be added to our order list, and
     * adds it if necessary.
     *
     * @param source The notification method to be tested
     */
    private void addHandlerToOrder(final PluginInfo source) {
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
        handlers.remove(target.getMetaData().getName());
    }

    /**
     * Retrieves a handler based on its name.
     *
     * @param name The name to search for
     *
     * @return The handler with the specified name or null if none were found.
     */
    public NotificationHandler getHandler(final String name) {
        return handlers.get(name);
    }

    /**
     * Retrieves the names of all the handlers registered with this plugin.
     *
     * @return All known notification handler names
     */
    public Collection<String> getHandlerNames() {
        return handlers.keySet();
    }

    /**
     * Does this plugin have any active notification handler?
     *
     * @return true iif active notification handlers are registered
     */
    public boolean hasActiveHandler() {
        return !handlers.isEmpty();
    }

    /**
     * Returns the user's preferred handler if loaded, or null if none loaded.
     *
     * @return Preferred notification handler
     */
    public NotificationHandler getPreferredHandler() {
        if (handlers.isEmpty()) {
            return null;
        }
        for (String method : order) {
            if (handlers.containsKey(method)) {
                return handlers.get(method);
            }
        }
        return null;
    }

    @Handler
    public void showConfig(final ClientPrefsOpenedEvent event) {
        final PreferencesDialogModel manager = event.getModel();
        final NotificationConfig configPanel = UIUtilities.invokeAndWait(
                () -> new NotificationConfig(manager.getIdentity(), pluginInfo.getDomain(),
                        manager.getConfigManager()
                                .getOptionList(pluginInfo.getDomain(), "methodOrder")));

        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Notifications", "", "category-notifications",
                configPanel);
        manager.getCategory("Plugins").addSubCategory(category);
    }

}

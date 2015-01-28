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

package com.dmdirc.addons.whoisonquery;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.ConnectionPrefsRequestedEvent;
import com.dmdirc.events.QueryOpenedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

/**
 * Sends a whois when a new query is opened.
 */
public class WhoisOnQueryManager {

    private final String domain;
    private final PluginInfo pluginInfo;
    private final DMDircMBassador eventBus;

    @Inject
    public WhoisOnQueryManager(@PluginDomain(WhoisOnQueryPlugin.class) final String domain,
            @PluginDomain(WhoisOnQueryPlugin.class) final PluginInfo pluginInfo,
            final DMDircMBassador eventBus) {
        this.domain = domain;
        this.pluginInfo = pluginInfo;
        this.eventBus = eventBus;
    }

    public void load() {
        eventBus.subscribe(this);
    }

    public void unload() {
        eventBus.unsubscribe(this);
    }

    @VisibleForTesting
    @Handler
    void handleQueryEvent(final QueryOpenedEvent event) {
        event.getQuery().getConnection().ifPresent(c -> {
           final boolean enable = c.getWindowModel().getConfigManager()
                   .getOptionBool(domain, "whoisonquery");
            if (enable) {
                c.requestUserInfo(event.getQuery().getUser());
            }
        });
    }

    @VisibleForTesting
    @Handler
    void handlePrefsEvent(final ClientPrefsOpenedEvent event) {
        final PreferencesCategory category = new PluginPreferencesCategory(pluginInfo,
                "Whois on Query", "Whois on query");
        category.addSetting(getSetting(event.getModel().getConfigManager(),
                event.getModel().getIdentity()));
        event.getModel().getCategory("Plugins").addSubCategory(category);
    }

    @VisibleForTesting
    @Handler
    void handleConnectionPrefsEvent(final ConnectionPrefsRequestedEvent event) {
        event.getCategory().addSetting(getSetting(event.getConfig(), event.getIdentity()));
    }

    private PreferencesSetting getSetting(final AggregateConfigProvider config,
            final ConfigProvider configProvider) {
        return new PreferencesSetting(PreferencesType.BOOLEAN, domain, "whoisonquery",
                "Send whois on query", "Request user information when a query is opened?",
                config, configProvider);
    }
}

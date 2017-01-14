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

package com.dmdirc.addons.debug;

import com.dmdirc.config.GlobalConfig;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.events.ServerConnectingEvent;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.WindowManager;
import net.engio.mbassy.listener.Handler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

/**
 * Debug plugin manager.
 */
@Singleton
public class DebugManager {

    private final RawWindowFactory windowFactory;

    private final AggregateConfigProvider config;

    private final String domain;

    private final WindowManager windowManager;

    private final EventBus eventBus;

    @Inject
    public DebugManager(@PluginDomain(DebugPlugin.class) final String domain,
            @GlobalConfig final AggregateConfigProvider globalConfig, final RawWindowFactory windowFactory,
            final WindowManager windowManager, final EventBus eventBus) {
        this.domain = domain;
        this.windowManager = windowManager;
        this.eventBus = eventBus;
        this.config = globalConfig;
        this.windowFactory = windowFactory;
    }

    public void load() {
        eventBus.subscribe(this);
    }

    public void unload() {
        eventBus.unsubscribe(this);
    }

    @Handler
    public void handleServerConnecting(final ServerConnectingEvent event) {
        if (config.getOptionBool(domain, "showraw") && !windowManager
                .getChildren(event.getConnection().getWindowModel()).stream().anyMatch(RawWindow.class::isInstance)) {
            windowFactory.getRawWindow(event.getConnection());
        }
    }
}

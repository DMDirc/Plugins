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

package com.dmdirc.addons.channelwho;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Factory for creating {@link ConnectionHandler}s.
 */
public class ConnectionHandlerFactory {

    private final AggregateConfigProvider config;
    private final ScheduledExecutorService executorService;
    private final EventBus eventBus;
    private final String domain;

    @Inject
    public ConnectionHandlerFactory(@GlobalConfig final AggregateConfigProvider config,
            @Named("channelwho") final ScheduledExecutorService executorService,
            final EventBus eventBus,
            @PluginDomain(ChannelWhoPlugin.class) final String domain) {
        this.config = config;
        this.executorService = executorService;
        this.eventBus = eventBus;
        this.domain = domain;
    }

    public ConnectionHandler get(final Connection connection) {
        final ConnectionHandler handler = new ConnectionHandler(config, executorService,
            eventBus, domain, connection);
        handler.load();
        return handler;
    }
}

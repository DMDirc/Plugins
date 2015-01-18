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

import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.ChannelUserAwayEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.ServerNumericEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import com.google.common.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.listener.Handler;

/**
 * Responsible for managing timers and settings required to who any {@link GroupChat}s on a
 * {@link Connection} as specified by the user.
 */
public class ConnectionHandler {

    private final Map<String, GroupChatUser> users;
    private final Connection connection;
    private final String domain;
    private final ScheduledExecutorService executorService;
    private final ConnectionManager connectionManager;
    private final ConfigBinder configBinder;
    private int whoInterval;
    private ScheduledFuture<?> future;

    public ConnectionHandler(
            final AggregateConfigProvider config,
            final ScheduledExecutorService executorService,
            final ConnectionManager connectionManager, final String domain,
            final Connection connection) {
        this.connection = connection;
        this.domain = domain;
        this.executorService = executorService;
        this.connectionManager = connectionManager;
        configBinder = config.getBinder().withDefaultDomain(domain);
        users = new HashMap<>();
    }

    public void load() {
        configBinder.bind(this, ConnectionHandler.class);
        connection.getWindowModel().getEventBus().subscribe(this);
    }

    public void unload() {
        configBinder.unbind(this);
        executorService.shutdown();
        connection.getWindowModel().getEventBus().unsubscribe(this);
        if (future != null) {
            future.cancel(true);
        }
    }

    @VisibleForTesting
    void checkWho() {
        connectionManager.getConnections().forEach(connection ->
                connection.getGroupChatManager().getChannels().forEach(channel -> {
            if (channel.getWindowModel().getConfigManager().getOptionBool(domain, "sendWho")) {
                channel.requestUsersInfo();
            }
        }));
    }

    @VisibleForTesting
    @ConfigBinding(key="whoInterval")
    void handleWhoInterval(final int value) {
        whoInterval = value;
        if (future != null) {
            future.cancel(true);
        }
        future = executorService.schedule(this::checkWho, whoInterval, TimeUnit.MILLISECONDS);
    }

    @VisibleForTesting
    @Handler
    void handleAwayEvent(final ChannelUserAwayEvent event) {
        if (!event.getReason().isPresent()) {
            event.setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
            users.put(event.getUser().getNickname(), event.getUser());
            event.getChannel().getConnection()
                    .ifPresent(c -> c.requestUserInfo(event.getUser().getUser()));
        }
    }

    @VisibleForTesting
    @Handler
    void handleServerNumericEvent(final ServerNumericEvent event) {
        if (event.getNumeric() == 301) {
            final String nickname = event.getArgs()[4];
            final String reason = event.getArgs()[5];
            final GroupChatUser user = users.remove(nickname);
            if (user != null) {
                connection.getWindowModel().getEventBus().publishAsync(
                        new ChannelUserAwayEvent(user.getGroupChat(), user,
                                Optional.ofNullable(reason)));
            }
        }
    }
}

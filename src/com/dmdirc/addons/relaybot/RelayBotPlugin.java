/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.relaybot;

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.plugins.Plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This plugin makes certain relay bots less obnoxious looking.
 *
 * @author shane
 */
public class RelayBotPlugin extends Plugin implements ActionListener, ConfigChangeListener {
    /** Known RelayChannelHandlers. */
    private final Map<Channel, RelayChannelHandler> handlers = new HashMap<Channel, RelayChannelHandler>();

    /**
     * Creates a new instance of the RelayBotPlugin.
     */
    public RelayBotPlugin() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        ActionManager.addListener(this, CoreActionType.CHANNEL_OPENED);
        ActionManager.addListener(this, CoreActionType.CHANNEL_CLOSED);
        ActionManager.addListener(this, CoreActionType.SERVER_CONNECTED);
        ActionManager.addListener(this, CoreActionType.SERVER_DISCONNECTED);
        ActionManager.addListener(this, CoreActionType.CHANNEL_QUIT);

        // Add ourself to all currently known channels that we should be
        // connected with.
        for (Server server : ServerManager.getServerManager().getServers()) {
            final Parser parser = server.getParser();
            if (parser instanceof IRCParser && !(parser.getCallbackManager() instanceof RelayCallbackManager)) {
                new RelayCallbackManager(this, (IRCParser)parser);
            }
            for (String channel : server.getChannels()) {
                final Channel chan = server.getChannel(channel);
                final String channelName = chan.getServer().getParser().getStringConverter().toLowerCase(chan.getName());
                getHandler(chan);
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        ActionManager.removeListener(this);

        // Remove RelayCallbackManagers
        for (Server server : ServerManager.getServerManager().getServers()) {
            final Parser parser = server.getParser();
            if (parser instanceof IRCParser && parser.getCallbackManager() instanceof RelayCallbackManager) {
                ((RelayCallbackManager) parser.getCallbackManager()).onSocketClosed(parser, new Date());
            }
        }

        // Remove from all channels.
        synchronized (handlers) {
            for (RelayChannelHandler handler : new ArrayList<RelayChannelHandler>(handlers.values())) {
                handler.restoreCoreChannelHandler();
            }
            handlers.clear();
        }
    }

    /**
     * Process an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        if (type == CoreActionType.CHANNEL_OPENED) {
            final Channel chan = (Channel)arguments[0];
            
            final String channelName = chan.getServer().getParser().getStringConverter().toLowerCase(chan.getName());
            getHandler(chan);
        } else if (type == CoreActionType.CHANNEL_CLOSED) {
            final Channel chan = (Channel)arguments[0];
            removeHandler(chan);
        } else if (type == CoreActionType.CHANNEL_QUIT) {
            final Channel chan = (Channel)arguments[0];
            final Parser parser = chan.getServer().getParser();
            final ChannelClientInfo cci = (ChannelClientInfo)arguments[1];
            final String channelName = parser.getStringConverter().toLowerCase(chan.getName());

            if (IdentityManager.getGlobalConfig().hasOptionString(getDomain(), channelName)) {
                final String botName = IdentityManager.getGlobalConfig().getOption(getDomain(), channelName);
                if (parser.getStringConverter().equalsIgnoreCase(botName, cci.getClient().getNickname())) {
                    // The bot quit :(
                    final RelayChannelHandler handler = getHandler(chan);
                    if (handler != null) {
                        handler.updateNames();
                    }
                }
            }
        } else if (type == CoreActionType.SERVER_CONNECTED) {
            final Server server = (Server)arguments[0];

            final Parser parser = server.getParser();
            if (parser instanceof IRCParser && !(parser.getCallbackManager() instanceof RelayCallbackManager)) {
                new RelayCallbackManager(this, (IRCParser)parser);
            }
        } else if (type == CoreActionType.SERVER_DISCONNECTED) {
            final Server server = (Server)arguments[0];

            // RelayCallbackManager will revert itself when this happens.

            // Unset any listeners for channels of this server
            for (String channel : server.getChannels()) {
                final Channel chan = server.getChannel(channel);
                final String channelName = chan.getServer().getParser().getStringConverter().toLowerCase(chan.getName());
                removeHandler(chan);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (!domain.equals(getDomain())) { return; }
        final boolean wasUnset = !(IdentityManager.getGlobalConfig().hasOptionString(domain, key));

        for (Server server : ServerManager.getServerManager().getServers()) {
            if (server.hasChannel(key)) {
                final Channel chan = server.getChannel(key);
                if (wasUnset) {
                    removeHandler(chan);
                } else {
                    getHandler(chan);
                }
            }
        }
    }

    /**
     * Do we have an intercept for the given channel object?
     * If we have one, we will return true.
     * If we should have one (as determined by checking the config) we will add
     * one and return true.
     * 
     * @param channel
     * @return true or false
     */
    public boolean isListening(final Channel channel) {
        return (getHandler(channel) != null);
    }

    /**
     * Get the RelayChannelHandler for a given Channel.
     * If we have one, we will return it.
     * If we should have one (as determined by checking the config) we will
     * create and return it.
     * Otherwise we will return null.
     *
     * @param channel Channel to get Handler for.
     * @return ChannelHandler or null
     */
    public RelayChannelHandler getHandler(final Channel channel) {
        synchronized (handlers) {
            if (handlers.containsKey(channel)) {
                return handlers.get(channel);
            } else {
                final String channelName = channel.getServer().getParser().getStringConverter().toLowerCase(channel.getName());
                if (IdentityManager.getGlobalConfig().hasOptionString(getDomain(), channelName)) {
                    final RelayChannelHandler handler = new RelayChannelHandler(this, channel);
                    handlers.put(channel, handler);
                    return handler;
                }
            }
        }

        return null;
    }

    /**
     * Remove the RelayChannelHandler for a given Channel.
     * If we have one already, we will return it and remove it.
     * Otherwise we will return null.
     *
     * @param channel Channel to remove Handler for.
     * @return Handler that we removed, or null.
     */
    public RelayChannelHandler removeHandler(final Channel channel) {
        synchronized (handlers) {
            if (handlers.containsKey(channel)) {
                final RelayChannelHandler handler = handlers.get(channel);
                handler.unset();
                handlers.remove(channel);

                return handler;
            }
        }

        return null;
    }
}
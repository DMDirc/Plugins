/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.ChannelEventHandler;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
import com.dmdirc.parser.irc.IRCCallbackManager;
import com.dmdirc.parser.irc.IRCChannelClientInfo;
import com.dmdirc.parser.irc.IRCParser;
import com.dmdirc.plugins.PluginClassLoader;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.messages.Styliser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class replaces the ChannelHandler in the core to allow intercepting
 * callbacks from the parser.
 *
 * This allows us to hide them from the core and send faked ones instead.
 *
 * @author shane
 */
public class RelayChannelHandler implements ChannelMessageListener {
    /** My Channel. */
    private final Channel myChannel;

    /** Core channel handler. */
    private final ChannelEventHandler coreChannelHandler;

    /** Plugin that owns this RelayChannelHandler. */
    private final RelayBotPlugin myPlugin;

    /** Known ChannelClients */
    private final Map<String, IRCChannelClientInfo> channelClients = new HashMap<String, IRCChannelClientInfo>();

    /**
     * Create a new RelayChannelHandler.
     *
     * @param myPlugin 
     * @param myChannel channel to hax!
     */
    public RelayChannelHandler(final RelayBotPlugin myPlugin, final Channel myChannel) {
        this.myChannel = myChannel;
        this.myPlugin = myPlugin;
        ChannelEventHandler ceh = null;
        try {
            // Get the core Channel handler.
            final Field field = myChannel.getClass().getDeclaredField("eventHandler");
            field.setAccessible(true);
            ceh = (ChannelEventHandler) field.get(myChannel);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        coreChannelHandler = ceh;

        if (coreChannelHandler != null) {
            final IRCCallbackManager cbm = (IRCCallbackManager) myChannel.getServer().getParser().getCallbackManager();
            cbm.delCallback(ChannelMessageListener.class, coreChannelHandler);
            cbm.addCallback(ChannelMessageListener.class, this, myChannel.getName());
        }
    }


    /**
     * Get a ChannelClient object for the given nick@server.
     *
     * @param channel Channel
     * @param nick Nickname to get channel client for.
     * @param sendJoinIfNew
     * @return Requested ChannelClient Info.
     */
    private IRCChannelClientInfo getChannelClient(final ChannelInfo channel, final Date date, final String nick, final boolean sendJoinIfNew) {
        final Parser parser = channel.getParser();
        final String storeName = parser.getStringConverter().toLowerCase(nick);
        synchronized (channelClients) {
            if (!channelClients.containsKey(storeName)) {
                final RelayClientInfo client = new RelayClientInfo(channel.getParser(), nick);
                final IRCChannelClientInfo newChannelClient = new IRCChannelClientInfo((IRCParser)channel.getParser(), client, channel);
                colourClient(newChannelClient);

                channelClients.put(storeName, newChannelClient);
                if (sendJoinIfNew && !client.isServer()) {
                    coreChannelHandler.onChannelJoin(parser, date, channel, newChannelClient);
                    // The nickcolour plugin colours the nicknames on join
                    // and uses nickname@server when colouring rather than
                    // the setting the user wanted, we can recolour here to
                    // fix that.
                    colourClient(newChannelClient);
                }
            }

            return channelClients.get(storeName);
        }
    }

    /**
     * Remove a stored ChannelClient
     *
     * @param channel Channel
     * @param nick Nickname to get channel client for.
     */
    private void removeChannelClient(final ChannelInfo channel, final String nick) {
        final Parser parser = channel.getParser();
        final String storeName = parser.getStringConverter().toLowerCase(nick);
        synchronized (channelClients) {
            channelClients.remove(storeName);
        }
    }

    /**
     * Rename a stored ChannelClient
     *
     * @param channelClient ChannelClient
     * @param newNick new Nickname
     */
    private void renameChannelClient(final IRCChannelClientInfo channelClient, final String newNick) {
        final Parser parser = channelClient.getChannel().getParser();
        final String storeName = parser.getStringConverter().toLowerCase(newNick);

        synchronized (channelClients) {
            channelClients.remove(channelClient.getClient().toString());
            ((RelayClientInfo) channelClient.getClient()).changeNickname(newNick);
            channelClients.put(storeName,channelClient);
        }
    }

    /**
     * Restore the callback handling to the coreChannelHandler.
     */
    public void restoreCoreChannelHandler() {
        if (coreChannelHandler != null) {
            final IRCCallbackManager cbm = (IRCCallbackManager) myChannel.getServer().getParser().getCallbackManager();

            // Force adding this callback to the CBM.
            if (cbm instanceof RelayCallbackManager) {
                ((RelayCallbackManager) cbm).forceAddCallback(ChannelMessageListener.class, coreChannelHandler, myChannel.getName());
            } else {
                cbm.addCallback(ChannelMessageListener.class, coreChannelHandler, myChannel.getName());
            }
            unset();
            updateNames();
        }
    }

    /**
     * Remove channel message handling from this ChannelHandler
     */
    public void unset() {
        myChannel.getServer().getParser().getCallbackManager().delCallback(ChannelMessageListener.class, this);
    }

    /**
    * Colour a client as needed.
    *
    * @param channelClient Client to colour
    */
    private void colourClient(final ChannelClientInfo channelClient) {
        // Use nick colour plugin to colour the client if available.
        final PluginInfo nickColour = PluginManager.getPluginManager().getPluginInfoByName("nickcolour");

        final boolean fullColour = IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "colourFullName");
        final RelayClientInfo client = (RelayClientInfo)channelClient.getClient();
        final boolean oldValue = client.getShowFullNickname();
        client.setShowFullNickname(fullColour);

        if (nickColour != null && nickColour.isLoaded()) {
            try {
                final Method gpcl = PluginInfo.class.getDeclaredMethod("getPluginClassLoader");
                gpcl.setAccessible(true);
                final PluginClassLoader pcl = (PluginClassLoader) gpcl.invoke(nickColour);

                final Class<?> nc = nickColour.getPlugin().getClass();
                final Method colourClient = nc.getDeclaredMethod("colourClient", new Class<?>[]{String.class, ChannelClientInfo.class});
                colourClient.setAccessible(true);
                colourClient.invoke(nickColour.getPlugin(), myChannel.getServer().getNetwork(), channelClient);
            } catch (Throwable t) { /* If it can't colour then oh well. */ }
        }
        
        client.setShowFullNickname(oldValue);
    }


    /**
     * Handle the IRCParsers incoming message.
     *
     * @param parser Parser that sent the message
     * @param channel Channel the message went to
     * @param channelClient Client who send the message
     * @param message Message content
     * @param host Host of client
     */
    @Override
    public void onChannelMessage(final Parser parser, final Date date, final ChannelInfo channel, final ChannelClientInfo channelClient, final String message, final String host) {
        final String channelName = parser.getStringConverter().toLowerCase(channel.getName());
        String botName;
        try {
            botName = IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), channelName);
        } catch (IllegalArgumentException iae) {
            botName = "";
        }
        final boolean isBot = parser.getStringConverter().equalsIgnoreCase(botName, channelClient.getClient().getNickname());

        final boolean joinNew = IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "joinOnDiscover");

        // See if we need to modify this message
        if (channelClient instanceof IRCChannelClientInfo && !botName.isEmpty() && isBot) {
            final String[] bits = message.split(" ", 2);
            final String initial = Styliser.stipControlCodes(bits[0]);

            if (initial.charAt(0) == '+') {
                // Channel Message
                final IRCChannelClientInfo newChannelClient = getChannelClient(channel, date, bits[0].substring(2, bits[0].length() - 1), joinNew);

                coreChannelHandler.onChannelMessage(parser, date, channel, newChannelClient, bits[1], host);
                return;
            } else if (initial.equalsIgnoreCase("***")) {
                // Some kind of state-changing action
                final String[] newBits = bits[1].split(" ");

                if (newBits.length > 2) {
                    final IRCChannelClientInfo newChannelClient = getChannelClient(channel, date, newBits[0], joinNew);

                    if (newBits[2].equalsIgnoreCase("joined")) {
                        // User joined a relayed channel.
                        if (!joinNew) {
                            // If auto join on discover isn't enabled, we will
                            // need to send this join, else it will already
                            // have been sent.
                            coreChannelHandler.onChannelJoin(parser, date, channel, newChannelClient);
                            // And recolour to combat the nickcolour plugin
                            // changing the colour on join.
                            colourClient(newChannelClient);
                        }
                        return;
                    } else if (newBits[2].equalsIgnoreCase("left")) {
                        // User left a relayed channel.
                        String reason = (newBits.length > 4) ? mergeBits(newBits, 4, newBits.length - 1, " ") : "()";
                        reason = reason.substring(1, reason.length() - 1);

                        coreChannelHandler.onChannelPart(parser, date, channel, newChannelClient, reason);
                        removeChannelClient(channel, newBits[0]);
                        return;
                    } else if (newBits[2].equalsIgnoreCase("quit")) {
                        // User quit a relayed channel.
                        String reason = (newBits.length > 4) ? mergeBits(newBits, 4, newBits.length - 1, " ") : "()";
                        reason = reason.substring(1, reason.length() - 1);

                        coreChannelHandler.onChannelQuit(parser, date, channel, newChannelClient, reason);
                        removeChannelClient(channel, newBits[0]);
                        return;
                    } else if (newBits[2].equalsIgnoreCase("kicked")) {
                        // User was kicked from a relayed channel.
                        String reason = (newBits.length > 7) ? mergeBits(newBits, 7, newBits.length - 1, " ") : "()";
                        reason = reason.substring(1, reason.length() - 1);

                        final IRCChannelClientInfo kickingChannelClient = (newBits.length > 6) ? getChannelClient(channel, date, newBits[6], joinNew) : null;

                        coreChannelHandler.onChannelKick(parser, date, channel, newChannelClient, kickingChannelClient, reason, "");
                        removeChannelClient(channel, newBits[0]);
                        return;
                    } else if (newBits[2].equalsIgnoreCase("now")) {
                        // User changed their nickname in a relayed channel.
                        if (newBits.length > 3) {
                            renameChannelClient(newChannelClient, newBits[3]);

                            coreChannelHandler.onChannelNickChanged(parser, date, channel, newChannelClient, newBits[0]);
                            return;
                        }
                    }
                }
            } else if (initial.charAt(0) == '*') {
                // Channel Action
                final String[] newBits = bits[1].split(" ", 2);

                final IRCChannelClientInfo newChannelClient = getChannelClient(channel, date, newBits[0], joinNew);

                coreChannelHandler.onChannelAction(parser, date, channel, newChannelClient, newBits[1], "");
                return;
            }
        }

        // Pass it on unchanged.
        coreChannelHandler.onChannelMessage(parser, date, channel, channelClient, message, host);
    }

    /**
     * Merge the given bits.
     *and
     * @param bits Bits to merge
     * @param start Start
     * @param end end
     * @param joiner What to use to join them
     * @return Joined bits.
     */
    private String mergeBits(final String[] bits, final int start, final int end, final String joiner) {
        final StringBuilder builder = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (bits.length < i) { break; }
            if (i != start) { builder.append(joiner); }
            builder.append(bits[i]);
        }

        return builder.toString();
    }

    /**
     * Send an onChannelNames() event for this channel.
     * This will cause all remote clients to vanish from the nicklist.
     */
    public void updateNames() {
        coreChannelHandler.onChannelGotNames(myChannel.getServer().getParser(), new Date(), myChannel.getChannelInfo());
    }
}

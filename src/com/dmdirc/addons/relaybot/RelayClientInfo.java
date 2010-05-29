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

import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.irc.IRCClientInfo;
import com.dmdirc.parser.irc.IRCParser;

/**
 * This class is used to proxy an IRCClientInfo and to show the nickname
 * as nick@network.
 */
public class RelayClientInfo extends IRCClientInfo {
    /** Shoulf getNickname() return the network aswell? */
    private boolean showFullNickname = true;

    /**
     * Create a new RelayClientInfo
     *
     * @param parser Parser that owns this client
     * @param host Host of client.
     */
    public RelayClientInfo(final Parser parser, final String host) {
        super((IRCParser) parser, host);
        super.setFake(true);
    }

    /**
     * Should the full nickname (nick@server) be used by getNickname?
     *
     * @param value
     */
    public void setShowFullNickname(final boolean value) {
        showFullNickname = value;
    }

    /**
     * Will the full nickname (nick@server) be used by getNickname?
     *
     * @return true/false
     */
    public boolean getShowFullNickname() {
        return showFullNickname;
    }

    /**
     * Get the nickname of this client.
     *
     * @return The nickname of this client.
     */
    @Override
    public String getNickname() {
        return (showFullNickname) ? super.getNickname()+"@"+super.getHostname() : super.getNickname();
    }

    /**
     * Change the nickname of this client.
     *
     * @param newNick new Nickname for this client.
     */
    public void changeNickname(final String newNick) {
        setUserBits(newNick, true);
    }

    /** {@inheritDoc} */
    @Override
    public String getHostname() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsername() {
        return "";
    }
}
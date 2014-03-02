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

package com.dmdirc.addons.parser_xmpp;

import com.dmdirc.parser.common.BaseChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;

/**
 * An XMPP-specific channel client info object.
 */
public class XmppChannelClientInfo extends BaseChannelClientInfo {

    /**
     * Creates a new client info object for the specified channel and client.
     *
     * @param channel The channel the association is with
     * @param client  The user that holds the association
     */
    public XmppChannelClientInfo(final ChannelInfo channel, final ClientInfo client) {
        super(channel, client);
    }

    @Override
    public String getImportantModePrefix() {
        return ""; // TODO: Implement
    }

    @Override
    public String getImportantMode() {
        return ""; // TODO: Implement
    }

    @Override
    public String getAllModes() {
        return ""; // TODO: Implement
    }

    @Override
    public String getAllModesPrefix() {
        return ""; // TODO: Implement
    }

    @Override
    public void kick(final String message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(final ChannelClientInfo o) {
        return 0; // TODO: Implement
    }

}

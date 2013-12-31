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

import com.dmdirc.parser.interfaces.LocalClientInfo;

/**
 * A representation of the local XMPP client.
 */
public class XmppLocalClientInfo extends XmppClientInfo implements LocalClientInfo {

    /**
     * Creates a new XMPP Client Info object with the specified details.
     *
     * @param parser The parser that owns this client info object
     * @param nick The nickname of the user this object represents
     * @param user The username of the user this object represents
     * @param host The hostname of the user this object represents
     */
    public XmppLocalClientInfo(final XmppParser parser, final String nick,
            final String user, final String host) {
        super(parser, nick, user, host);
    }

    /** {@inheritDoc} */
    @Override
    public void setNickname(final String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public String getModes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setAway(final String reason) {
        getParser().setAway(reason);
    }

    /** {@inheritDoc} */
    @Override
    public void setBack() {
        getParser().setBack();
    }

    /** {@inheritDoc} */
    @Override
    public void alterMode(final boolean add, final Character mode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void flushModes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

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

package com.dmdirc.addons.parser_msn;

import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.common.BaseClientInfo;

import net.sf.jml.MsnUser;
import net.sf.jml.MsnUserStatus;

/**
 * An MSN-specific client info object.
 */
public class MSNClientInfo extends BaseClientInfo {

    /** MSN contact. */
    private final MsnUser contact;

    /**
     * Creates a new client info object with the specified details.
     *
     * @param contact The contact this object represents
     * @param parser The parser that owns this client info object
     * @param nick The nickname of the user this object represents
     * @param user The username of the user this object represents
     * @param host The hostname of the user this object represents
     */
    public MSNClientInfo(final MsnUser contact, final MSNParser parser,
            final String nick, final String user, final String host) {
        super(parser, nick, user, host);

        this.contact = contact;
    }

    /** {@inheritDoc} */
    @Override
    public int getChannelCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public AwayState getAwayState() {
        if (contact.getStatus() == MsnUserStatus.ONLINE) {
            return AwayState.HERE;
        }
        return AwayState.AWAY;
    }

    /** {@inheritDoc} */
    @Override
    public MSNParser getParser() {
        return (MSNParser) super.getParser();
    }

    /** {@inheritDoc} */
    @Override
    public String getRealname() {
        return contact.getDisplayName();
    }

    /**
     * Returns the contact for this client info object
     *
     * @return MSN contact for this client
     */
    public MsnUser getContact() {
        return contact;
    }

}

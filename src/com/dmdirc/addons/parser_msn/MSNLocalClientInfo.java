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
import com.dmdirc.parser.interfaces.LocalClientInfo;
import com.dmdirc.parser.interfaces.callbacks.AwayStateListener;

import net.sf.jml.MsnOwner;
import net.sf.jml.MsnUserStatus;

/**
 * A representation of the local MSN client.
 */
public class MSNLocalClientInfo extends MSNClientInfo implements LocalClientInfo {

    /** MSN user. */
    private final MsnOwner owner;

    /**
     * Creates a new local client info object with the specified details.
     *
     * @param owner The client this object represents
     * @param parser The parser that owns this client info object
     * @param nick The nickname of the user this object represents
     * @param user The username of the user this object represents
     * @param host The hostname of the user this object represents
     */
    public MSNLocalClientInfo(final MsnOwner owner, final MSNParser parser,
            final String nick, final String user, final String host) {
        super(owner, parser, nick, user, host);

        this.owner = owner;
    }

    /** {@inheritDoc} */
    @Override
    public void setNickname(String name) {
        owner.setDisplayName(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void setAway(String reason) {
        owner.setStatus(MsnUserStatus.AWAY);

        getParser().getCallbackManager().getCallbackType(AwayStateListener.class)
                .call(AwayState.HERE, AwayState.AWAY, reason);
    }

    /** {@inheritDoc} */
    @Override
    public void setBack() {
        owner.setStatus(MsnUserStatus.ONLINE);

        getParser().getCallbackManager().getCallbackType(AwayStateListener.class)
                .call(AwayState.AWAY, AwayState.HERE, null);
    }

    /** {@inheritDoc} */
    @Override
    public void alterMode(boolean add, Character mode) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void flushModes() {
        //Ignore
    }

}

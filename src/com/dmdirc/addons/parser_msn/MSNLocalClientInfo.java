/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.parser.interfaces.LocalClientInfo;

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
    public void setNickname(final String name) {
        owner.setDisplayName(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void setAway(final String reason) {
        if ("Busy".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.BUSY);
        } else if ("Idle".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.IDLE);
        } else if ("BRB".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.BE_RIGHT_BACK);
        } else if ("Be Right Back".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.BE_RIGHT_BACK);
        } else if ("phone".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.ON_THE_PHONE);
        } else if ("on the phone".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.ON_THE_PHONE);
        } else if ("on phone".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.ON_THE_PHONE);
        } else if ("Out to lunch".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.OUT_TO_LUNCH);
        } else if ("Lunch".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.OUT_TO_LUNCH);
        } else if ("Hide".equalsIgnoreCase(reason)) {
            owner.setStatus(MsnUserStatus.HIDE);
        } else {
            owner.setStatus(MsnUserStatus.AWAY);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setBack() {
        owner.setStatus(MsnUserStatus.ONLINE);
    }

    /** {@inheritDoc} */
    @Override
    public void alterMode(final boolean add, final Character mode) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void flushModes() {
        //Ignore
    }

}

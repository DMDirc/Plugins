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

package com.dmdirc.addons.dcc.actions;

import com.dmdirc.interfaces.actions.ActionMetaType;
import com.dmdirc.interfaces.actions.ActionType;

/**
 * DCC actions.
 */
public enum DCCActions implements ActionType {

    /** DCC Chat Request. */
    DCC_CHAT_REQUEST(DCCEvents.DCC_CHAT_REQUEST,
    "DCC chat requested"),
    /** DCC Chat Request Sent. */
    DCC_CHAT_REQUEST_SENT(DCCEvents.DCC_CHAT_REQUEST_SENT,
    "DCC chat request sent"),
    /** DCC Message from another person. */
    DCC_CHAT_MESSAGE(DCCEvents.DCC_CHAT_MESSAGE,
    "DCC chat message recieved"),
    /** DCC Message to another person. */
    DCC_CHAT_SELFMESSAGE(DCCEvents.DCC_CHAT_SELFMESSAGE,
    "DCC chat message sent"),
    /** DCC Chat Socket Closed. */
    DCC_CHAT_SOCKETCLOSED(DCCEvents.DCC_CHAT_SOCKETCLOSED,
    "DCC chat socket closed"),
    /** DCC Chat Socket Opened. */
    DCC_CHAT_SOCKETOPENED(DCCEvents.DCC_CHAT_SOCKETOPENED,
    "DCC chat socket opened"),
    /** DCC Send Socket Closed. */
    DCC_SEND_SOCKETCLOSED(DCCEvents.DCC_SEND_SOCKETCLOSED,
    "DCC send socket closed"),
    /** DCC Send Socket Opened. */
    DCC_SEND_SOCKETOPENED(DCCEvents.DCC_SEND_SOCKETOPENED,
    "DCC send socket opened"),
    /** DCC Send Data Transfered. */
    DCC_SEND_DATATRANSFERED(DCCEvents.DCC_SEND_DATATRANSFERED,
    "DCC send data transferred"),
    /** DCC Send Request. */
    DCC_SEND_REQUEST(DCCEvents.DCC_SEND_REQUEST,
    "DCC send requested"),
    /** DCC Send Request Sent. */
    DCC_SEND_REQUEST_SENT(DCCEvents.DCC_SEND_REQUEST_SENT,
    "DCC send request sent");
    /** The type of this action. */
    private final ActionMetaType type;
    /** The name of this action. */
    private final String name;

    /**
     * Constructs a new core action.
     *
     * @param type The type of this action
     * @param name The name of this action
     */
    DCCActions(final ActionMetaType type, final String name) {
        this.type = type;
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public ActionMetaType getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

}

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

import com.dmdirc.addons.dcc.ChatContainer;
import com.dmdirc.addons.dcc.TransferContainer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionMetaType;

import java.io.File;

/**
 * Defines DCC-related events.
 */
public enum DCCEvents implements ActionMetaType {

    /** DCC Chat Request. */
    DCC_CHAT_REQUEST(new String[]{"connection", "client"}, Connection.class, String.class),
    /** DCC Chat Request Sent. */
    DCC_CHAT_REQUEST_SENT(new String[]{"connection", "client"}, Connection.class, String.class),
    /** DCC Message from another person. */
    DCC_CHAT_MESSAGE(new String[]{"DCCChatWindow", "Nickname", "Message"}, ChatContainer.class,
            String.class, String.class),
    /** DCC Message to another person. */
    DCC_CHAT_SELFMESSAGE(new String[]{"DCCChatWindow", "Message"}, ChatContainer.class, String.class),
    /** DCC Chat Socket Closed. */
    DCC_CHAT_SOCKETCLOSED(new String[]{"DCCChatWindow"}, ChatContainer.class),
    /** DCC Chat Socket Opened. */
    DCC_CHAT_SOCKETOPENED(new String[]{"DCCChatWindow"}, ChatContainer.class),
    /** DCC Send Socket Closed. */
    DCC_SEND_SOCKETCLOSED(new String[]{"DCCSendWindow"}, TransferContainer.class),
    /** DCC Send Socket Opened. */
    DCC_SEND_SOCKETOPENED(new String[]{"DCCSendWindow"}, TransferContainer.class),
    /** DCC Send Data Transfered */
    DCC_SEND_DATATRANSFERED(new String[]{"DCCSendWindow", "Bytes Transfered"},
            TransferContainer.class, int.class),
    /** DCC Send Request. */
    DCC_SEND_REQUEST(new String[]{"connection", "client", "file"}, Connection.class, String.class,
            String.class),
    /** DCC Send Request Sent. */
    DCC_SEND_REQUEST_SENT(new String[]{"connection", "client", "file"}, Connection.class,
            String.class, File.class);
    /** The names of the arguments for this meta type. */
    private String[] argNames;
    /** The classes of the arguments for this meta type. */
    private Class[] argTypes;

    /**
     * Creates a new instance of this meta-type.
     *
     * @param argNames The names of the meta-type's arguments
     * @param argTypes The types of the meta-type's arguments
     */
    DCCEvents(final String[] argNames, final Class... argTypes) {
        this.argNames = argNames;
        this.argTypes = argTypes;
    }

    @Override
    public int getArity() {
        return argNames.length;
    }

    @Override
    public Class[] getArgTypes() {
        return argTypes;
    }

    @Override
    public String[] getArgNames() {
        return argNames;
    }

    @Override
    public String getGroup() {
        return "DCC Events";
    }

}
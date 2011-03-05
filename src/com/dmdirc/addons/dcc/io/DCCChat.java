/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.addons.dcc.io;

import com.dmdirc.addons.dcc.DCCChatHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * This class handles a DCC Chat
 */
public class DCCChat extends DCC {

    /** The handler for this DCCChat. */
    private DCCChatHandler handler = null;
    /** Used to send data out the socket. */
    private PrintWriter out;
    /** Used to read data from the socket. */
    private BufferedReader in;
    /** Are we active? */
    private boolean active = false;

    /**
     * Change the handler for this DCC Chat.
     *
     * @param handler A class implementing DCCChatHandler
     */
    public void setHandler(final DCCChatHandler handler) {
        this.handler = handler;
    }

    /** {@inheritDoc} */
    @Override
    protected void socketOpened() {
        active = true;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if (handler != null) {
                handler.socketOpened(this);
            }
        } catch (IOException ioe) {
            socketClosed();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void socketClosed() {
        out = null;
        in = null;
        if (handler != null) {
            handler.socketClosed(this);
        }
        active = false;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean handleSocket() {
        if (out == null || in == null) {
            return false;
        }
        final String inLine;
        try {
            inLine = in.readLine();
            if (inLine == null) {
                return false;
            } else {
                if (handler != null) {
                    handler.handleChatMessage(this, inLine);
                }
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWriteable() {
        return out != null;
    }

    /**
     * Send a line out the socket.
     *
     * @param line The line to be sent
     */
    public void sendLine(final String line) {
        if (out != null) {
            out.printf("%s\r\n", line);
        }
    }

    /**
     * Are we an active DCC Chat?
     *
     * @return true iif active
     */
    public boolean isActive() {
        return active;
    }

}

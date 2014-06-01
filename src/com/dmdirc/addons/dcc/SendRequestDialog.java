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

package com.dmdirc.addons.dcc;

import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.interfaces.Parser;

import java.awt.Window;

/**
 * Confirm DCC Send dialog.
 */
public class SendRequestDialog extends StandardQuestionDialog {

    private static final long serialVersionUID = 1L;
    private final DCCManager manager;
    private final String token;
    private final long ip;
    private final int port;
    private final String filename;
    private final long size;
    private final String nickname;
    private final Parser parser;

    public SendRequestDialog(final Window owner, final DCCManager manager, final String token,
            final long ip, final int port, final String filename, final long size,
            final String nickname, final Connection connection) {
        super(owner, ModalityType.APPLICATION_MODAL, "DCC Send Request",
                "User " + nickname + " on "
                + connection.getAddress()
                + " would like to send you a file over DCC.\n\nFile: "
                + filename + "\n\nDo you want to continue?");
        this.manager = manager;
        this.token = token;
        this.ip = ip;
        this.port = port;
        this.filename = filename;
        this.size = size;
        this.nickname = nickname;
        this.parser = connection.getParser();
    }

    @Override
    public boolean save() {
        manager.handleDCCSend(token, ip, port, filename, size, nickname, parser);
        return true;
    }

    @Override
    public void cancelled() {
    }

}

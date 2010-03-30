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

package com.dmdirc.addons.dcc;

import com.dmdirc.addons.dcc.io.DCCTransfer;

/**
 * This interfaces allows DCC Transfer Windows to receive data from a
 * DCCTransfer.
 *
 * @author Shane 'Dataforce' McCormack
 */
public interface DCCTransferHandler {

    /**
     * Called when the socket is closed.
     *
     * @param dcc The DCCTransfer that this message is from
     */
    void socketClosed(final DCCTransfer dcc);

    /**
     * Called when the socket is opened.
     *
     * @param dcc The DCCTransfer that this message is from
     */
    void socketOpened(final DCCTransfer dcc);

    /**
     * Called when data is sent/recieved.
     *
     * @param dcc The DCCTransfer that this message is from
     * @param bytes The number of new bytes that were transfered
     */
    void dataTransfered(final DCCTransfer dcc, final int bytes);

}

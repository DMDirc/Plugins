/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.osd;

/**
 * Class for storing OSD Window timeout values and messages for the window queue.
 *
 * @since 0.6.5
 */
public class QueuedMessage {

    /** Timeout delay in seconds. */
    private final int timeout;
    /** Message to display on the OSD. */
    private final String message;

    /**
     * Creates a new instance of QueuedMessage.
     *
     * @param timeout Timeout for the OSD window in seconds
     * @param message Message to display in the OSD
     */
    public QueuedMessage(final int timeout, final String message) {
        this.timeout = timeout;
        this.message = message;
    }

    /**
     * Returns the timeout for this OSD window.
     *
     * @return Timeout in seconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Returns the message for the OSD window.
     *
     * @return Message for the OSD window
     */
    public String getMessage() {
        return message;
    }

}
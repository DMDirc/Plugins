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

package com.dmdirc.addons.logging;

import java.io.BufferedWriter;

/**
 * Represents an open file and the description of it.
 */
public class OpenFile {
    /** Last used time. */
    private long lastUsedTime = System.currentTimeMillis();
    /** Open file's writer. */
    private BufferedWriter writer = null;

    /**
     * Creates a new instance of this class.
     *
     * @param writer
     */
    protected OpenFile(final BufferedWriter writer) {
        this.writer = writer;
    }

    /**
     * Returns the last time this file was accessed by the client.
     *
     * @return last used time
     */
    public long getLastUsedTime() {
        return lastUsedTime;
    }

    /**
     * Sets the last used time for this file
     *
     * @param lastUsedTime Time this file was last used
     */
    public void setLastUsedTime(final long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    /**
     * Returns the writer used by this file.
     *
     * @return File's writer
     */
    public BufferedWriter getWriter() {
        return writer;
    }
}

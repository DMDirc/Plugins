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

package com.dmdirc.addons.conditional_execute;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.ui.input.TabCompleter;

import java.util.Collections;
import java.util.Date;

/**
 * Implements a fake input window, which swallows command output.
 */
public class BlackholeWriteableFrameContainer extends WritableFrameContainer {

    /** Real source window. */
    private final MessageTarget source;

    /**
     * Creates a new instance of BlackholeWriteableFrameContainer.
     *
     * @param target The message target that output gets sent to
     */
    public BlackholeWriteableFrameContainer(final MessageTarget source) {
        super(source.getIcon(), source.getName(), source.getTitle(), source.getConfigManager(), source.getCommandParser(), Collections.<String>emptyList());
        this.source = source;
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String line, final boolean timestamp) {
        // Do Nothing
    }

    /** {@inheritDoc} */
    @Override
    public void sendLine(final String line) {
        // Do Nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String type, final Date timestamp, final Object... args) {
        // Do Nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String type, final Object... args) {
        // Do Nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final StringBuffer type, final Date timestamp, final Object... args) {
        // Do Nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final StringBuffer type, final Object... args) {
        // Do Nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String line, final Date timestamp) {
        // Do Nothing
    }

    /** {@inheritDoc} */
    @Override
    public TabCompleter getTabCompleter() {
        return source.getTabCompleter();
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return source.getMaxLineLength();
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return source.getServer();
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // Do nothing
    }
}

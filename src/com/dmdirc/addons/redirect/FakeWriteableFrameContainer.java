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

package com.dmdirc.addons.redirect;

import com.dmdirc.MessageTarget;
import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;

/**
 * Implements a fake input window, which sends echoed text to the specified
 * chat window instead.
 * 
 * @author Chris
 */
public class FakeWriteableFrameContainer extends WritableFrameContainer<InputWindow> {
    
    /** The target for this window. */
    private final MessageTarget<? extends InputWindow> target;

    /**
     * Creates a new instance of FakeInputWindow.
     * 
     * @param target The message target that output gets sent to
     */
    public FakeWriteableFrameContainer(final MessageTarget<?> target) {
        super(target.getIcon(), target.getName(), target.getTitle(),
                InputWindow.class, target.getConfigManager(), target.getCommandParser());
        this.target = target;
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String line, final boolean timestamp) {
        target.sendLine(line);
    }

    /** {@inheritDoc} */
    @Override
    public void sendLine(final String line) {
        target.sendLine(line);
    }

    /** {@inheritDoc} */
    @Override
    public TabCompleter getTabCompleter() {
        return target.getTabCompleter();
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return target.getMaxLineLength();
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return target.getServer();
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

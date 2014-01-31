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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dmdirc.addons.swingdebug;

import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.core.components.WindowComponent;

import java.io.IOException;
import java.util.Arrays;

/**
 * Redirects either System.out or System.err to a window in the client.
 */
public class SystemStreamContainer extends FrameContainer {

    /** Stream reader thread. */
    private SystemStreamRedirectThread thread;
    /** Parent plugin. */
    private SwingDebugPlugin plugin;

    /**
     * Creates a new system stream container wrapping the specified stream.
     *
     * @param stream Stream to wrap
     * @param config Config to wrap
     * @param plugin Parent plugin
     */
    public SystemStreamContainer(final SystemStreamType stream,
            final AggregateConfigProvider config,
            final SwingDebugPlugin plugin) {
        super("dmdirc", stream.toString(), stream.toString(), config,
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier()));
        this.plugin = plugin;
        try {
            thread = new SystemStreamRedirectThread(stream, getDocument());
            thread.start();
        } catch (IOException ex) {
        }
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection() {
        return getParent() == null ? null : getParent().getConnection();
    }

    /** {@inheritDoc} */
    @Override
    protected void windowClosing() {
        thread.cancel();
        plugin.windowClosing(this);
        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
    }

}

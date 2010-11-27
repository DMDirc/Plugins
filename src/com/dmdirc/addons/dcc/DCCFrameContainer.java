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

import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;

/**
 * This class links DCC objects to a window.
 *
 * @param <T> The type of window which corresponds to this DCC frame
 * @author Shane 'Dataforce' McCormack
 */
public abstract class DCCFrameContainer<T extends InputWindow> extends WritableFrameContainer<T> {

    /** The dcc plugin that owns this frame */
    protected final DCCPlugin plugin;

    /** The Window we're using. */
    private boolean windowClosing = false;

    /**
     * Creates a new instance of DCCFrame.
     *
     * @param plugin The DCCPlugin that owns this frame
     * @param title The title of this window
     * @param icon The icon to use
     * @param windowClass The class of window to use for this container
     * @param parser Command parser to use for this window
     */
    public DCCFrameContainer(final DCCPlugin plugin, final String title, final String icon,
            final Class<T> windowClass, final CommandParser parser) {
        super(icon, title, title, windowClass, IdentityManager.getGlobalConfig(), parser);
        this.plugin = plugin;
    }

    /**
     * Sends a line of text to this container's source.
     *
     * @param line The line to be sent
     */
    @Override
    public void sendLine(final String line) {
    }

    /**
     * Returns the maximum length that a line passed to sendLine() should be,
     * in order to prevent it being truncated or causing protocol violations.
     *
     * @return The maximum line length for this container
     */
    @Override
    public int getMaxLineLength() {
        return 512;
    }

    /**
     * Returns the server instance associated with this container.
     *
     * @return the associated server connection
     */
    @Override
    public Server getServer() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TabCompleter getTabCompleter() {
        return new TabCompleter();
    }

    /**
     * Is the window closing?
     *
     * @return True if windowClosing has been called.
     */
    public final boolean isWindowClosing() {
        return windowClosing;
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        windowClosing = true;

        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
    }

}

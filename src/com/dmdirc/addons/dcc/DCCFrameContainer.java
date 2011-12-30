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

package com.dmdirc.addons.dcc;

import com.dmdirc.Server;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.ui.input.TabCompleter;

import java.util.Collection;

/**
 * This class links DCC objects to a window.
 */
public abstract class DCCFrameContainer extends WritableFrameContainer {

    /** The Window we're using. */
    private boolean windowClosing = false;

    /**
     * Creates a new instance of DCCFrame.
     *
     * @param title The title of this window
     * @param icon The icon to use
     * @param configManager Config manager
     * @param parser Command parser to use for this window
     * @param components The UI components that this frame requires
     */
    public DCCFrameContainer(final String title, final String icon,
            final ConfigManager configManager, final CommandParser parser,
            final Collection<String> components) {
        super(icon, title, title, configManager, parser, components);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxLineLength() {
        return 512;
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() { //NOPMD - server will always be null
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

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

package com.dmdirc.addons.parserdebug;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.ui.core.components.WindowComponent;

import java.util.Arrays;

/**
 * This class is used to show the parser debug in a window
 */
public class DebugWindow extends FrameContainer {

    /** The plugin that owns this window */
    protected final DebugPlugin plugin;
    /** The parser this window is debugging */
    protected Parser parser;
    /** The connection we're operating on */
    protected final Connection connection;

    /**
     * Creates a new instance of DebugWindow.
     *
     * @param plugin The plugin that owns this window
     * @param title The title of this window
     * @param parser The parser this plugin is debugging
     * @param server The Server window this is a child of
     */
    public DebugWindow(final DebugPlugin plugin, final String title, final Parser parser, final Server server) {
        super("raw", "Parser Debug", title, server.getConfigManager(),
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier()));
        this.plugin = plugin;
        this.parser = parser;
        this.connection = server;
    }

    /**
     * Returns the server instance associated with this container.
     *
     * @return the associated server connection
     */
    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * Set the parser to null to stop us holding onto parsers when the server
     * connection is closed.
     */
    public void unsetParser() {
        addLine("======================", true);
        addLine("Unset parser: "+parser, true);
        parser = null;
    }

    /**
     * Closes this container (and its associated frame).
     */
    @Override
    public void windowClosing() {
        // 2: Remove any callbacks or listeners
        if (parser != null) {
            parser.getCallbackManager().delCallback(DebugInfoListener.class, plugin);
        }

        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
    }

}

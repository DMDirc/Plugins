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

package com.dmdirc.addons.parserdebug;

import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;

/**
 * This class is used to show the parser debug in a window
 *
 * @author Shane 'Dataforce' McCormack
 */
public class DebugWindow extends FrameContainer<Window> {
    /** The window title. */
    protected final String title;
    /** The plugin that owns this window */
    protected DebugPlugin plugin;
    /** The parser this window is debugging */
    protected Parser parser;
    /** The Server window we are a child of */
    protected Server server;
    
    /**
     * Creates a new instance of DebugWindow.
     *
     * @param plugin The plugin that owns this window
     * @param title The title of this window
     * @param parser The parser this plugin is debugging
     * @param server The Server window this is a child of
     */
    public DebugWindow(final DebugPlugin plugin, final String title, final Parser parser, final Server server) {
        super("raw", "Parser Debug", "Parser Debug", Window.class, server.getConfigManager());
        this.title = title;
        this.plugin = plugin;
        this.parser = parser;
        this.server = server;

        WindowManager.addWindow(server, this);
    }
    
    /**
     * Returns a string identifier for this object/its frame.
     *
     * @return String identifier
     */
    @Override
    public String toString() {
        return title;
    }
    
    /**
     * Returns the server instance associated with this container.
     *
     * @return the associated server connection
     */
    @Override
    public Server getServer() {
        return server;
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
     * Closes this container (and it's associated frame).
     */
    @Override
    public void windowClosing() {
        // 1: Make the window non-visible
        for (Window window : getWindows()) {
            window.setVisible(false);
        }
        
        // 2: Remove any callbacks or listeners
        try {
            if (parser != null) {
                parser.getCallbackManager().delCallback(DebugInfoListener.class, plugin);
            }
        } catch (Exception e) { }
        
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
        
        // 6: Remove the window from the window manager
        WindowManager.removeWindow(this);
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
        this.parser = null;
        this.server = null;
        this.plugin = null;
    }
}

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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
import com.dmdirc.ui.WindowManager;

import java.awt.Toolkit;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Manages swing components and dependencies.
 */
@Singleton
public class SwingManager {

    /** The event queue to use. */
    private final DMDircEventQueue eventQueue;

    /** The window factory in use. */
    private final SwingWindowFactory windowFactory;

    /** The window manager to listen on for events. */
    private final WindowManager windowManager;

    /** The main frame of the Swing UI. */
    private final MainFrame mainFrame;

    /**
     * Creates a new instance of {@link SwingManager}.
     *
     * @param eventQueue The event queue to use.
     * @param windowFactory The window factory in use.
     * @param windowManager The window manager to listen on for events.
     * @param mainFrame The main frame of the Swing UI.
     * @param menuBar The menu bar to use for the main frame.
     */
    @Inject
    public SwingManager(
            final DMDircEventQueue eventQueue,
            final SwingWindowFactory windowFactory,
            final WindowManager windowManager,
            final MainFrame mainFrame,
            final MenuBar menuBar) {
        this.eventQueue = eventQueue;
        this.windowFactory = windowFactory;
        this.windowManager = windowManager;

        this.mainFrame = mainFrame;
        this.mainFrame.setMenuBar(menuBar);
    }

    /**
     * Handles loading of the UI.
     */
    public void load() {
        installEventQueue();
        windowManager.addListenerAndSync(windowFactory);
    }

    /**
     * Handles unloading of the UI.
     */
    public void unload() {
        uninstallEventQueue();
        windowManager.removeListener(windowFactory);
        windowFactory.dispose();
        mainFrame.dispose();
    }

    /**
     * Retrieves the window factory to use.
     *
     * @return A swing window factory instance.
     * @deprecated Should be injected.
     */
    @Deprecated
    public SwingWindowFactory getWindowFactory() {
        return windowFactory;
    }

    /**
     * Retrieves the main frame.
     *
     * @return A main frame instance.
     * @deprecated Should be injected.
     */
    @Deprecated
    public MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Installs the DMDirc event queue.
     */
    private void installEventQueue() {
        UIUtilities.invokeAndWait(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);
            }
        });
    }

    /**
     * Removes the DMDirc event queue.
     */
    private void uninstallEventQueue() {
        eventQueue.pop();
    }

}

/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.components.frames.CustomFrame;
import com.dmdirc.addons.ui_swing.components.frames.CustomInputFrame;
import com.dmdirc.addons.ui_swing.components.frames.QueryFrame;
import com.dmdirc.addons.ui_swing.components.frames.ServerFrame;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.util.ListenerList;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles creation of windows in the Swing UI.
 * 
 * @since 0.6.4
 * @author chris
 */
public class SwingWindowFactory implements FrameListener {

    /** A map of known implementations of window interfaces. */
    private static final Map<Class<? extends Window>, Class<? extends Window>> IMPLEMENTATIONS
            = new HashMap<Class<? extends Window>, Class<? extends Window>>();

    static {
        IMPLEMENTATIONS.put(Window.class, CustomFrame.class);
        IMPLEMENTATIONS.put(InputWindow.class, CustomInputFrame.class);
        IMPLEMENTATIONS.put(ServerWindow.class, ServerFrame.class);
        IMPLEMENTATIONS.put(QueryWindow.class, QueryFrame.class);
        IMPLEMENTATIONS.put(ChannelWindow.class, ChannelFrame.class);
    }

    /** The controller that owns this window factory. */
    private final SwingController controller;

    /** Our list of listeners. */
    private final ListenerList listeners = new ListenerList();

    /**
     * Creates a new window factory for the specified controller.
     *
     * @param controller The controller this factory is for
     */
    public SwingWindowFactory(final SwingController controller) {
        this.controller = controller;
    }

    /**
     * Registers a new listener which will be notified about the addition
     * and deletion of all Swing UI windows.
     *
     * @param listener The listener to be added
     */
    public void addWindowListener(final SwingWindowListener listener) {
        listeners.add(SwingWindowListener.class, listener);
    }

    /**
     * Un-registers the specified listener from being notificed about the
     * addiction and deletion of all Swing UI windows.
     *
     * @param listener The listener to be removed
     */
    public void removeWindowListener(final SwingWindowListener listener) {
        listeners.remove(SwingWindowListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer<?> window, final boolean focus) {
        addWindow(null, window, focus);
    }

    /**
     * Creates a new window for the specified container.
     *
     * @param <T> The type of window that should be created
     * @param window The container that owns the window
     * @param focus Whether the window should be focused initially
     * @return The created window or null on error
     */
    @SuppressWarnings("unchecked")
    protected <T extends Window> T doAddWindow(final FrameContainer<T> window,
            final boolean focus) {
        final Class<T> clazz;

        if (IMPLEMENTATIONS.containsKey(window.getWindowClass())) {
            clazz = (Class<T>) IMPLEMENTATIONS.get(window.getWindowClass());
        } else {
            clazz = window.getWindowClass();
        }

        try {
            final T frame = (T) clazz.getConstructors()[0].newInstance(controller, window);
            window.addWindow(frame);

            return frame;
        } catch (Exception ex) {
            Logger.appError(ErrorLevel.HIGH, "Unable to create window", ex);
            return null;
        }
    }

    /**
     * Retrieves a single Swing UI created window belonging to the specified
     * container. Returns null if the container is null or no such window exists.
     *
     * @param window The container whose windows should be searched
     * @return A relevant window or null
     */
    public Window getSwingWindow(final FrameContainer<?> window) {
        if (window == null) {
            return null;
        }

        for (Window child : window.getWindows()) {
            if (child.getController() == controller) {
                return child;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer<?> window) {
        delWindow(null, window);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer<?> parent,
            final FrameContainer<?> window, final boolean focus) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final Window parentWindow = getSwingWindow(parent);
                final Window childWindow = doAddWindow(window, focus);

                if (childWindow == null) {
                    return;
                }

                for (SwingWindowListener listener : listeners.get(SwingWindowListener.class)) {
                    listener.windowAdded(parentWindow, childWindow);
                }

                if (focus) {
                    childWindow.activateFrame();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer<?> parent, final FrameContainer<?> window) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final Window parentWindow = getSwingWindow(parent);
                final Window childWindow = getSwingWindow(window);

                for (SwingWindowListener listener : listeners.get(SwingWindowListener.class)) {
                    listener.windowDeleted(parentWindow, childWindow);
                }
            }
        });
    }
}

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

package com.dmdirc.addons.ui_web;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_web.uicomponents.WebChannelWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebInputWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebQueryWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebServerWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.ChannelWindow;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.QueryWindow;
import com.dmdirc.ui.interfaces.ServerWindow;
import com.dmdirc.ui.interfaces.Window;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages WebUI windows.
 *
 * @author chris
 */
public class WebWindowManager implements FrameListener {

    /** A map of known implementations of window interfaces. */
    private static final Map<Class<? extends Window>, Class<? extends Window>> IMPLEMENTATIONS
            = new HashMap<Class<? extends Window>, Class<? extends Window>>();

    static {
        IMPLEMENTATIONS.put(Window.class, WebWindow.class);
        IMPLEMENTATIONS.put(InputWindow.class, WebInputWindow.class);
        IMPLEMENTATIONS.put(ServerWindow.class, WebServerWindow.class);
        IMPLEMENTATIONS.put(QueryWindow.class, WebQueryWindow.class);
        IMPLEMENTATIONS.put(ChannelWindow.class, WebChannelWindow.class);
    }

    /** The controller that owns this manager. */
    private final WebInterfaceUI controller;

    /** Map of known windows. */
    private final Map<FrameContainer<?>, Window> windows
            = new HashMap<FrameContainer<?>, Window>();

    public WebWindowManager(final WebInterfaceUI controller) {
        this.controller = controller;
        
        WindowManager.addFrameListener(this);

        for (FrameContainer<?> container : WindowManager.getRootWindows()) {
            recursiveAdd(container);
        }
    }

    public Window getWindow(final FrameContainer<?> window) {
        return windows.get(window);
    }

    private void recursiveAdd(final FrameContainer<?> window) {
        addWindow(window, false);

        for (FrameContainer<?> child : window.getChildren()) {
            recursiveAdd(child);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer<?> window, final boolean focus) {
        windows.put(window, doAddWindow(window, focus));
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer<?> window) {
        windows.get(window).close();
        windows.remove(window);
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer<?> parent, final FrameContainer<?> window,
            final boolean focus) {
        addWindow(window, focus);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer<?> parent, final FrameContainer<?> window) {
        delWindow(window);
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

}

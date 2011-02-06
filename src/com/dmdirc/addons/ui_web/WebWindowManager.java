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
import com.dmdirc.addons.ui_web.uicomponents.WebServerWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.interfaces.FrameListener;
import com.dmdirc.ui.interfaces.Window;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Manages WebUI windows.
 */
public class WebWindowManager implements FrameListener {

    /** A map of known implementations of window interfaces. */
    private static final Map<Collection<String>, Class<? extends Window>> IMPLEMENTATIONS
            = new HashMap<Collection<String>, Class<? extends Window>>();

    static {
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier())),
                WebWindow.class);
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier())),
                WebInputWindow.class);
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier(),
                WindowComponent.CERTIFICATE_VIEWER.getIdentifier())),
                WebServerWindow.class);
        IMPLEMENTATIONS.put(new HashSet<String>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier(),
                WindowComponent.TOPICBAR.getIdentifier(),
                WindowComponent.USERLIST.getIdentifier())),
                WebChannelWindow.class);
    }

    /** The controller that owns this manager. */
    private final WebInterfaceUI controller;

    /** Map of known windows. */
    private final Map<FrameContainer, Window> windows
            = new HashMap<FrameContainer, Window>();

    /**
     * Creates a new window manager for the specified controller.
     *
     * @param controller The Web UI controller that owns this manager
     */
    public WebWindowManager(final WebInterfaceUI controller) {
        this.controller = controller;

        WindowManager.addFrameListener(this);

        for (FrameContainer container : WindowManager.getRootWindows()) {
            recursiveAdd(container);
        }
    }

    /**
     * Retrieves the web window corresponding to the specified container,
     * if any.
     *
     * @param container The container whose window should be retrieved
     * @return The corresponding web window, or null if there is none
     */
    public Window getWindow(final FrameContainer container) {
        return windows.get(container);
    }

    /**
     * Recursively adds a window for the specified container and all of its
     * children.
     *
     * @param container The container to create windows for
     */
    private void recursiveAdd(final FrameContainer container) {
        addWindow(container, false);

        for (FrameContainer child : container.getChildren()) {
            recursiveAdd(child);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer window, final boolean focus) {
        doAddWindow(window, focus);
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer window) {
        if (windows.containsKey(window)) {
            windows.remove(window);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addWindow(final FrameContainer parent, final FrameContainer window,
            final boolean focus) {
        if (windows.containsKey(parent)) {
            addWindow(window, focus);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        if (windows.containsKey(parent)) {
            delWindow(window);
        }
    }

    /**
     * Creates a new window for the specified container.
     *
     * @param window The container that owns the window
     * @param focus Whether the window should be focused initially
     */
    protected void doAddWindow(final FrameContainer window,
            final boolean focus) {
        final Class<? extends Window> clazz;

        if (IMPLEMENTATIONS.containsKey(window.getComponents())) {
            clazz = IMPLEMENTATIONS.get(window.getComponents());
        } else {
            clazz = window.getWindowClass();
        }

        try {
            final Window frame = (Window) clazz.getConstructors()[0].newInstance(controller, window);

            windows.put(window, frame);
        } catch (Exception ex) {
            Logger.appError(ErrorLevel.MEDIUM, "Unable to create window of type "
                    + clazz.getCanonicalName() + " for web ui", ex);
        }
    }

}

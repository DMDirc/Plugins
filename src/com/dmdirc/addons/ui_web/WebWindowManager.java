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

package com.dmdirc.addons.ui_web;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_web.uicomponents.WebInputWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.ui.FrameListener;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.WindowComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Manages WebUI windows.
 */
public class WebWindowManager implements FrameListener {

    /** A map of known implementations of window interfaces. */
    private static final Map<Collection<String>, Class<? extends Window>> IMPLEMENTATIONS
            = new HashMap<>();

    static {
        IMPLEMENTATIONS.put(new HashSet<>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier())),
                WebWindow.class);
        IMPLEMENTATIONS.put(new HashSet<>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier())),
                WebInputWindow.class);
        IMPLEMENTATIONS.put(new HashSet<>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier(),
                        WindowComponent.CERTIFICATE_VIEWER.getIdentifier())),
                WebInputWindow.class);
        IMPLEMENTATIONS.put(new HashSet<>(
                Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier(),
                        WindowComponent.TOPICBAR.getIdentifier(),
                        WindowComponent.USERLIST.getIdentifier())),
                WebInputWindow.class);
    }
    /** The controller that owns this manager. */
    private final WebInterfaceUI controller;
    /** The ID of the next window to be created. */
    private long nextId = 0L;
    /** Map of known windows. */
    private final Map<FrameContainer, WebWindow> windows = new HashMap<>();
    /** A map of window IDs to their windows. */
    private final Map<String, WebWindow> windowsById = new HashMap<>();
    /** Event bus to post errors . */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new window manager for the specified controller.
     *
     * @param controller    The Web UI controller that owns this manager
     * @param windowManager Window manager to add/remove windows
     * @param eventBus      The event bus to post errors on
     */
    public WebWindowManager(final WebInterfaceUI controller, final WindowManager windowManager,
            final DMDircMBassador eventBus) {
        this.controller = controller;
        this.eventBus = eventBus;

        windowManager.addListenerAndSync(this);
    }

    /**
     * Retrieves the web window corresponding to the specified container, if any.
     *
     * @param container The container whose window should be retrieved
     *
     * @return The corresponding web window, or null if there is none
     */
    public WebWindow getWindow(@Nullable final FrameContainer container) {
        return windows.get(container);
    }

    /**
     * Retrieves the web window with the specified ID, if any.
     *
     * @param id The ID of the window to retrieve
     *
     * @return The corresponding web window, or null if there isn't one
     */
    public WebWindow getWindow(final String id) {
        return windowsById.get(id);
    }

    @Override
    public void addWindow(final FrameContainer window, final boolean focus) {
        doAddWindow(window, focus);
    }

    @Override
    public void delWindow(final FrameContainer window) {
        if (windows.containsKey(window)) {
            windowsById.remove(windows.get(window).getId());
            windows.remove(window);
        }
    }

    @Override
    public void addWindow(final FrameContainer parent, final FrameContainer window,
            final boolean focus) {
        if (windows.containsKey(parent)) {
            addWindow(window, focus);
        }
    }

    @Override
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        if (windows.containsKey(parent)) {
            delWindow(window);
        }
    }

    /**
     * Retrieves a collection of all known windows.
     *
     * @return The collection of all known windows
     */
    public Collection<WebWindow> getWindows() {
        return Collections.unmodifiableCollection(windows.values());
    }

    /**
     * Creates a new window for the specified container.
     *
     * @param window The container that owns the window
     * @param focus  Whether the window should be focused initially
     */
    protected void doAddWindow(final FrameContainer window,
            final boolean focus) {
        final Class<? extends Window> clazz;

        if (IMPLEMENTATIONS.containsKey(window.getComponents())) {
            clazz = IMPLEMENTATIONS.get(window.getComponents());
        } else {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, null,
                    "Unable to create web window for components: " + window.getComponents(), ""));
            return;
        }

        try {
            final String id = String.valueOf(nextId++);

            final WebWindow frame = (WebWindow) clazz.getConstructors()[0].newInstance(controller,
                    window, id);

            windows.put(window, frame);
            windowsById.put(id, frame);
        } catch (ReflectiveOperationException ex) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex,
                    "Unable to create window of type " + clazz.getCanonicalName() + " for web ui",
                    ""));
        }
    }

}

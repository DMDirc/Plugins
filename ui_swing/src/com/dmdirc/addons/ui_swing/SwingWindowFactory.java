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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.CustomFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.CustomInputFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.ServerFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.addons.ui_swing.injection.SwingEventBus;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.ui.FrameListener;
import com.dmdirc.logger.ErrorLevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Handles creation of windows in the Swing UI.
 *
 * @since 0.6.4
 */
@Singleton
public class SwingWindowFactory implements FrameListener {

    /** A map of known implementations of window interfaces. */
    private final Map<Collection<String>, WindowProvider> implementations = new HashMap<>();
    /** A map of frame containers to their Swing windows. */
    private final Map<FrameContainer, TextFrame> windows = new HashMap<>();
    /** Active window manager. */
    private final Provider<ActiveFrameManager> activeFrameManager;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;
    /** The swing event bus. */
    private final DMDircMBassador swingEventBus;

    /**
     * Creates a new window factory for the specified controller.
     *
     * @param activeFrameManager      The provider for the active frame manager.
     * @param customFrameFactory      The factory to use to produce custom frames.
     * @param customInputFrameFactory The factory to use to produce custom input frames.
     * @param serverFrameFactory      The factory to use to produce server frames.
     * @param channelFrameFactory     The factory to use to produce channel frames.
     * @param eventBus                The event bus to post errors to
     * @param swingEventBus           The swing event bus;
     */
    @Inject
    public SwingWindowFactory(
            final Provider<ActiveFrameManager> activeFrameManager,
            final CustomFrameFactory customFrameFactory,
            final CustomInputFrameFactory customInputFrameFactory,
            final ServerFrameFactory serverFrameFactory,
            final ChannelFrameFactory channelFrameFactory,
            final DMDircMBassador eventBus,
            @SwingEventBus final DMDircMBassador swingEventBus) {
        this.activeFrameManager = activeFrameManager;
        this.eventBus = eventBus;
        this.swingEventBus = swingEventBus;

        registerImplementation(customFrameFactory);
        registerImplementation(customInputFrameFactory);
        registerImplementation(serverFrameFactory);
        registerImplementation(channelFrameFactory);
    }

    /**
     * Registers a new provider that will be used to create certain window implementations.
     *
     * <p>
     * If a previous provider exists for the same configuration, it will be replaced.
     *
     * @param provider   The provider to use to generate new windows.
     */
    public final void registerImplementation(final WindowProvider provider) {
        implementations.put(provider.getComponents(), provider);
    }

    @Override
    public void addWindow(final FrameContainer window, final boolean focus) {
        addWindow(null, window, focus);
    }

    @Override
    public void addWindow(final FrameContainer parent, final FrameContainer window,
            final boolean focus) {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final TextFrame parentWindow = getSwingWindow(parent);
                final TextFrame childWindow = doAddWindow(window);

                if (childWindow == null) {
                    return;
                }
                swingEventBus.publish(new SwingWindowAddedEvent(
                        Optional.ofNullable(parentWindow), childWindow));

                if (focus) {
                    activeFrameManager.get().setActiveFrame(childWindow);
                }
            }
        });
    }

    /**
     * Creates a new window for the specified container.
     *
     * @param window The container that owns the window
     *
     * @return The created window or null on error
     */
    protected TextFrame doAddWindow(final FrameContainer window) {
        if (!implementations.containsKey(window.getComponents())) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.HIGH, null,
                    "Unable to create window: Unknown type.", ""));
            return null;
        }

        final WindowProvider provider = implementations.get(window.getComponents());
        final TextFrame frame = provider.getWindow(window);
        if (frame != null) {
            windows.put(window, frame);
        }
        return frame;
    }

    @Override
    public void delWindow(final FrameContainer window) {
        delWindow(null, window);
    }

    @Override
    public void delWindow(final FrameContainer parent, final FrameContainer window) {
        final TextFrame parentWindow = getSwingWindow(parent);
        final TextFrame childWindow = getSwingWindow(window);
        windows.remove(window);
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingEventBus.publish(new SwingWindowDeletedEvent(
                        Optional.ofNullable(parentWindow), childWindow));
            }
        });
    }

    /**
     * Retrieves a single Swing UI created window belonging to the specified container. Returns null
     * if the container is null or no such window exists.
     *
     * @param window The container whose windows should be searched
     *
     * @return A relevant window or null
     */
    public TextFrame getSwingWindow(@Nullable final FrameContainer window) {
        return windows.get(window);
    }

    /** Disposes of this window factory, removing all listeners. */
    public void dispose() {
        for (TextFrame frame : windows.values()) {
            frame.dispose();
        }
    }

    /**
     * Provides a new window instance for a container.
     */
    public interface WindowProvider {

        /**
         * Gets a new window for the specified container.
         *
         * @param container The container to create a new window for.
         *
         * @return A new window for the given container.
         */
        TextFrame getWindow(FrameContainer container);

        /**
         * Gets the set of components that this provider can provide windows for.
         *
         * @return The components this provider operates on.
         */
        Set<String> getComponents();

    }

}

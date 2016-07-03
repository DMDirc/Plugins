/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.components.frames.ChannelFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.CustomFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.CustomInputFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.ServerFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingActiveWindowChangeRequestEvent;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.ui.FrameListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * Handles creation of windows in the Swing UI.
 *
 * @since 0.6.4
 */
@Singleton
public class SwingWindowFactory implements FrameListener {

    private static final Logger LOG = LoggerFactory.getLogger(SwingWindowFactory.class);
    /** A map of known implementations of window interfaces. */
    private final Map<Collection<String>, WindowProvider> implementations = new HashMap<>();
    /** A map of frame containers to their Swing windows. */
    private final Map<WindowModel, TextFrame> windows = new HashMap<>();
    /** The swing event bus. */
    private final SwingEventBus swingEventBus;

    /**
     * Creates a new window factory for the specified controller.
     *
     * @param customFrameFactory      The factory to use to produce custom frames.
     * @param customInputFrameFactory The factory to use to produce custom input frames.
     * @param serverFrameFactory      The factory to use to produce server frames.
     * @param channelFrameFactory     The factory to use to produce channel frames.
     * @param swingEventBus           The swing event bus;
     */
    @Inject
    public SwingWindowFactory(
            final CustomFrameFactory customFrameFactory,
            final CustomInputFrameFactory customInputFrameFactory,
            final ServerFrameFactory serverFrameFactory,
            final ChannelFrameFactory channelFrameFactory,
            final SwingEventBus swingEventBus) {
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
    public void addWindow(final WindowModel window, final boolean focus) {
        addWindow(null, window, focus);
    }

    @Override
    public void addWindow(final WindowModel parent, final WindowModel window,
            final boolean focus) {
        UIUtilities.invokeLater(() -> {
            final TextFrame parentWindow = getSwingWindow(parent);
            final TextFrame childWindow = doAddWindow(window);

            if (childWindow == null) {
                return;
            }
            swingEventBus.publish(new SwingWindowAddedEvent(
                    Optional.ofNullable(parentWindow), childWindow));

            if (focus) {
                swingEventBus.publishAsync(new SwingActiveWindowChangeRequestEvent(
                                Optional.ofNullable(childWindow)));
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
    protected TextFrame doAddWindow(final WindowModel window) {
        if (!implementations.containsKey(window.getComponents())) {
            LOG.error(USER_ERROR, "Unable to create window: Unknown type.");
            return null;
        }

        final WindowProvider provider = implementations.get(window.getComponents());
        final TextFrame frame = provider.getWindow(window);
        if (frame != null) {
            frame.init();
            windows.put(window, frame);
        }
        return frame;
    }

    @Override
    public void delWindow(final WindowModel window) {
        delWindow(null, window);
    }

    @Override
    public void delWindow(final WindowModel parent, final WindowModel window) {
        final TextFrame parentWindow = getSwingWindow(parent);
        final TextFrame childWindow = getSwingWindow(window);
        windows.remove(window);
        UIUtilities.invokeLater(() -> swingEventBus.publish(new SwingWindowDeletedEvent(
                Optional.ofNullable(parentWindow), childWindow)));
    }

    /**
     * Retrieves a single Swing UI created window belonging to the specified container. Returns null
     * if the container is null or no such window exists.
     *
     * @param window The container whose windows should be searched
     *
     * @return A relevant window or null
     */
    public TextFrame getSwingWindow(@Nullable final WindowModel window) {
        return windows.get(window);
    }

    /** Disposes of this window factory, removing all listeners. */
    public void dispose() {
        windows.values().forEach(TextFrame::dispose);
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
        TextFrame getWindow(WindowModel container);

        /**
         * Gets the set of components that this provider can provide windows for.
         *
         * @return The components this provider operates on.
         */
        Set<String> getComponents();

    }

}

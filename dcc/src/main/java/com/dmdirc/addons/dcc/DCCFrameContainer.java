/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.dcc;

import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * This class links DCC objects to a window.
 */
public abstract class DCCFrameContainer extends FrameContainer {

    /** The Window we're using. */
    private boolean windowClosing = false;

    /**
     * Creates a new instance of DCCFrame.
     *
     * @param title               The title of this window
     * @param icon                The icon to use
     * @param configManager       Config manager
     * @param eventBus            The bus to dispatch events on.
     * @param components          The UI components that this frame requires
     */
    public DCCFrameContainer(
            final String title,
            final String icon,
            final AggregateConfigProvider configManager,
            final BackBufferFactory backBufferFactory,
            final EventBus eventBus,
            final Collection<String> components) {
        super(icon, title, title, configManager, backBufferFactory,
                eventBus,
                components);
        initBackBuffer();
    }

    @Override
    public Optional<Connection> getConnection() {
        return Optional.empty();
    }

    /**
     * Is the window closing?
     *
     * @return True if windowClosing has been called.
     */
    public boolean isWindowClosing() {
        return windowClosing;
    }

    @Override
    public void close() {
        windowClosing = true;

        super.close();
    }

}

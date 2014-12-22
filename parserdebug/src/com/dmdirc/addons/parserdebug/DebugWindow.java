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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.util.URLBuilder;

import java.util.Arrays;
import java.util.Optional;

/**
 * This class is used to show the parser debug in a window
 */
public class DebugWindow extends FrameContainer {

    /** The debug listener for this window. */
    protected final DebugInfoListener listener;
    /** The parser this window is debugging. */
    protected Parser parser;
    /** The connection we're operating on. */
    protected final Connection connection;

    /**
     * Creates a new instance of DebugWindow.
     */
    public DebugWindow(
            final DebugInfoListener listener,
            final String title,
            final Parser parser,
            final Connection connection,
            final URLBuilder urlBuilder,
            final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory) {
        super(connection.getWindowModel(), "raw", "Parser Debug", title,
                connection.getWindowModel().getConfigManager(), backBufferFactory,
                urlBuilder, eventBus, Arrays.asList(WindowComponent.TEXTAREA.getIdentifier()));
        this.listener = listener;
        this.parser = parser;
        this.connection = connection;
    }

    @Override
    public Optional<Connection> getOptionalConnection() {
        return Optional.of(connection);
    }

    /**
     * Set the parser to null to stop us holding onto parsers when the server connection is closed.
     */
    public void unsetParser() {
        addLine("======================", true);
        addLine("Unset parser: " + parser, true);
        parser = null;
    }

    /**
     * Closes this container (and its associated frame).
     */
    @Override
    public void close() {
        super.close();

        // Remove any callbacks or listeners
        if (parser != null) {
            parser.getCallbackManager().delCallback(DebugInfoListener.class, listener);
        }
    }

}

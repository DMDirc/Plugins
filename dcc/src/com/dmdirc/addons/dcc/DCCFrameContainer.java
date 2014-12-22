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

package com.dmdirc.addons.dcc;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.URLBuilder;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * This class links DCC objects to a window.
 */
public abstract class DCCFrameContainer extends FrameContainer {

    /** The Window we're using. */
    private boolean windowClosing = false;

    /**
     * Creates a new instance of DCCFrame.
     *
     * @param parent              The parent of this frame container, if any.
     * @param title               The title of this window
     * @param icon                The icon to use
     * @param configManager       Config manager
     * @param parser              Command parser to use for this window
     * @param messageSinkManager  The sink manager to use to dispatch messages.
     * @param tabCompleterFactory The factory to use to create tab completers.
     * @param urlBuilder          The URL builder to use when finding icons.
     * @param eventBus            The bus to dispatch events on.
     * @param components          The UI components that this frame requires
     */
    public DCCFrameContainer(
            @Nullable final FrameContainer parent,
            final String title,
            final String icon,
            final AggregateConfigProvider configManager,
            final BackBufferFactory backBufferFactory,
            final CommandParser parser,
            final MessageSinkManager messageSinkManager,
            final TabCompleterFactory tabCompleterFactory,
            final URLBuilder urlBuilder,
            final DMDircMBassador eventBus,
            final Collection<String> components) {
        super(parent, icon, title, title, configManager, backBufferFactory, urlBuilder, parser,
                tabCompleterFactory.getTabCompleter(configManager),
                messageSinkManager,
                eventBus,
                components);
    }

    @Override
    public int getMaxLineLength() {
        return 512;
    }

    @Override
    public Connection getConnection() { //NOPMD - server will always be null
        return null;
    }

    @Override
    public Optional<Connection> getOptionalConnection() {
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

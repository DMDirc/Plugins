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

package com.dmdirc.addons.redirect;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.Formatter;
import com.dmdirc.ui.messages.sink.MessageSinkManager;
import com.dmdirc.util.URLBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

/**
 * Implements a fake input window, which sends echoed text to the specified chat window instead.
 */
public class FakeWriteableFrameContainer extends FrameContainer {

    /** The target for this window. */
    private final FrameContainer target;

    /**
     * Creates a new instance of FakeInputWindow.
     */
    public FakeWriteableFrameContainer(
            final FrameContainer target,
            final MessageSinkManager messageSinkManager,
            final DMDircMBassador eventBus,
            final URLBuilder urlBuilder,
            final BackBufferFactory backBufferFactory) {
        super(target, target.getIcon(), target.getName(), target.getTitle(),
                target.getConfigManager(), backBufferFactory, urlBuilder,
                target.getCommandParser(), target.getTabCompleter(), messageSinkManager, eventBus,
                Collections.<String>emptyList());
        this.target = target;
    }

    @Override
    public void addLine(final String line, final boolean timestamp) {
        addLine(line);
    }

    @Override
    public void sendLine(final String line) {
        target.sendLine(line);
    }

    @Override
    public void addLine(final String type, final Date timestamp, final Object... args) {
        addLine(type, args);
    }

    @Override
    public void addLine(final String type, final Object... args) {
        sendLine(Formatter.formatMessage(getConfigManager(), type, args));
    }

    @Override
    public void addLine(final StringBuffer type, final Date timestamp, final Object... args) {
        addLine(type, args);
    }

    @Override
    public void addLine(final StringBuffer type, final Object... args) {
        addLine(type.toString(), args);
    }

    @Override
    public void addLine(final String line, final Date timestamp) {
        addLine(line);
    }

    @Override
    public int getMaxLineLength() {
        return target.getMaxLineLength();
    }

    @Override
    @Deprecated
    public Connection getConnection() {
        return target.getConnection();
    }

    @Override
    public Optional<Connection> getOptionalConnection() {
        return target.getOptionalConnection();
    }

}

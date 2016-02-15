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

package com.dmdirc.addons.redirect;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.Formatter;

import java.util.Collections;
import java.util.Optional;

/**
 * Implements a fake input window, which sends echoed text to the specified chat window instead.
 */
public class FakeWriteableFrameContainer extends FrameContainer {

    /** The target for this window. */
    private final WindowModel target;

    /**
     * Creates a new instance of FakeInputWindow.
     */
    public FakeWriteableFrameContainer(
            final WindowModel target,
            final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory) {
        super(target, target.getIcon(), target.getName(), target.getTitle(),
                target.getConfigManager(), backBufferFactory,
                target.getTabCompleter(), eventBus, Collections.<String>emptyList());
        this.target = target;
        initBackBuffer();
        setCommandParser(target.getCommandParser());
    }

    @Override
    public void sendLine(final String line) {
        target.sendLine(line);
    }

    @Override
    @Deprecated
    public void addLine(final String type, final Object... args) {
        sendLine(Formatter.formatMessage(getConfigManager(), type, args));
    }

    @Override
    public int getMaxLineLength() {
        return target.getMaxLineLength();
    }

    @Override
    public Optional<Connection> getConnection() {
        return target.getConnection();
    }

}

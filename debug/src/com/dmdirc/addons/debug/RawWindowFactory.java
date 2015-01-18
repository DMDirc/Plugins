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

package com.dmdirc.addons.debug;

import com.dmdirc.FrameContainer;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for raw windows.
 */
@Singleton
public class RawWindowFactory {

    private final TabCompleterFactory tabCompleterFactory;
    private final CommandController commandController;
    private final MessageSinkManager messageSinkManager;
    private final BackBufferFactory backBufferFactory;
    private final WindowManager windowManager;

    @Inject
    public RawWindowFactory(final TabCompleterFactory tabCompleterFactory,
            final CommandController commandController, final MessageSinkManager messageSinkManager,
            final BackBufferFactory backBufferFactory, final WindowManager windowManager) {
        this.tabCompleterFactory = tabCompleterFactory;
        this.commandController = commandController;
        this.messageSinkManager = messageSinkManager;
        this.backBufferFactory = backBufferFactory;
        this.windowManager = windowManager;
    }

    public RawWindow getRawWindow(final Connection connection) {
        final RawWindow rawWindow = new RawWindow(connection, commandController,
                messageSinkManager, tabCompleterFactory,  backBufferFactory);
        windowManager.addWindow((FrameContainer) connection.getWindowModel(), rawWindow);
        return rawWindow;
    }

}

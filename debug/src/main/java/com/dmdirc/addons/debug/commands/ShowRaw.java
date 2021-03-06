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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.addons.debug.RawWindowFactory;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Adds a raw window to a given server.
 */
public class ShowRaw extends DebugCommand {

    private final RawWindowFactory windowFactory;

    /**
     * Creates a new instance of the command.
     */
    @Inject
    public ShowRaw(final Provider<Debug> commandProvider, final RawWindowFactory windowFactory) {
        super(commandProvider);
        this.windowFactory = windowFactory;
    }

    @Override
    public String getName() {
        return "showraw";
    }

    @Override
    public String getUsage() {
        return " - Adds a raw window to the current server or parent server";
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final Optional<Connection> connection = origin.getConnection();
        if (connection.isPresent()) {
            windowFactory.getRawWindow(connection.get());
        } else {
            showError(origin, args.isSilent(), "Cannot show raw window here.");
        }
    }

}

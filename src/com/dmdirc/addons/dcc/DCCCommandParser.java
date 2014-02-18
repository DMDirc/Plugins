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

import com.dmdirc.FrameContainer;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

/**
 * DCC CommandParser.
 */
public class DCCCommandParser extends GlobalCommandParser {

    /** A version number for this class. */
    private static final long serialVersionUID = 2009290901;

    /**
     * Creates a new instance of the GlobalCommandParser.
     *
     * @param configManager     Config manager
     * @param commandController The controller to load commands and command info from.
     */
    public DCCCommandParser(
            final AggregateConfigProvider configManager,
            final CommandController commandController) {
        super(configManager, commandController);
    }

    /** Loads the relevant commands into the parser. */
    @Override
    protected void loadCommands() {
        getCommandManager().loadCommands(this, CommandType.TYPE_GLOBAL);
    }

    /**
     * Called when the input was a line of text that was not a command. This normally means it is
     * sent to the server/channel/user as-is, with no further processing.
     *
     * @param origin The window in which the command was typed
     * @param line   The line input by the user
     */
    @Override
    protected void handleNonCommand(final FrameContainer origin, final String line) {
        ((WritableFrameContainer) origin).sendLine(line);
    }

    /** {@inheritDoc} */
    @Override
    public void setOwner(final FrameContainer owner) {
        // Don't care
    }

}

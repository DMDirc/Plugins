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

package com.dmdirc.addons.dcc;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
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
     * @param eventBus          Event bus to post events on
     */
    public DCCCommandParser(
            final AggregateConfigProvider configManager,
            final CommandController commandController,
            final DMDircMBassador eventBus) {
        super(configManager, commandController, eventBus);
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
        origin.sendLine(line);
    }

    @Override
    public void setOwner(final FrameContainer owner) {
        // Don't care
    }

}

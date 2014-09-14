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

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.parser.interfaces.Parser;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The ParserDebug Command allows controlling of which parsers spam debug info.
 */
@CommandOptions(allowOffline = false)
public final class ParserDebugCommand extends Command {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo(
            "parserdebug", "parserdebug - Enables/Disables hooks for "
            + "onDebugInfo for the parser that owns this window",
            CommandType.TYPE_SERVER);
    /** Parser debug manager. */
    final ParserDebugManager parserDebugManager;

    /**
     * Creates a new instance of ParserDebugCommand.
     *
     * @param controller         The controller to use for command information.
     * @param parserDebugManager Parser debug manager
     */
    @Inject
    public ParserDebugCommand(
            final CommandController controller,
            final ParserDebugManager parserDebugManager) {
        super(controller);
        this.parserDebugManager = parserDebugManager;
    }

    /**
     * Executes this command.
     *
     * @param origin      The frame container in which this command was issued
     * @param commandArgs The user supplied arguments
     * @param context     The Context of this command execution
     */
    @Override
    public void execute(@Nonnull final FrameContainer origin, final CommandArguments commandArgs,
            final CommandContext context) {
        final boolean isSilent = commandArgs.isSilent();

        final Parser parser = ((ServerCommandContext) context).getConnection().getParser();

        if (parser == null) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Unable to get a parser for this window.");
            return;
        }
        if (parserDebugManager.containsParser(parser)) {
            if (parserDebugManager.removeParser(parser, false)) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Removing callback ok");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "Removing callback failed");
            }
        } else {
            if (parserDebugManager.addParser(parser, origin.getConnection())) {
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Adding callback ok");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "Adding callback failed");
            }
        }
    }

}

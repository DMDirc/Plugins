/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

package com.dmdirc.addons.logging;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.ui.input.AdditionalTabTargets;

import lombok.AllArgsConstructor;

/**
 * The dcop command retrieves information from a dcop application.
 */
@AllArgsConstructor
public final class LoggingCommand extends Command implements
        IntelligentCommand {

    /** Command name. */
    private static final String LOGGING = "logging";
    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo(LOGGING,
            "logging - <set|help> [parameters]",
            CommandType.TYPE_SERVER);
    /** Logging plugin. */
    private final LoggingPlugin plugin;

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length > 0) {
            if (args.getArguments()[0].equalsIgnoreCase("history")) {
                if (!plugin.showHistory(origin)) {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unable to open history for this window.");
                }
            } else if (args.getArguments()[0].equalsIgnoreCase("help")) {
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, LOGGING + " reload           - Reload the logging plugin.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, LOGGING + " history          - Open the history of this window, if available.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, LOGGING + " help             - Show this help.");
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unknown command '" + args.getArguments()[0] + "'. Use " + LOGGING + " help for a list of commands.");
            }
        } else {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Use " + LOGGING + " help for a list of commands.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        if (arg == 0) {
            res.add("reload");
            res.add("history");
            res.add("help");
            res.excludeAll();
        }
        return res;
    }
}


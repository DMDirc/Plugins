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

package com.dmdirc.addons.logging;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The logging command retrieves information from a dcop application.
 */
public class LoggingCommand extends BaseCommand implements IntelligentCommand {

    /** Command name. */
    private static final String LOGGING = "logging";
    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo(LOGGING,
            "logging <history|help> - view logging related information",
            CommandType.TYPE_SERVER);
    /** Logging manager. */
    private final LoggingManager manager;

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     * @param manager    The manager providing logging services.
     */
    @Inject
    public LoggingCommand(final CommandController controller, final LoggingManager manager) {
        super(controller);
        this.manager = manager;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length > 0) {
            if ("history".equalsIgnoreCase(args.getArguments()[0])) {
                if (!manager.showHistory(origin)) {
                    showError(origin, args.isSilent(), "Unable to open history for this window.");
                }
            } else if ("help".equalsIgnoreCase(args.getArguments()[0])) {
                showOutput(origin, args.isSilent(), LOGGING
                        + " history          - Open the history of this window, if available.");
                showOutput(origin, args.isSilent(), LOGGING
                        + " help             - Show this help.");
            } else {
                showError(origin, args.isSilent(), "Unknown command '"
                        + args.getArguments()[0] + "'. Use " + LOGGING
                        + " help for a list of commands.");
            }
        } else {
            showError(origin, args.isSilent(), "Use " + LOGGING + " help for a list of commands.");
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        if (arg == 0) {
            res.add("history");
            res.add("help");
            res.excludeAll();
        }
        return res;
    }

}

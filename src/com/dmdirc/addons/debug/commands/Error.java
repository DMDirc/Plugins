/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.input.AdditionalTabTargets;

/**
 * Creates DMDirc errors with the specified parameters.
 */
public class Error extends DebugCommand implements IntelligentCommand {

    /** Error level to create. */
    private ErrorLevel el = ErrorLevel.HIGH;

    /**
     * Creates a new instance of the command.
     *
     * @param command Parent command
     */
    public Error(final Debug command) {
        super(command);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "error";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return "<user|app> [<low|medium|high|fatal|unknown>] - Creates an error"
                + " with the specified parameters, defaults to high priority.";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        if ((args.getArguments().length == 1
                || args.getArguments().length == 2)
                && args.getArguments()[0].equals("user")) {
            Logger.userError(getLevel(args.getArguments()),
                    "Debug error message");
        } else if ((args.getArguments().length == 1
                || args.getArguments().length == 2)
                && args.getArguments()[0].equals("app")) {
            Logger.appError(getLevel(args.getArguments()),
                    "Debug error message", new IllegalArgumentException());
        } else {
            showUsage(origin, args.isSilent(), getName(), getUsage());
        }
    }

    /**
     * Returns the error level specified by the provided arguments.
     *
     * @param args command arguments
     *
     * @return Error level
     */
    private ErrorLevel getLevel(final String... args) {
        if (args.length >= 2) {
            final String level = args[1];
            if ("low".equals(level)) {
                el = ErrorLevel.LOW;
            } else if ("medium".equals(level)) {
                el = ErrorLevel.MEDIUM;
            } else if ("fatal".equals(level)) {
                el = ErrorLevel.FATAL;
            } else if ("unknown".equals(level)) {
                el = ErrorLevel.UNKNOWN;
            } else {
                el = ErrorLevel.HIGH;
            }
        } else {
            el = ErrorLevel.HIGH;
        }
        return el;
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        res.excludeAll();

        if (arg == 1) {
            res.add("user");
            res.add("app");
        } else if (arg == 2) {
            res.add("low");
            res.add("medium");
            res.add("high");
            res.add("fatal");
            res.add("unknown");
        }

        return res;
    }

}

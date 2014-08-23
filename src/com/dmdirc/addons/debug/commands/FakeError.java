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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.AppErrorEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.inject.Inject;
import javax.inject.Provider;

import net.engio.mbassy.bus.MBassador;

/**
 * Creates DMDirc errors with the specified parameters.
 */
public class FakeError extends DebugCommand implements IntelligentCommand {

    /** The event bus to post errors on . */
    private final MBassador eventBus;

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     * @param eventBus        The event bus to post errors on
     */
    @Inject
    public FakeError(final Provider<Debug> commandProvider, final MBassador eventBus) {
        super(commandProvider);
        this.eventBus = eventBus;
    }

    @Override
    public String getName() {
        return "error";
    }

    @Override
    public String getUsage() {
        return "<user|app> [<low|medium|high|fatal|unknown>] - Creates an error"
                + " with the specified parameters, defaults to high priority.";
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if ((args.getArguments().length == 1
                || args.getArguments().length == 2)
                && args.getArguments()[0].equals("user")) {
            eventBus.publishAsync(new UserErrorEvent(getLevel(args.getArguments()),
                    null, "Debug error message", ""));
        } else if ((args.getArguments().length == 1
                || args.getArguments().length == 2)
                && args.getArguments()[0].equals("app")) {
            eventBus.publishAsync(new AppErrorEvent(getLevel(args.getArguments()),
                    new IllegalArgumentException(), "Debug error message", ""));
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
            try {
                return ErrorLevel.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ErrorLevel.HIGH;
            }
        } else {
            return ErrorLevel.HIGH;
        }
    }

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

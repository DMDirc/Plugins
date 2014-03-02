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
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Times how long it takes to execute commands.
 */
public class Time extends DebugCommand implements IntelligentCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     */
    @Inject
    public Time(final Provider<Debug> commandProvider) {
        super(commandProvider);
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    public String getUsage() {
        return "<command to time> - times the specified command";
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        doTime(origin, args);
    }

    /**
     * Facilitates timing of a command.
     *
     * @param origin The origin of the command
     * @param window The window to be passed on to the timed command, if any
     * @param args   The arguments that were passed to the command
     */
    private void doTime(final FrameContainer origin,
            final CommandArguments args) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), getName(), getUsage());
            return;
        }

        if (origin instanceof WritableFrameContainer) {
            final WritableFrameContainer container = (WritableFrameContainer) origin;
            final long start = System.currentTimeMillis();
            container.getCommandParser().parseCommand(origin,
                    args.getArgumentsAsString(0));
            final long end = System.currentTimeMillis();
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "Command executed in " + (end - start) + " milliseconds.");
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        res.excludeAll();

        if (arg == 1) {
            res.include(TabCompletionType.COMMAND);
        }

        return res;
    }

}

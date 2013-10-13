/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.time;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.ui.input.AdditionalTabTargets;

import java.util.Map.Entry;
import java.util.Set;

/**
 * The timer command allows users to schedule commands to occur after a certain
 * interval, or to repeatedly occur with a specified delay.
 */
public class TimerCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("timer",
            "timer [--list|--cancel <timer id> | <repetitions> <interval> "
            + "<command>] - lists all active timers / cancells an active timer "
            + "of given ID / schedules a command to be executed after a certain "
            + "time",
            CommandType.TYPE_GLOBAL);
    /** The TimerManager for this TimerCommand. */
    private final TimerManager manager;

    /**
     * Creates a new instance of TimerCommand.
     *
     * @param manager The instance of TimerManager associated with this command
     * @param commandController The controller to use for command information.
     */
    public TimerCommand(final TimerManager manager, final CommandController commandController) {
        super(commandController);

        this.manager = manager;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {

        if (args.getArguments().length > 0) {
            switch (args.getArguments()[0]) {
                case "--cancel":
                    int timerKey = 0;
                    try {
                        timerKey = Integer.parseInt(args.getArgumentsAsString(1));
                    } catch (NumberFormatException ex) {
                        doUsage(origin, args.isSilent());
                        return;
                    }
                    if (manager.hasTimerWithID(timerKey)) {
                        manager.getTimerByID(timerKey).cancelTimer();
                        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Timer cancelled");
                    } else {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR, "There is currently"
                                + " no timer with that ID");
                        return;
                    }
                    break;
                case "--list":
                    final Set<Entry<Integer, TimedCommand>> timerList = manager.listTimers();
                    if (!timerList.isEmpty()) {
                        for (Entry<Integer, TimedCommand> entry : timerList) {
                            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Timer ID: "
                                    + entry.getKey() + " - " + entry.getValue().getCommand());
                        }
                    } else {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR, "There are " +
                                "currently no active timers");
                    }
                    break;
                default:
                    if (args.getArguments().length < 3) {
                        doUsage(origin, args.isSilent());
                    } else {
                        int repetitions = 0;
                        int interval = 0;

                        final String command = args.getArgumentsAsString(2);

                        try {
                            repetitions = Integer.parseInt(args.getArguments()[0]);
                            interval = Integer.parseInt(args.getArguments()[1]);
                        } catch (NumberFormatException ex) {
                            doUsage(origin, args.isSilent());
                            return;
                        }

                        if (interval < 1) {
                            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Cannot use" +
                                    " intervals below 1");
                            return;
                        }

                        manager.addTimer(repetitions, interval, command,
                                origin);

                        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Command scheduled.");
                    }   break;
            }
        } else {
            doUsage(origin, args.isSilent());
        }
    }

    /**
     * Displays usage information for this command.
     * @param origin The window that the command was entered in
     * @param isSilent Whether this command is being silenced or not
     */
    private void doUsage(final FrameContainer origin, final boolean isSilent) {
        showUsage(origin, isSilent, "timer", "[--list|--cancel <timer id> | " +
                "<repetitions> <interval> <command>]");
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();

        targets.excludeAll();
        if (arg == 0) {
            targets.add("--list");
            targets.add("--cancel");
        } else if (arg == 1 && "--cancel".equals(context.getPreviousArgs().get(0))) {
            for (Integer i : manager.getTimerIDs()) {
                targets.add(i.toString());
            }
        }

        return targets;
    }
}

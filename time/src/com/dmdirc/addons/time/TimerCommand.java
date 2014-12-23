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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The timer command allows users to schedule commands to occur after a certain interval, or to
 * repeatedly occur with a specified delay.
 */
public class TimerCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("timer",
            "timer [--list|--cancel <timer id> | <repetitions> <interval> "
            + "<command>] - lists all active timers / cancels an active timer "
            + "of given ID / schedules a command to be executed after a certain "
            + "time",
            CommandType.TYPE_GLOBAL);
    /** The TimerManager for this TimerCommand. */
    private final TimerManager manager;

    /**
     * Creates a new instance of TimerCommand.
     *
     * @param manager           The instance of TimerManager associated with this command
     * @param commandController The controller to use for command information.
     */
    @Inject
    public TimerCommand(final TimerManager manager, final CommandController commandController) {
        super(commandController);

        this.manager = manager;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {

        if (args.getArguments().length > 0) {
            switch (args.getArguments()[0]) {
                case "--cancel":
                    doCancel(origin, args.isSilent(), args.getArgumentsAsString(1));
                    break;
                case "--list":
                    doList(origin, args.isSilent());
                    break;
                default:
                    if (args.getArguments().length < 3) {
                        doUsage(origin, args.isSilent());
                    } else {
                        doCommand(origin, args);
                    }
                    break;
            }
        } else {
            doUsage(origin, args.isSilent());
        }
    }

    private void doCommand(final FrameContainer origin, final CommandArguments args) {
        final int repetitions;
        final int interval;

        final String command = args.getArgumentsAsString(2);

        try {
            repetitions = Integer.parseInt(args.getArguments()[0]);
            interval = Integer.parseInt(args.getArguments()[1]);
        } catch (NumberFormatException ex) {
            doUsage(origin, args.isSilent());
            return;
        }

        if (interval < 1) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Cannot use intervals below 1");
            return;
        }

        manager.addTimer(repetitions, interval, command, origin);

        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Command scheduled.");
    }

    private void doCancel(final FrameContainer origin, final boolean isSilent, final String arg) {
        final int timerKey;
        try {
            timerKey = Integer.parseInt(arg);
        } catch (NumberFormatException ex) {
            doUsage(origin, isSilent);
            return;
        }
        if (manager.hasTimerWithID(timerKey)) {
            manager.getTimerByID(timerKey).cancelTimer();
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Timer cancelled");
        } else {
            sendLine(origin, isSilent, FORMAT_ERROR, "There is currently no timer with that ID");
        }
    }

    private void doList(final FrameContainer origin, final boolean isSilent) {
        final Set<Entry<Integer, TimedCommand>> timerList = manager.listTimers();
        if (timerList.isEmpty()) {
            sendLine(origin, isSilent, FORMAT_ERROR, "There are " + "currently no active timers");
        } else {
            for (Entry<Integer, TimedCommand> entry : timerList) {
                sendLine(origin, isSilent, FORMAT_OUTPUT,
                        "Timer ID: " + entry.getKey() + " - " + entry.getValue().getCommand());
            }
        }
    }

    /**
     * Displays usage information for this command.
     *
     * @param origin   The window that the command was entered in
     * @param isSilent Whether this command is being silenced or not
     */
    private void doUsage(final FrameContainer origin, final boolean isSilent) {
        showUsage(origin, isSilent, "timer", "[--list|--cancel <timer id> | "
                + "<repetitions> <interval> <command>]");
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets targets = new AdditionalTabTargets();

        targets.excludeAll();
        if (arg == 0) {
            targets.add("--list");
            targets.add("--cancel");
        } else if (arg == 1 && "--cancel".equals(context.getPreviousArgs().get(0))) {
            targets.addAll(manager.getTimerIDs().stream()
                    .map(Object::toString).collect(Collectors.toList()));
        }

        return targets;
    }

}

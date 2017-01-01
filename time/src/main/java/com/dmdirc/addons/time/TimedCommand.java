/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.time;

import com.dmdirc.interfaces.WindowModel;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Timed command represents a command that has been scheduled by the user.
 */
public class TimedCommand extends TimerTask {

    /** The number of repetitions remaining. */
    private int repetitions;
    /** The command to execute. */
    private final String command;
    /** The container to use for executing commands. */
    private final WindowModel origin;
    /** The number of seconds between each execution. */
    private final int delay;
    /** The key for this timer in the Timer Manager. */
    private final int timerKey;
    /** The manager for this timer. */
    private final TimerManager manager;
    /** The timer we're using for scheduling this command. */
    private Timer timer;

    /**
     * Creates a new instance of TimedCommand.
     *
     * @param manager           The manager that is controlling this command.
     * @param timerKey          The key for this timer in the Timer Manager.
     * @param repetitions       The number of times this command will be executed
     * @param delay             The number of seconds between each execution
     * @param command           The command to be executed
     * @param origin            The frame container to use for the execution
     */
    public TimedCommand(
            final TimerManager manager,
            final int timerKey,
            final int repetitions,
            final int delay,
            final String command,
            final WindowModel origin) {
        this.timerKey = timerKey;
        this.repetitions = repetitions;
        this.command = command;
        this.origin = origin;
        this.manager = manager;
        this.delay = delay;
    }

    public void schedule(final TimerFactory timerFactory) {
        timer = timerFactory.getTimer("Timed Command Timer");
        timer.schedule(this, delay * 1000L, delay * 1000L);
    }

    /**
     * Returns the command this timer is due to execute.
     *
     * @return Command the timer will run
     */
    public String getCommand() {
        return command;
    }

    /**
     * Cancels this timer and removes it from the Timer Manager
     */
    public void cancelTimer() {
        manager.removeTimer(timerKey);
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void run() {
        if (timer == null) {
            return;
        }
        origin.getInputModel().get().getCommandParser().parseCommand(origin, command);

        if (--repetitions <= 0) {
            manager.removeTimer(timerKey);
            timer.cancel();
        }
    }

}

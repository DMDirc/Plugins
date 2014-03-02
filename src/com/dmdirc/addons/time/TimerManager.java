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
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.CommandController;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

/**
 * Class to manage Timers.
 */
public class TimerManager {

    /** Map of all the timers that are running. */
    private final Map<Integer, TimedCommand> timerList = new HashMap<>();
    /** The command controller to use when executing global commands. */
    private final CommandController commandController;
    /** Action controller. */
    private final ActionController actionController;
    /** Have we registered our types already? */
    private static boolean registered;
    /** The timer to use for scheduling. */
    private Timer timer;

    @Inject
    public TimerManager(final CommandController commandController,
            final ActionController actionController) {
        this.commandController = commandController;
        this.actionController = actionController;
    }

    public void load() {
        if (!registered) {
            actionController.registerTypes(TimeActionType.values());
            registered = true;
        }

        final int offset = 60 - Calendar.getInstance().get(Calendar.SECOND);

        timer = new Timer("Time plugin timer");

        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                runTimer();
            }
        }, 1000 * offset, 1000 * 60);
    }

    public void unload() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Adds a timer to the internal list and starts the timer.
     *
     * @param repetitions Amount of times the timer repeats
     * @param interval    Interval between repetitions
     * @param command     Command to be run when the timer fires
     * @param origin      The frame container to use for the execution
     */
    public void addTimer(final int repetitions, final int interval,
            final String command, final FrameContainer origin) {

        synchronized (this) {
            final int timerKey = findFreeKey();
            timerList.put(timerKey, new TimedCommand(this, commandController, timerKey,
                    repetitions, interval, command, origin));
        }
    }

    /**
     * Removes a timer from our internal list of active timers. This should only be called when a
     * timer is cancelled.
     *
     * @param timerKey Key of the timer to remove
     */
    public void removeTimer(final int timerKey) {
        timerList.remove(timerKey);
    }

    /**
     * Gets a list of all current active timers.
     *
     * @return Returns a set of keys for all active timers
     */
    public Set<Entry<Integer, TimedCommand>> listTimers() {
        return timerList.entrySet();
    }

    /**
     * Returns a list of all known Timer IDs.
     *
     * @return returns an Integer Set of Timer IDs
     */
    public Set<Integer> getTimerIDs() {
        return timerList.keySet();
    }

    /**
     * Locates a key in our list that is currently not being used by a timer.
     *
     * @return Returns a key that can be used for creating a new timer
     */
    private int findFreeKey() {
        int i = 0;
        while (timerList.containsKey(i)) {
            i++;
        }
        return i;
    }

    /**
     * Matches a Timer ID to a Timer. This will return null if there is no Timer with the provided
     * ID.
     *
     * @param id ID that we want the timer for
     *
     * @return Returns the Timer for the ID provided or null if none exist
     */
    public TimedCommand getTimerByID(final int id) {
        return timerList.get(id);
    }

    /**
     * Checkes if there is a timer with the provided ID.
     *
     * @param id ID that we want to check if there is a timer for
     *
     * @return Returns True iif there is a timer with the ID
     */
    public boolean hasTimerWithID(final int id) {
        return timerList.containsKey(id);
    }

    /** Handles a timer event that occurs every minute. */
    public void runTimer() {
        final Calendar cal = Calendar.getInstance();

        actionController.triggerEvent(TimeActionType.TIME_MINUTE, null, cal);

        if (cal.get(Calendar.MINUTE) == 0) {
            actionController.triggerEvent(TimeActionType.TIME_HOUR, null, cal);

            if (cal.get(Calendar.HOUR_OF_DAY) == 0) {
                actionController.triggerEvent(TimeActionType.TIME_DAY, null, cal);
            }
        }
    }

}

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

package com.dmdirc.addons.time;

import com.dmdirc.interfaces.WindowModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

/**
 * Class to manage Timers.
 */
public class TimerManager {

    /** Map of all the timers that are running. */
    private final Map<Integer, TimedCommand> timerList = new HashMap<>();
    /** The timer factory to create timers with. */
    private final TimerFactory timerFactory;

    @Inject
    public TimerManager(final TimerFactory timerFactory) {
        this.timerFactory = timerFactory;
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
            final String command, final WindowModel origin) {

        synchronized (this) {
            final int timerKey = findFreeKey();
            final TimedCommand timedCommand = new TimedCommand(this, timerKey,
                    repetitions, interval, command, origin);
            timerList.put(timerKey, timedCommand);
            timedCommand.schedule(timerFactory);
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

}

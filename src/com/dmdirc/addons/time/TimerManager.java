/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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
import com.dmdirc.ui.interfaces.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class to manage Timers.
 *
 * @author Simon Mott
 * @since 0.6.5
 */
public class TimerManager {

    /** Map of all the timers that are running. */
    private final Map<Integer, TimedCommand> timerList;

    /** Creates a new instance of TimeManager. */
    public TimerManager() {
        timerList = new HashMap<Integer, TimedCommand>();
    }

    /**
     * Adds a timer to the internal list and starts the timer.
     *
     * @param repetitions Amount of times the timer repeats
     * @param interval Interval between repetitions
     * @param command Command to be run when the timer fires
     * @param origin The frame container to use for the execution
     * @param commandContext The window the command came from
     */
    public void addTimer(int repetitions, int interval, String command,
            FrameContainer<?> origin, Window commandContext) {

        int timerKey = findFreeKey();
        timerList.put(timerKey, new TimedCommand(this, timerKey, repetitions,
                interval, command, origin, commandContext));
    }

    /**
     * Removes a timer from our internal list of active timers. This should only
     * be called when a timer is cancelled.
     *
     * @param timerKey Key of the timer to remove
     */
    public void removeTimer(int timerKey) {
        timerList.remove(timerKey);
    }

    /**
     * Gets a list of all current active timers.
     *
     * @return Returns a set of keys for all active timers
     */
    public Set<Integer> listTimers() {
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
     * Matches a Key to a Timer.
     *
     * @param key Key that we want the timer for
     *
     * @return Returns the Timer for the key provided
     */
    public TimedCommand getTimerByKey(int key) {
        return timerList.get(key);
    }
}
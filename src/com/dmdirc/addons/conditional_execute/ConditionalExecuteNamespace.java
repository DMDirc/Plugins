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

package com.dmdirc.addons.conditional_execute;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Class representing a ConditionalExecute Namespace.
 */
public class ConditionalExecuteNamespace {
    /** Name of this namespace. */
    private final String name;

    /** Are we currently inhibited? */
    private boolean inhibited = false;

    /** Are we currently forced? */
    private boolean forced = false;

    /** What is the current limit time? */
    private long limitTime = 0L;

    /** Create a new ConditionalExecuteNamespace. */
    public ConditionalExecuteNamespace(final String name) {
        this.name = name;
    }

    /**
     * Can a command in this namespace be run?
     *
     * This checks as follows:
     *   - Is the namespace inhibited? - return False
     *   - Is the namespace in "force" mode?  - return True
     *   - If inverse is true, are we under the limit time? return True
     *   - If inverse is false, are we over the limit time? return True
     *   - return False
     *
     * @param inverse Check if we are *before* the limit time rather than after?
     * @return True if the command can be run.
     */
    public boolean canRun(final boolean inverse) {
        if (isInhibited()) {
            return false;
        } else if (isForced()) {
            return true;
        } else if (inverse && System.currentTimeMillis() < limitTime) {
            return true;
        } else if (!inverse && System.currentTimeMillis() > limitTime) {
            return true;
        } else {
            return false;
        }
    }

    /** Inhibit this namespace. */
    public void inhibit() {
        this.inhibited = true;
        this.forced = false;
    }

    /** Force this namespace. */
    public void force() {
        this.inhibited = false;
        this.forced = true;
    }

    /** UnForce and Uninhibit this namespace. */
    public void allow() {
        this.inhibited = false;
        this.forced = false;
    }

    /**
     * Change the limit time on this namespace.
     *
     * @param difference Change (in milliseconds) to make to the limit time.
     * @return New limit time.
     */
    public long changeLimit(final long difference) {
        this.limitTime += difference;
        return this.limitTime;
    }

    /**
     * Set the limit time on this namespace.
     *
     * @param time new Limit time (in milliseconds)
     * @return New limit time.
     */
    public long setLimit(final long time) {
        this.limitTime = time;
        return this.limitTime;
    }

    /**
     * Are we currently forced?
     *
     * @return Are we currently forced?
     */
    public boolean isForced() {
        return forced;
    }

    /**
     * Are we currently inhibited?
     *
     * @return Are we currently inhibited?
     */
    public boolean isInhibited() {
        return inhibited;
    }

    /**
     * Current limit time.
     *
     * @return The current limit time.
     */
    public long getLimitTime() {
        return limitTime;
    }

    /**
     * Get the name of this namespace.
     *
     * @return The name of this namespace.
     */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getName());
        if (isForced()) {
          sb.append(" - Forced");
        } else if (isInhibited()) {
          sb.append(" - Inhibited");
        } else {
          sb.append(" - Limit Time: ");
          final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          sb.append(dateFormat.format(limitTime));
        }
        if (canRun(false)) {
            sb.append(" [Can Run]");
        }
        if (canRun(true)) {
            sb.append(" [Can Run Inverse]");
        }
        return sb.toString();
    }
}

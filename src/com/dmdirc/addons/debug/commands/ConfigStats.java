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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.ConfigManager;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Outputs usage statistics for config settings.
 */
public class ConfigStats extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     */
    @Inject
    public ConfigStats(final Provider<Debug> commandProvider) {
        super(commandProvider);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "configstats";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return "[+<X>|<Y>|<regex>] - show config usage stats, optionally "
                + "filtering to the top X entries, entries with at least Y "
                + "usages, or entries matching the specified regex";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 2) {
            if (args.getArguments()[1].startsWith("+")) {
                int arg;
                try {
                    arg = Integer.parseInt(args.getArguments()[1].substring(1));
                } catch (NumberFormatException e) {
                    arg = Integer.MAX_VALUE;
                }
                doConfigStatsTop(origin, args.isSilent(), arg);
            } else if (args.getArguments()[1].matches("^[0-9]+$")) {
                int arg;
                try {
                    arg = Integer.parseInt(args.getArguments()[1]);
                } catch (NumberFormatException e) {
                    arg = -1;
                }
                doConfigStatsCutOff(origin, args.isSilent(), arg);
            } else {
                doConfigStatsOption(origin, args.isSilent(),
                        args.getArguments()[1]);
            }
        } else {
            doConfigStatsCutOff(origin, args.isSilent(), -1);
        }
    }



    /**
     * Shows stats related to the config system, showing any options matching
     * the regex.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     * @param regex Regex to match options against
     */
    private void doConfigStatsOption(final FrameContainer origin,
            final boolean isSilent, final String regex) {
        final SortedSet<Entry<String, Integer>> sortedStats = getSortedStats();
        boolean found = false;
        for (Map.Entry<String, Integer> entry : sortedStats) {
            if (entry.getKey().matches(regex)) {
                sendLine(origin, isSilent, FORMAT_OUTPUT,
                        entry.getKey() + " - " + entry.getValue());
                found = true;
            }
        }
        if (!found) {
            sendLine(origin, isSilent, FORMAT_ERROR, "Unable to locate option.");
        }
    }

    /**
     * Shows stats related to the config system, listing the top X number of
     * options.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     * @param top Top number of entries to show
     */
    private void doConfigStatsTop(final FrameContainer origin,
            final boolean isSilent, final int top) {
        final SortedSet<Entry<String, Integer>> sortedStats = getSortedStats();
        int i = 0;
        for (Map.Entry<String, Integer> entry : sortedStats) {
            if (i == top) {
                break;
            }
            i++;
            sendLine(origin, isSilent, FORMAT_OUTPUT, entry.getKey() + " - " + entry.getValue());
        }
    }

    /**
     * Shows stats related to the config system, lists all values with number
     * of usages over the specified value.
     *
     * @param origin The window this command was executed in
     * @param isSilent Whether this command has been silenced or not
     * @param cutoff Cut off value for stats
     */
    private void doConfigStatsCutOff(final FrameContainer origin,
            final boolean isSilent, final int cutoff) {
        final SortedSet<Entry<String, Integer>> sortedStats = getSortedStats();
        for (Map.Entry<String, Integer> entry : sortedStats) {
            if (entry.getValue() <= cutoff) {
                break;
            }
            sendLine(origin, isSilent, FORMAT_OUTPUT, entry.getKey() + " - " + entry.getValue());
        }
    }

    /**
     * Gets a sorted Set of config options and the number of times they have
     * been called.
     *
     * @return Sorted set of config options and usages
     */
    private static SortedSet<Entry<String, Integer>> getSortedStats() {
        final SortedSet<Entry<String, Integer>> sortedStats = new TreeSet<>(new ValueComparator());
        sortedStats.addAll(ConfigManager.getStats().entrySet());
        return sortedStats;
    }

    /** Reverse value comparator for a map entry. */
    private static class ValueComparator implements Comparator<Entry<String, Integer>>, Serializable {

        /**
         * A version number for this class. It should be changed whenever the
         * class structure is changed (or anything else that would prevent
         * serialized objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 1;

        /** {@inheritDoc} */
        @Override
        public int compare(final Entry<String, Integer> o1, final Entry<String, Integer> o2) {
            int returnValue = o1.getValue().compareTo(o2.getValue()) * -1;

            if (returnValue == 0) {
                returnValue = o1.getKey().compareToIgnoreCase(o2.getKey());
            }

            return returnValue;
        }
    }

}

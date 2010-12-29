/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.ui.messages.Styliser;
import java.util.Map.Entry;

/**
 * Outputs a thread dump.
 */
public class Threads extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param command Parent command
     */
    public Threads(final Debug command) {
        super(command);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "threads";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return " - Shows a thread dump for the running JVM";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        for (Entry<Thread, StackTraceElement[]> thread
                : Thread.getAllStackTraces().entrySet()) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, Styliser.CODE_BOLD
                    + thread.getKey().getName());

            for (StackTraceElement element : thread.getValue()) {
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        Styliser.CODE_FIXED + "    " + element.toString());
            }
        }
    }

}

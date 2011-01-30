/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes,
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

package com.dmdirc.addons.exec;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.StreamReader;
import com.dmdirc.util.CommandUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * A command which allows users execute scripts.
 */
public class ExecCommand extends Command {

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        try {
            if (args.getArguments().length == 0) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Could not execute: No command given");
            } else if (!new File(args.getArguments()[0]).exists()) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                       "Could not execute: " + args.getArguments()[0]
                       + " does not exist.");
            } else {
                final Process p = Runtime.getRuntime().exec(
                        CommandUtils.parseArguments(args.getArgumentsAsString(
                        0)));
                if (!args.isSilent()) {
                    final List<String> execOutput = new LinkedList<String>();
                    final StreamReader reader = new StreamReader(
                            p.getInputStream(), execOutput);
                    reader.run();
                    for (String line : execOutput) {
                        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, line);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to run application: "
                    + ex.getMessage(), ex);
        }
    }
}

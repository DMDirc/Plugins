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
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
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

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("exec",
            "exec <command> [<parameters>] - executes an external program "
            + "and displays the output", CommandType.TYPE_GLOBAL);

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final String[] commandArray = CommandUtils.parseArguments(
                args.getArgumentsAsString());

        try {
            // This checks the command to execute has correct quotes
            // (if necessary). Without this /exec "command arg1 arg2 would error.
            if (commandArray.length == 0) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                       "Could not execute: Invalid file name provided");
            } else if (!new File(commandArray[0]).exists()) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                       "Could not execute: " + commandArray[0] + " does not exist.");
            } else {
                final Process p = Runtime.getRuntime().exec(commandArray);
                final List<String> execOutput = args.isSilent()
                        ? null : new LinkedList<String>();
                final List<String> errorOutput = args.isSilent()
                        ? null : new LinkedList<String>();
                final StreamReader inputReader = new StreamReader(
                            p.getInputStream(), execOutput);
                final StreamReader errorReader = new StreamReader(
                            p.getErrorStream(), errorOutput);

                inputReader.run();
                errorReader.run();
                if (!args.isSilent()) {
                    for (String line : execOutput) {
                        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, line);
                    }
                    for (String line : errorOutput) {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR, line);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to run application: "
                    + ex.getMessage(), ex);
        }
    }
}

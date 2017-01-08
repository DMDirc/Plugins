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

package com.dmdirc.addons.exec;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.util.CommandUtils;
import com.dmdirc.util.LogUtils;
import com.dmdirc.util.io.StreamUtils;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * A command which allows users execute scripts.
 */
public class ExecCommand extends BaseCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ExecCommand.class);
    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("exec",
            "exec <command> [<parameters>] - executes an external program "
            + "and displays the output", CommandType.TYPE_GLOBAL);

    @Inject
    public ExecCommand(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        final String[] commandArray = CommandUtils.parseArguments(
                args.getArgumentsAsString());

        try {
            // This checks the command to execute has correct quotes
            // (if necessary). Without this /exec "command arg1 arg2 would error.
            if (commandArray.length == 0) {
                showError(origin, args.isSilent(),
                        "Could not execute: Invalid file name provided");
            } else if (!new File(commandArray[0]).exists()) {
                showError(origin, args.isSilent(),
                        "Could not execute: " + commandArray[0] + " does not exist.");
            } else {
                final Process p = Runtime.getRuntime().exec(commandArray);
                if (args.isSilent()) {
                    StreamUtils.readStream(p.getInputStream());
                    StreamUtils.readStream(p.getErrorStream());
                } else {
                    final List<String> execOutput = CharStreams.readLines(
                            new InputStreamReader(p.getInputStream()));
                    final List<String> errorOutput = CharStreams.readLines(
                            new InputStreamReader(p.getErrorStream()));
                    for (String line : execOutput) {
                        showOutput(origin, args.isSilent(), line);
                    }
                    for (String line : errorOutput) {
                        showError(origin, args.isSilent(), line);
                    }
                }
            }
        } catch (IOException ex) {
            LOG.info(LogUtils.USER_ERROR, "Unable to run application: {}", ex.getMessage(), ex);
        }
    }

}

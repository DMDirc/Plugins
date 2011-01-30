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
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * A command which allows users execute scripts.
 */
public class ExecCommand extends Command implements CommandInfo {

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {

        int offset = 0;
        boolean isSilent = false;

        if (args.getArguments().length > 0 && args.getArguments()[0]
                .equals("--silent")) {
            isSilent = true;
            offset++;
        }
        final String path = args.getArgumentsAsString(offset, offset);

        try {
            if (args.getArguments().length == 0) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Please provide a valid command to execute.");
            } else if (!(new File(path).exists())) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                       path + " does not exist.");
            } else {
                Process p = Runtime.getRuntime().exec(parseArguments(
                        args.getArgumentsAsString(offset)));
                if (!isSilent) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            p.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, line);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.MEDIUM, ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "exec";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_GLOBAL;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "exec [--silent] - Executes a command and displays the output";
    }

    /**
     * Parses the specified command into an array of arguments. Arguments are
     * separated by spaces. Multi-word arguments may be specified by starting
     * the argument with a quote (") and finishing it with a quote (").
     *
     * @param command The command to parse
     * @return An array of arguments corresponding to the command
     */
    protected static String[] parseArguments(final String command) {
        final List<String> args = new ArrayList<String>();
        final StringBuilder builder = new StringBuilder();
        boolean inquote = false;

        for (String word : command.split(" ")) {
            if (word.endsWith("\"") && inquote) {
                args.add(builder.toString() + ' ' + word.substring(0, word.length() - 1));
                builder.delete(0, builder.length());
                inquote = false;
            } else if (inquote) {
                if (builder.length() > 0) {
                    builder.append(' ');
                }

                builder.append(word);
            } else if (word.startsWith("\"") && !word.endsWith("\"")) {
                inquote = true;
                builder.append(word.substring(1));
            } else if (word.startsWith("\"") && word.endsWith("\"")) {
                if (word.length() == 1) {
                    inquote = true;
                } else {
                    args.add(word.substring(1, word.length() - 1));
                }
            } else {
                args.add(word);
            }
        }

        return args.toArray(new String[args.size()]);
    }
}

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

package com.dmdirc.addons.conditional_execute;

import com.dmdirc.FrameContainer;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * The ConditionalExecute command allows the user to conditionally execute a command based on
 * external and pre-determined conditions.
 */
public class ConditionalExecuteCommand extends Command {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("conditionalexecute",
            "conditionalexecute <args> - Conditionally execute a command", CommandType.TYPE_GLOBAL);
    /** Store details about current namespaces. */
    private final Map<String, ConditionalExecuteNamespace> namespaces = new HashMap<>();

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public ConditionalExecuteCommand(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(final FrameContainer origin, final CommandArguments args,
            final CommandContext context) {
        final String cmdname = args.getWordsAsString(0, 0);

        ConditionalExecuteNamespace namespace = null;
        final String[] arguments = args.getArguments();
        boolean manipulated = false;
        boolean inverse = false;

        for (int i = 0; i < arguments.length; i++) {
            final String arg = arguments[i].toLowerCase();
            final String nextArg = i + 1 < arguments.length ? arguments[i + 1] : "";

            if (arg.equalsIgnoreCase("--help")) {
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Usage:");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, cmdname + " <args>");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, cmdname
                        + " --namespace <name> <namespace commands>");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, cmdname
                        + " --namespace <name> [--inverse] </commandToRun <command args>>");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "Commands can only be specified if no other non-namespace args are given.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "When trying to run a command, the namespace will be checked to see if the command can be run.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "The checks performed are as follows:");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "   1) Does the namespace exist? if not, run the command and create the namespace.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "   2) Is the namespace inhibited? - Do not run the command.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "   3) Is the namespace in forced mode? - Run the command.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "   4) If --inverse is specified, are we under the limit time? Run the command");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "   5) If --inverse is not specified, are we over the limit time? Run the command");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "   6) Do not run the command.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "General Arguments.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --list                   - List all current namespaces and their status");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --help                   - Print this help.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --reset                  - Remove all namespaces.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Useful things:");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --namespace <name>       - Namespace to modify. If the namespace does not exist, it will be created. Namespaces are not remembered across sessions.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Arguments related to a namespace:");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --settime <time>         - Set the limit time on this namespace. Time can be either a time in seconds, 'now' for now, or 'nowifless' to set to now only if it is currently less.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --delay <seconds>        - Increase the 'limit' time on this namespace by <seconds> seconds");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --inhibit                - Prevent any attempts at running commands in this namespace from executing");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --force                  - Any future attempts at running commands in this namespace will always execute");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --allow                  - Disable '--force' or '--inhibit' and resume normal operation.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --remove                 - Remove this namespace.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --status                 - Show the status of this namespace.");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Arguments when running a command:");
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                        "  --inverse              - Inverse the match against the 'limit' time.");
                return;
            } else if (arg.equalsIgnoreCase("--list")) {
                if (namespaces.isEmpty()) {
                    sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                            "There are currently no known namespaces.");
                } else {
                    sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Current namespaces: ");
                    for (final Map.Entry<String, ConditionalExecuteNamespace> e : namespaces.
                            entrySet()) {
                        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "    " + e.getValue().
                                toString());
                    }
                }
                return;
            } else if (arg.equalsIgnoreCase("--reset")) {
                namespaces.clear();
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "All namespaces removed.");
                return;
            } else if (namespace == null) {
                if (arg.equalsIgnoreCase("--namespace")) {
                    if (nextArg.isEmpty()) {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR,
                                "Error: You must specify a namespace.");
                        return;
                    } else {
                        if (!namespaces.containsKey(nextArg.toLowerCase())) {
                            namespaces.put(nextArg.toLowerCase(), new ConditionalExecuteNamespace(
                                    nextArg.toLowerCase()));
                        }
                        namespace = namespaces.get(nextArg.toLowerCase());

                        // Skip the next argument.
                        i++;
                    }
                } else {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR,
                            "Error: You must specify a namespace first.");
                    return;
                }
            } else if (arg.equalsIgnoreCase("--inhibit")) {
                namespace.inhibit();
                manipulated = true;
            } else if (arg.equalsIgnoreCase("--force")) {
                namespace.force();
                manipulated = true;
            } else if (arg.equalsIgnoreCase("--allow")) {
                namespace.reset();
                manipulated = true;
            } else if (arg.equalsIgnoreCase("--settime")) {
                if (nextArg.isEmpty()) {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR,
                            "Error: You must provide a time to use.");
                    return;
                } else if (nextArg.equalsIgnoreCase("now")) {
                    namespace.setLimit(System.currentTimeMillis());
                    i++;
                    manipulated = true;
                } else if (nextArg.equalsIgnoreCase("nowifless")) {
                    if (namespace.getLimitTime() < System.currentTimeMillis()) {
                        namespace.setLimit(System.currentTimeMillis());
                    }
                    i++;
                    manipulated = true;
                } else {
                    try {
                        namespace.setLimit(Long.parseLong(nextArg) * 1000);
                        i++;
                        manipulated = true;
                    } catch (final NumberFormatException nfe) {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR, "Error: Invalid time: "
                                + nextArg);
                        return;
                    }
                }
            } else if (arg.equalsIgnoreCase("--delay")) {
                if (nextArg.isEmpty()) {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR,
                            "Error: You must provide a delay to use.");
                    return;
                } else {
                    try {
                        namespace.changeLimit(Long.parseLong(nextArg) * 1000);
                        i++;
                        manipulated = true;
                    } catch (final NumberFormatException nfe) {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR, "Error: Invalid delay: "
                                + nextArg);
                        return;
                    }
                }
            } else if (arg.equalsIgnoreCase("--remove")) {
                namespaces.remove(namespace.getName());
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Removed namespace '" + namespace.
                        getName() + "'");
                return;
            } else if (arg.equalsIgnoreCase("--status")) {
                // Show the current status, incase some manipulations occured prior to this.
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, namespaces.get(namespace.getName()));
                return;
            } else if (arg.equalsIgnoreCase("--inverse")) {
                inverse = true;
            } else if (manipulated) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "You can't run commands and manipulate the namespace at the same time, ignored.");
            } else {
                // Command to run!
                if (namespace.canRun(inverse) && origin instanceof WritableFrameContainer) {
                    ((WritableFrameContainer) origin).getCommandParser().parseCommand(origin, args.
                            getArgumentsAsString(i++));
                }
                return;
            }
        }

        // If we get here, we either manipulated something, or should show the usage text.
        if (manipulated) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Namespace updated.");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, namespace.toString());
            namespaces.put(namespace.getName(), namespace);
        } else {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Usage:");
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "");
            sendLine(origin, args.isSilent(), FORMAT_ERROR, cmdname + " <args>");
            sendLine(origin, args.isSilent(), FORMAT_ERROR, cmdname
                    + " --namespace <name> <namespace commands>");
            sendLine(origin, args.isSilent(), FORMAT_ERROR, cmdname
                    + " --namespace <name> [--inverse] </commandToRun <command args>>");
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "");
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "For more information, see " + cmdname
                    + " --help");
        }
    }

}

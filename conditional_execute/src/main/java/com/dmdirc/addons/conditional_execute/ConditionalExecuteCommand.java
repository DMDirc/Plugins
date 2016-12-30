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

package com.dmdirc.addons.conditional_execute;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.InputModel;
import com.dmdirc.interfaces.WindowModel;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * The ConditionalExecute command allows the user to conditionally execute a command based on
 * external and pre-determined conditions.
 */
public class ConditionalExecuteCommand extends BaseCommand {

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
    public void execute(@Nonnull final WindowModel origin, final CommandArguments args,
            final CommandContext context) {
        final String cmdname = args.getWordsAsString(0, 0);

        ConditionalExecuteNamespace namespace = null;
        final String[] arguments = args.getArguments();
        boolean manipulated = false;
        boolean inverse = false;

        for (int i = 0; i < arguments.length; i++) {
            final String arg = arguments[i].toLowerCase();
            final String nextArg = i + 1 < arguments.length ? arguments[i + 1] : "";

            if ("--help".equalsIgnoreCase(arg)) {
                showOutput(origin, args.isSilent(), "Usage:");
                showOutput(origin, args.isSilent(), "");
                showOutput(origin, args.isSilent(), cmdname + " <args>");
                showOutput(origin, args.isSilent(), cmdname
                        + " --namespace <name> <namespace commands>");
                showOutput(origin, args.isSilent(), cmdname
                        + " --namespace <name> [--inverse] </commandToRun <command args>>");
                showOutput(origin, args.isSilent(), "");
                showOutput(origin, args.isSilent(),
                        "Commands can only be specified if no other non-namespace args are given.");
                showOutput(origin, args.isSilent(), "");
                showOutput(origin, args.isSilent(),
                        "When trying to run a command, the namespace will be checked to see if the command can be run.");
                showOutput(origin, args.isSilent(),
                        "The checks performed are as follows:");
                showOutput(origin, args.isSilent(),
                        "   1) Does the namespace exist? if not, run the command and create the namespace.");
                showOutput(origin, args.isSilent(),
                        "   2) Is the namespace inhibited? - Do not run the command.");
                showOutput(origin, args.isSilent(),
                        "   3) Is the namespace in forced mode? - Run the command.");
                showOutput(origin, args.isSilent(),
                        "   4) If --inverse is specified, are we under the limit time? Run the command");
                showOutput(origin, args.isSilent(),
                        "   5) If --inverse is not specified, are we over the limit time? Run the command");
                showOutput(origin, args.isSilent(), "   6) Do not run the command.");
                showOutput(origin, args.isSilent(), "");
                showOutput(origin, args.isSilent(), "General Arguments.");
                showOutput(origin, args.isSilent(),
                        "  --list                   - List all current namespaces and their status");
                showOutput(origin, args.isSilent(),
                        "  --help                   - Print this help.");
                showOutput(origin, args.isSilent(),
                        "  --reset                  - Remove all namespaces.");
                showOutput(origin, args.isSilent(), "");
                showOutput(origin, args.isSilent(), "Useful things:");
                showOutput(origin, args.isSilent(),
                        "  --namespace <name>       - Namespace to modify. If the namespace does not exist, it will be created. Namespaces are not remembered across sessions.");
                showOutput(origin, args.isSilent(), "");
                showOutput(origin, args.isSilent(), "Arguments related to a namespace:");
                showOutput(origin, args.isSilent(),
                        "  --settime <time>         - Set the limit time on this namespace. Time can be either a time in seconds, 'now' for now, or 'nowifless' to set to now only if it is currently less.");
                showOutput(origin, args.isSilent(),
                        "  --delay <seconds>        - Increase the 'limit' time on this namespace by <seconds> seconds");
                showOutput(origin, args.isSilent(),
                        "  --inhibit                - Prevent any attempts at running commands in this namespace from executing");
                showOutput(origin, args.isSilent(),
                        "  --force                  - Any future attempts at running commands in this namespace will always execute");
                showOutput(origin, args.isSilent(),
                        "  --allow                  - Disable '--force' or '--inhibit' and resume normal operation.");
                showOutput(origin, args.isSilent(),
                        "  --remove                 - Remove this namespace.");
                showOutput(origin, args.isSilent(),
                        "  --status                 - Show the status of this namespace.");
                showOutput(origin, args.isSilent(), "");
                showOutput(origin, args.isSilent(), "Arguments when running a command:");
                showOutput(origin, args.isSilent(),
                        "  --inverse              - Inverse the match against the 'limit' time.");
                return;
            } else if ("--list".equalsIgnoreCase(arg)) {
                if (namespaces.isEmpty()) {
                    showOutput(origin, args.isSilent(),
                            "There are currently no known namespaces.");
                } else {
                    showOutput(origin, args.isSilent(), "Current namespaces: ");
                    for (final Map.Entry<String, ConditionalExecuteNamespace> e : namespaces.
                            entrySet()) {
                        showOutput(origin, args.isSilent(),"    " + e.getValue());
                    }
                }
                return;
            } else if ("--reset".equalsIgnoreCase(arg)) {
                namespaces.clear();
                showOutput(origin, args.isSilent(), "All namespaces removed.");
                return;
            } else if (namespace == null) {
                if ("--namespace".equalsIgnoreCase(arg)) {
                    if (nextArg.isEmpty()) {
                        showError(origin, args.isSilent(), "Error: You must specify a namespace.");
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
                    showError(origin, args.isSilent(),
                            "Error: You must specify a namespace first.");
                    return;
                }
            } else if ("--inhibit".equalsIgnoreCase(arg)) {
                namespace.inhibit();
                manipulated = true;
            } else if ("--force".equalsIgnoreCase(arg)) {
                namespace.force();
                manipulated = true;
            } else if ("--allow".equalsIgnoreCase(arg)) {
                namespace.reset();
                manipulated = true;
            } else if ("--settime".equalsIgnoreCase(arg)) {
                if (nextArg.isEmpty()) {
                    showError(origin, args.isSilent(), "Error: You must provide a time to use.");
                    return;
                } else if ("now".equalsIgnoreCase(nextArg)) {
                    namespace.setLimit(System.currentTimeMillis());
                    i++;
                    manipulated = true;
                } else if ("nowifless".equalsIgnoreCase(nextArg)) {
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
                        showError(origin, args.isSilent(), "Error: Invalid time: " + nextArg);
                        return;
                    }
                }
            } else if ("--delay".equalsIgnoreCase(arg)) {
                if (nextArg.isEmpty()) {
                    showError(origin, args.isSilent(),  "Error: You must provide a delay to use.");
                    return;
                } else {
                    try {
                        namespace.changeLimit(Long.parseLong(nextArg) * 1000);
                        i++;
                        manipulated = true;
                    } catch (final NumberFormatException nfe) {
                        showError(origin, args.isSilent(), "Error: Invalid delay: " + nextArg);
                        return;
                    }
                }
            } else if ("--remove".equalsIgnoreCase(arg)) {
                namespaces.remove(namespace.getName());
                showOutput(origin, args.isSilent(),
                        "Removed namespace '" + namespace.getName() + '\'');
                return;
            } else if ("--status".equalsIgnoreCase(arg)) {
                // Show the current status, in case some manipulations occurred prior to this.
                showOutput(origin, args.isSilent(), namespaces.get(namespace.getName()).toString());
                return;
            } else if ("--inverse".equalsIgnoreCase(arg)) {
                inverse = true;
            } else if (manipulated) {
                showError(origin, args.isSilent(),
                        "You can't run commands and manipulate the namespace at the same time, ignored.");
            } else {
                // Command to run!
                if (namespace.canRun(inverse)) {
                    final String command = args.getArgumentsAsString(i);
                    origin.getInputModel().map(InputModel::getCommandParser)
                            .ifPresent(cp -> cp.parseCommand(origin, command));
                }
                return;
            }
        }

        // If we get here, we either manipulated something, or should show the usage text.
        if (manipulated) {
            showOutput(origin, args.isSilent(), "Namespace updated.");
            showOutput(origin, args.isSilent(), namespace.toString());
            namespaces.put(namespace.getName(), namespace);
        } else {
            showError(origin, args.isSilent(), "Usage:");
            showError(origin, args.isSilent(), "");
            showError(origin, args.isSilent(), cmdname + " <args>");
            showError(origin, args.isSilent(), cmdname
                    + " --namespace <name> <namespace commands>");
            showError(origin, args.isSilent(), cmdname
                    + " --namespace <name> [--inverse] </commandToRun <command args>>");
            showError(origin, args.isSilent(), "");
            showError(origin, args.isSilent(), "For more information, see " + cmdname
                    + " --help");
        }
    }

}

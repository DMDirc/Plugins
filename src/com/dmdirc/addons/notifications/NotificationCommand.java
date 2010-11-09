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
package com.dmdirc.addons.notifications;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.plugins.ExportedService;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import java.util.List;

/**
 * Notification command, delegating notification to one of the registered
 * notification commands as preferred by the end user.
 */
public class NotificationCommand extends Command implements
        IntelligentCommand, CommandInfo {

    /** The plugin that's using this command. */
    private final NotificationsPlugin parent;

    /**
     * Creates a new instance of this notification command.
     *
     * @param parent The plugin that's instansiating this command
     */
    public NotificationCommand(final NotificationsPlugin parent) {
        super();

        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer<?> origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length > 0 && args.getArguments()[0]
                .equalsIgnoreCase("--methods")) {
            doMethodList(origin, args.isSilent());
        } else if (args.getArguments().length > 0 && args.getArguments()[0]
                .equalsIgnoreCase("--method")) {
            if (args.getArguments().length > 1) {
                final String sourceName = args.getArguments()[1];
                final ExportedService source = parent.getMethod(sourceName)
                        .getExportedService("showNotification");

                if (source == null) {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR,
                            "Method not found.");
                } else {
                    source.execute("DMDirc", args.getArgumentsAsString(2));
                }
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "You must specify a source when using --method.");
            }
        } else {
            if (parent.hasActiveMethod()) {
                parent.getPreferredMethod().getExportedService(
                        "showNotification").execute("DMDirc", args
                        .getArgumentsAsString(0));
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "No active notification methods available.");
            }
        }
    }

    /**
     * Outputs a list of methods for the notifcation command.
     *
     * @param origin The input window where the command was entered
     * @param isSilent Whether this command is being silenced
     */
    private void doMethodList(final FrameContainer<?> origin,
            final boolean isSilent) {
        final List<PluginInfo> methods = parent.getMethods();

        if (methods.isEmpty()) {
            sendLine(origin, isSilent, FORMAT_ERROR, "No notification "
                    + "methods available.");
        } else {
            final String[] headers = {"Method"};
            final String[][] data = new String[methods.size()][1];
            int i = 0;
            for (PluginInfo method : methods) {
                data[i][0] = method.getName();
                i++;
            }

            sendLine(origin, isSilent, FORMAT_OUTPUT, doTable(headers, data));
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        if (arg == 0) {
            final AdditionalTabTargets res = TabCompleter
                    .getIntelligentResults(arg, context, 0);
            res.add("--methods");
            res.add("--method");
            return res;
        } else if (arg == 1 && context.getPreviousArgs().get(0)
                .equalsIgnoreCase("--method")) {
            final AdditionalTabTargets res = new AdditionalTabTargets();
            res.excludeAll();
            for (PluginInfo source : parent.getMethods()) {
                res.add(source.getName());
            }
            return res;
        } else if (arg > 1 && context.getPreviousArgs().get(0)
                .equalsIgnoreCase("--method")) {
            final AdditionalTabTargets res = TabCompleter
                    .getIntelligentResults(arg, context, 2);
            return res;
        } else {
            final AdditionalTabTargets res =  TabCompleter
                    .getIntelligentResults(arg, context, context
                    .getPreviousArgs().get(0)
                    .equalsIgnoreCase("--methods") ? 1 : 0);
            return res;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "notification";
    }

    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "notification [--methods|--method <method>] text - "
                + "Notifies you of the text";
    }

    /** {@inheritDoc} */
    @Override
    public CommandType getType() {
        return CommandType.TYPE_GLOBAL;
    }
}

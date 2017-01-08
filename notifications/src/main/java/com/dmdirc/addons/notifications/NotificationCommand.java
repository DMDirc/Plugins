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

package com.dmdirc.addons.notifications;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.BaseCommand;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Notification command, delegating notification to one of the registered notification commands as
 * preferred by the end user.
 */
public class NotificationCommand extends BaseCommand implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo(
            "notification",
            "notification [--methods|--method <method>] text - "
            + "Notifies you of the text",
            CommandType.TYPE_GLOBAL);
    /** The notifications manager to get notification plugins from. */
    private final NotificationsManager manager;

    /**
     * Creates a new instance of this notification command.
     *
     * @param controller The controller to use for command information.
     * @param manager    The notifications manager to get notification plugins from
     */
    public NotificationCommand(final CommandController controller,
            final NotificationsManager manager) {
        super(controller);

        this.manager = manager;
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length > 0 &&
                "--methods".equalsIgnoreCase(args.getArguments()[0])) {
            doMethodList(origin, args.isSilent());
        } else if (args.getArguments().length > 0 &&
                "--method".equalsIgnoreCase(args.getArguments()[0])) {
            if (args.getArguments().length > 1) {
                final String sourceName = args.getArguments()[1];
                final NotificationHandler handler = manager.getHandler(sourceName);

                if (handler == null) {
                    showError(origin, args.isSilent(), "Method not found.");
                } else {
                    handler.showNotification("DMDirc", args.getArgumentsAsString(2));
                }
            } else {
                showError(origin, args.isSilent(),
                        "You must specify a method when using --method.");
            }
        } else if (manager.hasActiveHandler()) {
            manager.getPreferredHandler().showNotification("DMDirc", args.getArgumentsAsString(0));
        } else {
            showError(origin, args.isSilent(), "No active notification methods available.");
        }
    }

    /**
     * Outputs a list of methods for the notifcation command.
     *
     * @param origin   The input window where the command was entered
     * @param isSilent Whether this command is being silenced
     */
    private void doMethodList(final WindowModel origin,
            final boolean isSilent) {
        final Collection<String> handlers = manager.getHandlerNames();

        if (handlers.isEmpty()) {
            showError(origin, isSilent, "No notification methods available.");
        } else {
            final String[] headers = {"Method"};
            final String[][] data = new String[handlers.size()][1];
            int i = 0;
            for (String handler : handlers) {
                data[i][0] = handler;
                i++;
            }

            showOutput(origin, isSilent, doTable(headers, data));
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        res.excludeAll();
        if (arg == 0) {
            res.add("--methods");
            res.add("--method");
            return res;
        } else if (arg == 1 && "--method".equalsIgnoreCase(context.getPreviousArgs().get(0))) {
            res.addAll(manager.getHandlerNames());
            return res;
        }
        return res;
    }

}

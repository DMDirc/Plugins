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

package com.dmdirc.addons.freedesktop_notifications;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;

/**
 * The FDNotify Command shows a nice popup on using the FreeDesktop VisualNotifications.
 */
public final class FDNotifyCommand extends Command {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("fdnotify",
            "fdnotify <message> - Show a nice popup where available",
            CommandType.TYPE_GLOBAL);
    /** Plugin that owns this command. */
    final FreeDesktopNotificationsPlugin myPlugin;

    /**
     * Creates a new instance of FDNotifyCommand.
     *
     * @param controller The controller to use for command information.
     * @param myPlugin   the plugin creating this command.
     */
    public FDNotifyCommand(final CommandController controller,
            final FreeDesktopNotificationsPlugin myPlugin) {
        super(controller);
        this.myPlugin = myPlugin;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin, final CommandArguments args,
            final CommandContext context) {
        new Thread() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                myPlugin.showNotification("", args.getArgumentsAsString());
            }
        }.start();
    }

}

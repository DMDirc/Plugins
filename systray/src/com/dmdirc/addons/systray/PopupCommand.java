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

package com.dmdirc.addons.systray;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The /popup command allows the user to show a popup message from the system tray icon.
 */
public class PopupCommand extends Command {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("popup",
            "popup <message> - shows the message as a system tray popup",
            CommandType.TYPE_GLOBAL);
    /** Systray manager, used to show notifications. */
    private final SystrayManager manager;

    /**
     * Creates a new instance of PopupCommand.
     *
     * @param manager           Systray manager, used to show notifications
     * @param commandController The controller to use for command information.
     */
    @Inject
    public PopupCommand(final SystrayManager manager, final CommandController commandController) {
        super(commandController);

        this.manager = manager;
    }

    /**
     * Used to show a notification using this plugin.
     *
     * @param title   Title of dialog if applicable
     * @param message Message to show
     *
     * @return True if the notification was shown.
     */
    public boolean showPopup(final String title, final String message) {
        manager.notify(title, message);
        return true;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        showPopup("DMDirc", args.getArgumentsAsString());
    }

}

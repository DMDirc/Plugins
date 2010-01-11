/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Server;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.List;

/**
 * The FDNotify Command shows a nice popup on using the FreeDesktop
 * VisualNotifications.
 *
 * @author Shane 'Dataforce' McCormack
 */
public final class FDNotifyCommand extends GlobalCommand {
    /** Plugin that owns this command. */
    final FreeDesktopNotificationsPlugin myPlugin;
    
    /**
     * Creates a new instance of FDNotifyCommand.
     *
     * @param myPlugin the plugin creating this command.
     */
    public FDNotifyCommand(final FreeDesktopNotificationsPlugin myPlugin) {
        super();
        this.myPlugin = myPlugin;
        CommandManager.registerCommand(this);
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent, final CommandArguments args) {
        myPlugin.showNotification("", args.getArgumentsAsString());
    }
    
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "fdnotify";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showInHelp() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelp() {
        return "fdnotify <message> - Show a nice popup where available";
    }
}

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

package com.dmdirc.addons.ui_swing.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.CommandOptions;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Opens the server settings window for the server.
 */
@CommandOptions(allowOffline = false)
public class ServerSettings extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("serversettings",
            "serversettings - opens the server settings window", CommandType.TYPE_SERVER);
    /** The dialog provider used to show the settings dialog. */
    private final KeyedDialogProvider<Connection, ServerSettingsDialog> dialogProvider;

    /**
     * Creates a new instance of the {@link ServerSettings} command.
     *
     * @param commandController The command controller to use for command info.
     * @param dialogProvider    The dialog provider used to show the settings dialog
     */
    @Inject
    public ServerSettings(
            final CommandController commandController,
            final KeyedDialogProvider<Connection, ServerSettingsDialog> dialogProvider) {
        super(commandController);
        this.dialogProvider = dialogProvider;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        dialogProvider.displayOrRequestFocus(
                context.getSource().getOptionalConnection().orElse(null));
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        return new AdditionalTabTargets().excludeAll();
    }

}

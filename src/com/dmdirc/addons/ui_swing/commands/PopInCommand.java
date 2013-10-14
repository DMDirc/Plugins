/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;

import javax.inject.Inject;

/**
 * Command to pop in windows.
 */
public class PopInCommand extends Command {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("popin",
            "popin - Reattaches the current window to the main client if it"
                + " has been detached via the popin command.",
            CommandType.TYPE_GLOBAL);
    /** SwingController associated with this popin Command. */
    private final SwingController controller;

    /**
     * Create a new instance of PopInCommand.
     *
     * @param controller SwingWindowController associated with this command
     * @param commandController The command controller to use for command info.
     */
    @Inject
    public PopInCommand(
            final SwingController controller,
            final CommandController commandController) {
        super(commandController);
        this.controller = controller;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin, final CommandArguments args,
            final CommandContext context) {
        UIUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                final TextFrame swingWindow = controller.getWindowFactory()
                        .getSwingWindow(origin);
                if (swingWindow == null) {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR, "There is"
                            + " currently no window to pop in.");
                } else {
                    swingWindow.setPopout(false);
                }
            }
        });
    }

}

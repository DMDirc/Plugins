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
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ui.InputWindow;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.inject.Inject;

/**
 * The input command allows you to manipulate text in a windows inputField.
 *
 * @since 0.6.4
 */
public class Input extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("input",
            "input [--clear] <text to insert into inputfield> - Adds text to"
            + " the active window's input field",
            CommandType.TYPE_GLOBAL);
    /** Window factory instance used to get a container's window. */
    private final SwingWindowFactory windowFactory;

    /**
     * Creates a new input command.
     *
     * @param windowFactory Window factory to get windows from
     * @param commandController The controller to use for command information.
     */
    @Inject
    public Input(final SwingWindowFactory windowFactory, final CommandController commandController) {
        super(commandController);
        this.windowFactory = windowFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "input",
                    "[--clear] <text to insert into inputfield");
            return;
        } else if (args.getArguments().length == 1
                && "--clear".equals(args.getArgumentsAsString(0))) {
            ((InputWindow) windowFactory.getSwingWindow(origin))
                    .getInputHandler().clearInputField();
        } else {
            ((InputWindow) windowFactory.getSwingWindow(origin))
                    .getInputHandler().addToInputField(args
                    .getArgumentsAsString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.add("--clear");
        }
        return res;
    }

}

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

package com.dmdirc.addons.redirect;

import com.dmdirc.FrameContainer;
import com.dmdirc.MessageTarget;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompleter;
import com.dmdirc.util.URLBuilder;

/**
 * The redirect command allows the user to redirect the output from another command that would
 * normally echo results locally to a query or channel window instead.
 */
public class RedirectCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final BaseCommandInfo INFO = new BaseCommandInfo("redirect",
            "redirect <command> - sends the output of the command to a "
            + "channel or query window",
            CommandType.TYPE_CHAT);
    /** The sink manager to use to despatch messages. */
    private final MessageSinkManager messageSinkManager;
    /** The URL builder to use when finding icons. */
    private final URLBuilder urlBuilder;

    /**
     * Creates a new instance of this command.
     *
     * @param controller         The controller to use for command information.
     * @param messageSinkManager The sink manager to use to despatch messages.
     * @param urlBuilder         The URL builder to use when finding icons.
     */
    public RedirectCommand(
            final CommandController controller,
            final MessageSinkManager messageSinkManager,
            final URLBuilder urlBuilder) {
        super(controller);
        this.messageSinkManager = messageSinkManager;
        this.urlBuilder = urlBuilder;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final MessageTarget target = ((ChatCommandContext) context)
                .getChat();
        target.getCommandParser().parseCommand(new FakeWriteableFrameContainer(
                target, messageSinkManager, urlBuilder), args.getArgumentsAsString());
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        return TabCompleter.getIntelligentResults(arg, context, 0);
    }

}

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

package com.dmdirc.addons.debug.commands;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Outputs a table of all known identities.
 */
public class Identities extends DebugCommand {

    /** The controller to read identities from. */
    private final IdentityController identityController;

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     * @param controller      The controller to read identities from.
     */
    @Inject
    public Identities(final Provider<Debug> commandProvider, final IdentityController controller) {
        super(commandProvider);

        this.identityController = controller;
    }

    @Override
    public String getName() {
        return "identities";
    }

    @Override
    public String getUsage() {
        return "[type] - Outputs all known identities";
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final String type;

        if (args.getArguments().length == 0) {
            type = null;
        } else {
            type = args.getArgumentsAsString();
        }

        final List<ConfigProvider> identities = identityController.getProvidersByType(type);
        final String[][] data = new String[identities.size()][4];

        int i = 0;
        for (ConfigProvider source : identities) {
            data[i++] = new String[]{source.getName(),
                source.getTarget().getTypeName(), source.getTarget().getData(),
                String.valueOf(source.getTarget().getOrder())};
        }

        sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                doTable(new String[]{"Name", "Target", "Data", "Priority"}, data));
    }

}

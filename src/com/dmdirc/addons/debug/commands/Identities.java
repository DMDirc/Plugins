/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
import com.dmdirc.addons.debug.DebugPlugin;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.config.Identity;

import java.util.List;

/**
 * Outputs a table of all known identities.
 */
public class Identities extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param plugin Parent debug plugin
     * @param command Parent command
     */
    public Identities(final DebugPlugin plugin, final Debug command) {
        super(plugin, command);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "identities";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return "[type] - Outputs all known identities";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final String type;

        if (args.getArguments().length == 0) {
            type = null;
        } else {
            type = args.getArgumentsAsString();
        }

        final List<Identity> identities = getPlugin().getIdentityManager()
                .getIdentitiesByType(type);
        final String[][] data = new String[identities.size()][4];

        int i = 0;
        for (Identity source : identities) {
            data[i++] = new String[]{source.getName(),
            source.getTarget().getTypeName(), source.getTarget().getData(),
            String.valueOf(source.getTarget().getOrder())};
        }

        sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                doTable(new String[]{"Name", "Target", "Data", "Priority"}, data));
    }

}

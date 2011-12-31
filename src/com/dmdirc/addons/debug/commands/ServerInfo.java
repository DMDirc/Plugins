/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
import com.dmdirc.Server;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.addons.debug.DebugPlugin;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;

/**
 * Outputs general information about a connected server.
 */
public class ServerInfo extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param plugin Parent debug plugin
     * @param command Parent command
     */
    public ServerInfo(final DebugPlugin plugin, final Debug command) {
        super(plugin, command);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "serverinfo";
    }

    /** {@inheritDoc} */
    @Override
    public String getUsage() {
        return " - Outputs information about the server";
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (origin.getServer() == null) {
            sendLine(origin, args.isSilent(), FORMAT_ERROR,
                    "This window isn't connected to a server");
        } else {
            final Server server = origin.getServer();
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Server name: "
                    + server.getName());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Actual name: "
                    + server.getParser().getServerName());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Network: "
                    + server.getNetwork());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "IRCd: "
                    + server.getParser().getServerSoftware() + " - "
                    + server.getParser().getServerSoftwareType());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Modes: "
                    + server.getParser().getBooleanChannelModes() + " "
                    + server.getParser().getListChannelModes() + " "
                    + server.getParser().getParameterChannelModes() + " "
                    + server.getParser().getDoubleParameterChannelModes());
        }
    }

}

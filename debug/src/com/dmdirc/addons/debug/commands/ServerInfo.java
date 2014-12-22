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
import com.dmdirc.interfaces.Connection;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Outputs general information about a connected server.
 */
public class ServerInfo extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     */
    @Inject
    public ServerInfo(final Provider<Debug> commandProvider) {
        super(commandProvider);
    }

    @Override
    public String getName() {
        return "serverinfo";
    }

    @Override
    public String getUsage() {
        return " - Outputs information about the server";
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        final Optional<Connection> optionalConnection = origin.getConnection();
        if (optionalConnection.isPresent()) {
            final Connection connection = optionalConnection.get();
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Server name: "
                    + connection.getAddress());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Actual name: "
                    + connection.getParser().getServerName());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Network: "
                    + connection.getNetwork());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "IRCd: "
                    + connection.getParser().getServerSoftware() + " - "
                    + connection.getParser().getServerSoftwareType());
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Modes: "
                    + connection.getParser().getBooleanChannelModes() + ' '
                    + connection.getParser().getListChannelModes() + ' '
                    + connection.getParser().getParameterChannelModes() + ' '
                    + connection.getParser().getDoubleParameterChannelModes());
        } else {
            sendLine(origin, args.isSilent(), FORMAT_ERROR,
                    "This window isn't connected to a server");
        }
    }

}

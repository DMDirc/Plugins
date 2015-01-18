/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.ConfigProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Outputs the config info related to the current context.
 */
public class ConfigInfo extends DebugCommand {

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     */
    @Inject
    public ConfigInfo(final Provider<Debug> commandProvider) {
        super(commandProvider);
    }

    @Override
    public String getName() {
        return "configinfo";
    }

    @Override
    public String getUsage() {
        return " - Outputs context specific config information";
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        for (ConfigProvider source : origin.getConfigManager().getSources()) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, source.getTarget()
                    + " - " + source + "(" + source.getTarget().getOrder()
                    + ")");
        }
    }

}

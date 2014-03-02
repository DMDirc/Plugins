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
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.plugins.Service;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.plugins.ServiceProvider;
import com.dmdirc.ui.input.AdditionalTabTargets;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Outputs information regarding available services.
 */
public class Services extends DebugCommand implements IntelligentCommand {

    /** The service manager to get services from. */
    private final ServiceManager serviceManager;

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     * @param serviceManager  The service manager to get services from.
     */
    @Inject
    public Services(final Provider<Debug> commandProvider, final ServiceManager serviceManager) {
        super(commandProvider);

        this.serviceManager = serviceManager;
    }

    @Override
    public String getName() {
        return "services";
    }

    @Override
    public String getUsage() {
        return "[full] - Outputs available servers, optionally showing which"
                + " are active and which are not";
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Available Services:");
        for (Service service : serviceManager.getAllServices()) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "    "
                    + service.toString());
            if (args.getArguments().length > 0
                    && args.getArguments()[0].equals("full")) {
                for (ServiceProvider provider : service.getProviders()) {
                    sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                            "            " + provider.getProviderName()
                            + " [Active: " + provider.isActive() + "]");
                }
            }
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        res.excludeAll();

        if (arg == 1) {
            res.add("full");
        }

        return res;
    }

}

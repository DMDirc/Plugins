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

package com.dmdirc.addons.redirect;

import com.dmdirc.interfaces.CommandController;
import com.dmdirc.messages.MessageSinkManager;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

/**
 * The redirect plugin allows the suer to redirect the output of commands that
 * would normally echo their results locally to a channel or chat window instead.
 */
public class RedirectPlugin extends BaseCommandPlugin {

    /**
     * Creates a new instance of this plugin.
     *
     * @param commandController Command controller to register commands
     * @param messageSinkManager The sink manager to use to despatch messages.
     */
    public RedirectPlugin(
            final CommandController commandController,
            final MessageSinkManager messageSinkManager) {
        super(commandController);
        registerCommand(new RedirectCommand(commandController, messageSinkManager),
                RedirectCommand.INFO);
    }
}

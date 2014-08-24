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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.debug.Debug;
import com.dmdirc.addons.debug.DebugCommand;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.events.FirstRunEvent;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Opens the DMDirc first run wizard.
 */
public class FirstRun extends DebugCommand {

    /** Event bus to trigger first run event on. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of the command.
     *
     * @param commandProvider The provider to use to access the main debug command.
     * @param eventBus        Event bus to trigger first run event on.
     */
    @Inject
    public FirstRun(final Provider<Debug> commandProvider, final DMDircMBassador eventBus) {
        super(commandProvider);

        this.eventBus = eventBus;
    }

    @Override
    public String getName() {
        return "firstrun";
    }

    @Override
    public String getUsage() {
        return " - shows the first run wizard";
    }

    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        eventBus.publish(new FirstRunEvent());
    }

}

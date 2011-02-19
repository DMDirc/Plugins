/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.calc;

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.BasePlugin;

/**
 * A plugin which parses and evaluates various mathematical expressions.
 *
 * @author chris
 */
public class CalcPlugin extends BasePlugin {

    /** The command we register when loaded. */
    private final CalcCommand command = new CalcCommand();
    /** The CommandInfo object describing the calc command. */
    private final CalcCommandInfo commandInfo = new CalcCommandInfo();

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        CommandManager.getCommandManager().registerCommand(command, commandInfo);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        CommandManager.getCommandManager().unregisterCommand(commandInfo);
    }

}

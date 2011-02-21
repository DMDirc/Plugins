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

package com.dmdirc.addons.activewindow;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.PluginManager;

/**
 * Plugin to provide an active window command to the Swing UI.
 */
public final class ActiveWindowPlugin extends BasePlugin {

    /** The command we've registered. */
    private ActiveCommand command;

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        command = new ActiveCommand(((SwingController) PluginManager
                .getPluginManager().getPluginInfoByName("ui_swing")
                .getPlugin()).getMainFrame());
        CommandManager.getCommandManager().registerCommand(command, ActiveCommand.INFO);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        CommandManager.getCommandManager().unregisterCommand(ActiveCommand.INFO);
    }
}

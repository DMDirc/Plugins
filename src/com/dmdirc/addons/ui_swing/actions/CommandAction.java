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

package com.dmdirc.addons.ui_swing.actions;

import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.ui.Window;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Executes a command.
 */
public class CommandAction extends AbstractAction {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** Command parser. */
    private final CommandParser parser;
    /** Window. */
    private final transient Window window;
    /** Command. */
    private final String command;

    /**
     * Creates a new instance of CommandAction.
     *
     * @param parser  Command parser
     * @param window  Window
     * @param name    Command name
     * @param command Command to execute
     */
    public CommandAction(final CommandParser parser, final Window window,
            final String name, final String command) {
        super(name);

        this.parser = parser;
        this.window = window;
        this.command = command;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        for (String line : command.split("\n")) {
            parser.parseCommand(window.getContainer(), line);
        }
    }

}

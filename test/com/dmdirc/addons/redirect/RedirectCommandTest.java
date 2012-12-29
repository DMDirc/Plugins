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

package com.dmdirc.addons.redirect;

import com.dmdirc.MessageTarget;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.commandparser.commands.global.Echo;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.ui.InputWindow;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class RedirectCommandTest {
    
    private CommandManager controller;

    @Before
    public void setup() throws InvalidIdentityFileException {
        controller = mock(CommandManager.class);
        when(controller.getCommandChar()).thenReturn('/');
    }

    @Ignore
    @Test
    public void testExecute() {
        final ConfigManager manager = mock(ConfigManager.class);
        final MessageTarget target = mock(MessageTarget.class);
        final InputWindow window = mock(InputWindow.class);
        when(manager.getOptionInt("general", "commandhistory")).thenReturn(2);
        when(manager.getOption("formatter", "commandOutput")).thenReturn("%1$s");
        when(manager.getOptionString("formatter", "commandOutput")).thenReturn("%1$s");
        when (window.getContainer()).thenReturn(target);
        when(window.getContainer().getConfigManager()).thenReturn(manager);
        final CommandParser parser = new GlobalCommandParser(manager, controller);
        parser.registerCommand(new Echo(controller), Echo.INFO);
        when(target.getCommandParser()).thenReturn(parser);
        
        final RedirectCommand command = new RedirectCommand(controller);

        command.execute(target, new CommandArguments(controller, "/redirect /echo test"),
                new ChatCommandContext(window.getContainer(), RedirectCommand.INFO, target));

        verify(target).sendLine("test");
    }

}

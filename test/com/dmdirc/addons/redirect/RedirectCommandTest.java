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

import com.dmdirc.FrameContainer;
import com.dmdirc.MessageTarget;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.context.ChatCommandContext;
import com.dmdirc.commandparser.commands.global.Echo;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.InputWindow;
import com.dmdirc.ui.WindowManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RedirectCommandTest {

    @Mock private MessageTarget target;
    @Mock private InputWindow inputWindow;
    @Mock private CommandController commandController;
    @Mock private WritableFrameContainer frameContainer;
    @Mock private AggregateConfigProvider configProvider;
    @Mock private CommandParser commandParser;
    @Mock private WindowManager windowManager;

    @Before
    public void setup() {
        when(commandController.getCommandChar()).thenReturn('/');
        when(commandController.getSilenceChar()).thenReturn('.');
        when(inputWindow.getContainer()).thenReturn(frameContainer);
        when(target.getConfigManager()).thenReturn(configProvider);
        when(target.getCommandParser()).thenReturn(commandParser);
        when(configProvider.hasOptionString("formatter", "commandOutput")).thenReturn(true);
        when(configProvider.getOption("formatter", "commandOutput")).thenReturn("%1$s");
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                new Echo(commandController, windowManager).execute(
                        ((FrameContainer) invocation.getArguments()[0]),
                        new CommandArguments(commandController, "/echo test"),
                        null);
                return null;
            }
        }).when(commandParser).parseCommand(any(FrameContainer.class), eq("/echo test"));
    }

    @Test
    public void testExecute() {
        final RedirectCommand command = new RedirectCommand();

        command.execute(target, new CommandArguments(commandController, "/redirect /echo test"),
                new ChatCommandContext(frameContainer, RedirectCommand.INFO, target));

        verify(target).sendLine("test");
    }

}

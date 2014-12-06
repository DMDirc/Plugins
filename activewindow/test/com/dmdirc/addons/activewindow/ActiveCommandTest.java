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

package com.dmdirc.addons.activewindow;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.IntelligentCommand.IntelligentCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.ui.input.TabCompleterUtils;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActiveCommandTest {

    @Mock private IntelligentCommandContext intelligentCommandContext;
    @Mock private CommandParser commandParser;
    @Mock private CommandController commandController;
    @Mock private ActiveFrameManager activeFrameManager;
    @Mock private TabCompleterUtils tabCompleterUtils;
    @Mock private FrameContainer frameContainer;
    @Mock private TextFrame textFrame;
    @Mock private CommandArguments commandArguments;
    @Mock private CommandContext commandContext;
    private ActiveCommand activeCommand;

    @Before
    public void setUp() throws Exception {
        when(textFrame.getContainer()).thenReturn(frameContainer);
        when(frameContainer.getCommandParser()).thenReturn(commandParser);
        when(commandArguments.getArgumentsAsString()).thenReturn("some arguments");
        activeCommand = new ActiveCommand(commandController, activeFrameManager, tabCompleterUtils);
    }

    @Test
    public void testExecute_NoActive() throws Exception {
        when(activeFrameManager.getActiveFrame()).thenReturn(Optional.empty());
        activeCommand.execute(frameContainer, commandArguments, commandContext);
        verify(commandParser, never())
                .parseCommand(frameContainer, commandArguments.getArgumentsAsString());
    }

    @Test
    public void testExecute_Active() throws Exception {
        when(activeFrameManager.getActiveFrame()).thenReturn(Optional.of(textFrame));
        activeCommand.execute(frameContainer, commandArguments, commandContext);
        verify(commandParser).parseCommand(frameContainer, commandArguments.getArgumentsAsString());
    }

    @Test
    public void testGetSuggestions() throws Exception {
        activeCommand.getSuggestions(1, intelligentCommandContext);
        verify(tabCompleterUtils).getIntelligentResults(1, intelligentCommandContext, 0);
    }
}
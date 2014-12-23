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

package com.dmdirc.addons.time;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.commands.IntelligentCommand.IntelligentCommandContext;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.ui.input.AdditionalTabTargets;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimerCommandTest {

    @Mock private TimerManager timerManager;
    @Mock private CommandController commandController;
    @Mock private FrameContainer frameContainer;
    @Mock private CommandContext commandContext;
    @Mock private IntelligentCommandContext intelligentCommandContext;
    @Mock private CommandArguments commandArguments;
    @Mock private TimedCommand timer;

    private TimerCommand instance;

    @Before
    public void setUp() throws Exception {
        instance = new TimerCommand(timerManager, commandController);
        when(commandArguments.isSilent()).thenReturn(false);
        when(timerManager.getTimerByID(anyInt())).thenReturn(timer);
        when(commandController.getCommandChar()).thenReturn('/');
    }

    private void mockCommandArguments(final String one, final String two) {
        when(commandArguments.getArguments()).thenReturn(new String[]{one, two});
        when(commandArguments.getArgumentsAsString()).thenReturn((one + ' ' + two).trim());
        when(commandArguments.getArgumentsAsString(1)).thenReturn(two.trim());
    }

    private void mockCommandArguments(final String one, final String two, final String three) {
        when(commandArguments.getArguments()).thenReturn(new String[]{one, two, three});
        when(commandArguments.getArgumentsAsString()).thenReturn((one + ' ' + two + ' ' + three).trim());
        when(commandArguments.getArgumentsAsString(1)).thenReturn((two + ' ' + three).trim());
        when(commandArguments.getArgumentsAsString(2)).thenReturn(three.trim());
    }

    private void mockCommandArguments(final String one, final String two, final String three,
            final String four) {
        when(commandArguments.getArguments())
                .thenReturn(new String[]{one, two, three, four});
        when(commandArguments.getArgumentsAsString())
                .thenReturn((one + ' ' + two + ' ' + three + ' ' + four).trim());
        when(commandArguments.getArgumentsAsString(1))
                .thenReturn((two + ' ' + three + ' ' + four).trim());
        when(commandArguments.getArgumentsAsString(2))
                .thenReturn((three + ' ' + four).trim());
    }

    @Test
    public void testExecute_cancel_not_number() throws Exception {
        mockCommandArguments("--cancel", "one", "");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(timerManager, never()).hasTimerWithID(anyInt());
        verify(timerManager, never()).getTimerByID(anyInt());
        verify(timer, never()).cancelTimer();
    }

    @Test
    public void testExecute_cancel_wrong_id() throws Exception {
        mockCommandArguments("--cancel", "1", "");
        when(timerManager.hasTimerWithID(1)).thenReturn(false);
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(timerManager, times(1)).hasTimerWithID(1);
        verify(timerManager, never()).getTimerByID(1);
        verify(timer, never()).cancelTimer();
    }

    @Test
    public void testExecute_cancel_right_id() throws Exception {
        mockCommandArguments("--cancel", "1", "");
        when(timerManager.hasTimerWithID(1)).thenReturn(true);
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(timerManager, times(1)).hasTimerWithID(1);
        verify(timerManager, times(1)).getTimerByID(1);
        verify(timer, times(1)).cancelTimer();
    }

    @Test
    public void testExecute_list_empty() throws Exception {
        when(timerManager.listTimers()).thenReturn(Sets.newHashSet());
        mockCommandArguments("--list", "", "");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(timerManager, times(1)).listTimers();
        verify(frameContainer).addLine("commandError", "There are currently no active timers");
    }

    @Test
    public void testExecute_list_not_empty() throws Exception {
        when(timerManager.listTimers()).thenReturn(
                ImmutableMap.<Integer, TimedCommand>builder().put(1, timer).build().entrySet());
        mockCommandArguments("--list", "", "");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(timerManager, times(1)).listTimers();
        verify(frameContainer).addLine("commandOutput", "Timer ID: 1 - null");
    }

    @Test
    public void testExecute_incorrect() throws Exception {
        mockCommandArguments("woop", "woop");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(frameContainer, times(1)).addLine(eq("commandUsage"), eq('/'),
                eq(TimerCommand.INFO.getName()), eq(TimerCommand.INFO.getHelp()));
    }

    @Test
    public void testExecute_no_args() throws Exception {
        when(commandArguments.getArguments()).thenReturn(new String[0]);
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(frameContainer, times(1)).addLine(eq("commandUsage"), eq('/'),
                eq(TimerCommand.INFO.getName()), eq(TimerCommand.INFO.getHelp()));
    }

    @Test
    public void testExecute_too_few_args() throws Exception {
        mockCommandArguments("woop", "woop");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(frameContainer, times(1)).addLine(eq("commandUsage"), eq('/'),
                eq(TimerCommand.INFO.getName()), eq(TimerCommand.INFO.getHelp()));
    }

    @Test
    public void testExecute_repetitions_not_number() throws Exception {
        mockCommandArguments("woop", "woop", "woop");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(frameContainer, times(1)).addLine(eq("commandUsage"), eq('/'),
                eq(TimerCommand.INFO.getName()), eq(TimerCommand.INFO.getHelp()));
    }

    @Test
    public void testExecute_interval_not_number() throws Exception {
        mockCommandArguments("1", "woop", "woop");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(frameContainer, times(1)).addLine(eq("commandUsage"), eq('/'),
                eq(TimerCommand.INFO.getName()), eq(TimerCommand.INFO.getHelp()));
    }

    @Test
    public void testExecute_interval_invalid() throws Exception {
        mockCommandArguments("1", "0", "woop");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(frameContainer, times(1))
                .addLine(eq("commandError"), eq("Cannot use intervals below 1"));
    }

    @Test
    public void testExecute_add() throws Exception {
        mockCommandArguments("1", "1", "woop");
        instance.execute(frameContainer, commandArguments, commandContext);
        verify(timerManager).addTimer(1, 1, "woop", frameContainer);
        verify(frameContainer, times(1)).addLine(eq("commandOutput"), eq("Command scheduled."));
    }

    @Test
    public void testGetSuggestions_first() throws Exception {
        final AdditionalTabTargets targets = instance.getSuggestions(0, intelligentCommandContext);
        assertEquals(Lists.newArrayList("--list", "--cancel"), targets);
    }

    @Test
    public void testGetSuggestions_not_cancel() throws Exception {
        when(intelligentCommandContext.getPreviousArgs()).thenReturn(Lists.newArrayList("--list"));
        final AdditionalTabTargets targets = instance.getSuggestions(1, intelligentCommandContext);
        assertEquals(Lists.<String>newArrayList(), targets);
    }

    @Test
    public void testGetSuggestions_second() throws Exception {
        when(intelligentCommandContext.getPreviousArgs())
                .thenReturn(Lists.newArrayList("--cancel"));
        when(timerManager.getTimerIDs()).thenReturn(Sets.newHashSet(1, 2, 3));
        final AdditionalTabTargets targets = instance.getSuggestions(1, intelligentCommandContext);
        assertEquals(Lists.newArrayList("1", "2", "3"), targets);
    }

    @Test
    public void testGetSuggestions_third() throws Exception {
        final AdditionalTabTargets targets = instance.getSuggestions(3, intelligentCommandContext);
        assertEquals(Lists.<String>newArrayList(), targets);
    }
}
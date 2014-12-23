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
import com.dmdirc.commandparser.parsers.CommandParser;

import java.util.Timer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimedCommandTest {

    @Mock private TimerManager timerManager;
    @Mock private FrameContainer origin;
    @Mock private CommandParser commandParser;
    @Mock private TimerFactory timerFactory;
    @Mock private Timer timer;

    private TimedCommand instance;

    @Before
    public void setUp() throws Exception {
        when(timerFactory.getTimer(anyString())).thenReturn(timer);
        when(origin.getCommandParser()).thenReturn(commandParser);
        instance = new TimedCommand(timerManager, 1, 2, 3, "command", origin);
    }

    @Test
    public void testSchedule() throws Exception {
        instance.schedule(timerFactory);
        verify(timer).schedule(instance, 3000, 3000);
    }

    @Test
    public void testGetCommand() throws Exception {
        assertEquals("command", instance.getCommand());
    }

    @Test
    public void testCancelTimer_WithoutSchedule() throws Exception {
        instance.cancelTimer();
        verify(timerManager).removeTimer(1);
    }

    @Test
    public void testCancelTimer() throws Exception {
        instance.schedule(timerFactory);
        instance.cancelTimer();
        verify(timerManager).removeTimer(1);
        verify(timer).cancel();
    }

    @Test
    public void testRun_withoutSchedule() throws Exception {
        instance.run();
        verify(commandParser, never()).parseCommand(origin, "command");
        verify(timerManager, never()).removeTimer(1);
        verify(timer, never()).cancel();
    }

    @Test
    public void testRun_LessThanRepetitions() throws Exception {
        instance.schedule(timerFactory);
        instance.run();
        verify(commandParser, times(1)).parseCommand(origin, "command");
        verify(timerManager, never()).removeTimer(1);
        verify(timer, never()).cancel();
    }

    @Test
    public void testRun_AllRepetitions() throws Exception {
        instance.schedule(timerFactory);
        instance.run();
        instance.run();
        verify(commandParser, times(2)).parseCommand(origin, "command");
        verify(timerManager, times(1)).removeTimer(1);
        verify(timer, times(1)).cancel();
    }
}
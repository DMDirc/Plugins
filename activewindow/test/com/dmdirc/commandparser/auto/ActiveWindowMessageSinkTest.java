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

package com.dmdirc.commandparser.auto;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.activewindow.ActiveWindowMessageSink;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.ui.messages.sink.MessageSinkManager;

import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActiveWindowMessageSinkTest {

    private static final Pattern PATTERN = Pattern.compile("active");
    @Mock private ActiveFrameManager activeFrameManager;
    @Mock private MessageSinkManager messageSinkManager;
    @Mock private FrameContainer frameContainer;
    @Mock private TextFrame textFrame;
    @Mock private Date date;
    private ActiveWindowMessageSink sink;

    @Before
    public void setUp() throws Exception {
        sink = new ActiveWindowMessageSink(activeFrameManager);
        when(textFrame.getContainer()).thenReturn(frameContainer);
    }

    @Test
    public void testGetPattern() throws Exception {
        assertEquals(PATTERN.toString(), sink.getPattern().toString());
    }

    @Test
    public void testHandleMessage_NoActive() throws Exception {
        when(activeFrameManager.getActiveFrame()).thenReturn(Optional.empty());
        sink.handleMessage(messageSinkManager, frameContainer, null, date, "type", "message");
        verify(frameContainer, never()).addLine("type", date, "message");
    }

    @Test
    public void testHandleMessage_Active() throws Exception {
        when(activeFrameManager.getActiveFrame()).thenReturn(Optional.of(textFrame));
        sink.handleMessage(messageSinkManager, frameContainer, null, date, "type", "message");
        verify(frameContainer).addLine("type", date, "message");
    }
}
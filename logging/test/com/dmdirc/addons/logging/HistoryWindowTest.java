/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.logging;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.events.DisplayPropertyMap;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.IRCDocument;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class HistoryWindowTest {

    @Mock private IRCDocument document;
    @Mock private BackBuffer backBuffer;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder configBinder;
    @Mock private Connection connection;
    @Mock private FrameContainer frameContainer;
    @Mock private DMDircMBassador eventBus;
    @Mock private BackBufferFactory backBufferFactory;
    private HistoryWindow instance;

    @Before
    public void setUp() throws Exception {
        when(backBufferFactory.getBackBuffer(any())).thenReturn(backBuffer);
        when(backBuffer.getDocument()).thenReturn(document);
        when(config.getBinder()).thenReturn(configBinder);
        when(frameContainer.getConfigManager()).thenReturn(config);
        when(frameContainer.getConnection()).thenReturn(Optional.of(connection));
        instance = new HistoryWindow("Awesome",
                Paths.get(getClass().getResource("logfile.txt").toURI()),
                frameContainer, eventBus, backBufferFactory, 4);
    }

    @Test
    public void testGetConnection() throws Exception {
        assertEquals(Optional.of(connection), instance.getConnection());
    }

    @Test
    public void testOutputLoggingBackBuffer() throws Exception {
        instance.outputLoggingBackBuffer(4);
        final InOrder inOrder = inOrder(document);
        inOrder.verify(document).addText(eq(new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]")
                .parse("[21/12/2015 12:58:02]").getTime()), eq
                (DisplayPropertyMap.EMPTY), eq("RAAR"));
        inOrder.verify(document).addText(eq(new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]")
                .parse("[21/12/2015 12:59:03]").getTime()), eq
                (DisplayPropertyMap.EMPTY), eq("RAAAR"));
        inOrder.verify(document).addText(eq(new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]")
                .parse("[21/12/2015 13:00:04]").getTime()), eq
                (DisplayPropertyMap.EMPTY), eq("RAAAAR"));
        inOrder.verify(document).addText(eq(new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]")
                .parse("[21/12/2015 13:01:05]").getTime()), eq
                (DisplayPropertyMap.EMPTY), eq("RAAAAAR"));
        inOrder.verifyNoMoreInteractions();
    }
}
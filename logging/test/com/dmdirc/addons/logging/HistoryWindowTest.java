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
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.messages.BackBuffer;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
@RunWith(MockitoJUnitRunner.class)
public class HistoryWindowTest {

    @Mock private BackBuffer backBuffer;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder configBinder;
    @Mock private Connection connection;
    @Mock private WindowModel frameContainer;
    @Mock private DMDircMBassador eventBus;
    @Mock private BackBufferFactory backBufferFactory;
    private HistoryWindow instance;

    @Before
    public void setUp() throws Exception {
        when(backBufferFactory.getBackBuffer(any())).thenReturn(backBuffer);
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
        final LineListener listener = new LineListener();
        instance.getEventBus().subscribe(listener);
        instance.outputLoggingBackBuffer(4);

        assertTrue(listener.latch.await(5, TimeUnit.SECONDS));
        assertEquals("[21/12/2015 12:58:02] RAAR", listener.values.get(0).getLine());
        assertEquals("[21/12/2015 12:59:03] RAAAR", listener.values.get(1).getLine());
        assertEquals("[21/12/2015 13:00:04] RAAAAR", listener.values.get(2).getLine());
        assertEquals("[21/12/2015 13:01:05] RAAAAAR", listener.values.get(3).getLine());
    }

    @Listener(references = References.Strong)
    private static class LineListener {

        public final List<HistoricalLineRestoredEvent> values = new ArrayList<>();
        public final CountDownLatch latch = new CountDownLatch(4);

        @Handler
        private void handleLineRestored(final HistoricalLineRestoredEvent event) {
            values.add(event);
            latch.countDown();
        }

    }
}
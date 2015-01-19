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

package com.dmdirc.addons.channelwho;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ServerConnectingEvent;
import com.dmdirc.events.ServerDisconnectedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.ConnectionManager;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelWhoManagerTest {

    @Mock private ConnectionHandlerFactory connectionHandlerFactory;
    @Mock private ConnectionHandler connectionHandler;
    @Mock private ConnectionManager connectionManager;
    @Mock private Connection connection;
    @Mock private Connection connection2;
    @Mock private DMDircMBassador eventBus;

    private ChannelWhoManager instance;

    @Before
    public void setUp() throws Exception {
        when(connectionManager.getConnections()).thenReturn(Lists.newArrayList(connection));
        when(connectionHandlerFactory.get(connection)).thenReturn(connectionHandler);
        instance = new ChannelWhoManager("domain", connectionHandlerFactory, connectionManager,
                eventBus);
        instance.load();
    }

    @Test
    public void testLoad() throws Exception {
        verify(eventBus).subscribe(instance);
        verify(connectionHandlerFactory).get(connection);
    }

    @Test
    public void testUnload() throws Exception {
        instance.unload();
        verify(eventBus).unsubscribe(instance);
        verify(connectionHandler).unload();
    }

    @Test
    public void testServerConnectionEvent() throws Exception {
        instance.handleServerConnectingEvent(new ServerConnectingEvent(connection2));
        verify(connectionHandlerFactory).get(connection);
    }

    @Test
    public void testServerDisconnectionEvent_Existing() throws Exception {
        instance.handleServerDisconnectedEvent(new ServerDisconnectedEvent(connection));
        verify(connectionHandler).unload();
    }

    @Test
    public void testServerDisconnectionEvent_Unknown() throws Exception {
        instance.handleServerDisconnectedEvent(new ServerDisconnectedEvent(connection2));
        verify(connectionHandler, never()).unload();
    }
}
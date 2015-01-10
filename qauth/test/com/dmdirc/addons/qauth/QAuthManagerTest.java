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

package com.dmdirc.addons.qauth;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.Invite;
import com.dmdirc.Query;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.events.QueryMessageEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerInviteReceivedEvent;
import com.dmdirc.events.ServerNoticeEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginInfo;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QAuthManagerTest {

    @Mock private DMDircMBassador eventBus;
    @Mock private PluginInfo pluginInfo;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder configBinder;
    @Mock private ServerConnectedEvent serverConnectedEvent;
    @Mock private ServerInviteReceivedEvent serverInviteReceivedEvent;
    @Mock private ServerNoticeEvent serverNoticeEvent;
    @Mock private QueryMessageEvent queryMessageEvent;
    @Mock private User user;
    @Mock private Query query;
    @Mock private Invite invite;
    @Mock private Connection connection;
    private QAuthManager instance;

    @Before
    public void setUp() throws Exception {
        when(serverConnectedEvent.getConnection()).thenReturn(connection);
        when(serverInviteReceivedEvent.getConnection()).thenReturn(connection);
        when(serverInviteReceivedEvent.getInvite()).thenReturn(invite);
        when(serverInviteReceivedEvent.getChannel()).thenReturn("#channel");
        when(serverInviteReceivedEvent.getUser()).thenReturn(user);
        when(serverNoticeEvent.getConnection()).thenReturn(connection);
        when(serverNoticeEvent.getUser()).thenReturn(user);
        when(serverNoticeEvent.getMessage()).thenReturn("You are now logged in as username.");
        when(queryMessageEvent.getQuery()).thenReturn(query);
        when(queryMessageEvent.getUser()).thenReturn(user);
        when(queryMessageEvent.getMessage()).thenReturn("You are now logged in as username.");
        when(user.getNickname()).thenReturn("Q");
        when(query.getConnection()).thenReturn(Optional.of(connection));
        when(connection.getNetwork()).thenReturn("Quakenet");
        when(config.getBinder()).thenReturn(configBinder);
        when(configBinder.withDefaultDomain("pluginDomain")).thenReturn(configBinder);
        instance = new QAuthManager("pluginDomain", pluginInfo, config, eventBus);

        instance.handleUsername("username");
        instance.handlePassword("password");
        instance.handleAcceptInvites(true);
        instance.handleAutoInvite(true);
    }

    @Test
    public void testLoad() {
        instance.load();
        verify(configBinder).bind(instance, QAuthManager.class);
        verify(eventBus).subscribe(instance);
    }

    @Test
    public void testUnload() {
        instance.unload();
        verify(configBinder).unbind(instance);
        verify(eventBus).unsubscribe(instance);
    }

    @Test
    public void testConnected_IncorrectNetwork() {
        when(connection.getNetwork()).thenReturn("NotQuakenet");
        instance.handleConnect(serverConnectedEvent);
        verify(connection, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testConnected_Quakenet() {
        instance.handleConnect(serverConnectedEvent);
        verify(connection).sendMessage("Q@Cserve.quakenet.org", "auth username password");
    }

    @Test
    public void testInvites_NoHandle() {
        instance.handleAcceptInvites(false);
        instance.handleInvite(serverInviteReceivedEvent);
        verifyZeroInteractions(invite);
    }

    @Test
    public void testInvites_NotQuakenet() {
        when(connection.getNetwork()).thenReturn("NotQuakenet");
        instance.handleInvite(serverInviteReceivedEvent);
        verifyZeroInteractions(invite);
    }

    @Test
    public void testInvites_Quakenet_NotQ() {
        when(user.getNickname()).thenReturn("NotQ");
        instance.handleAcceptInvites(false);
        instance.handleInvite(serverInviteReceivedEvent);
        verifyZeroInteractions(invite);
    }

    @Test
    public void testInvites_Quakenet_Q() {
        instance.handleInvite(serverInviteReceivedEvent);
        verify(invite).accept();
    }

    @Test
    public void testCommunication_Message_NoConnection() {
        when(queryMessageEvent.getQuery()).thenReturn(query);
        when(query.getConnection()).thenReturn(Optional.<Connection>empty());
        instance.handleMessages(queryMessageEvent);
        verifyZeroInteractions(connection);
    }

    @Test
    public void testCommunication_Message_NotQuakenet() {
        when(connection.getNetwork()).thenReturn("NotQuakenet");
        instance.handleMessages(queryMessageEvent);
        verify(connection, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testCommunication_Message_Quakenet_NotQ() {
        when(user.getNickname()).thenReturn("NotQ");
        instance.handleMessages(queryMessageEvent);
        verify(connection, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testCommunication_Message_Quakenet_Q() {
        instance.handleMessages(queryMessageEvent);
        verify(connection).sendMessage("Q@Cserve.quakenet.org", "invite");
    }

    @Test
    public void testCommunication_Message_Off() {
        instance.handleAutoInvite(false);
        instance.handleMessages(queryMessageEvent);
        verify(connection, never()).sendMessage("Q@Cserve.quakenet.org", "invite");
    }

    @Test
    public void testCommunication_Notice_NotQuakenet() {
        when(connection.getNetwork()).thenReturn("NotQuakenet");
        instance.handleNotices(serverNoticeEvent);
        verify(connection, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testCommunication_Notice_Quakenet_NotQ() {
        when(user.getNickname()).thenReturn("NotQ");
        instance.handleNotices(serverNoticeEvent);
        verify(connection, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testCommunication_Notice_Quakenet_Q() {
        instance.handleNotices(serverNoticeEvent);
        verify(connection).sendMessage("Q@Cserve.quakenet.org", "invite");
    }

    @Test
    public void testCommunication_Notice_Off() {
        instance.handleAutoInvite(false);
        instance.handleNotices(serverNoticeEvent);
        verify(connection, never()).sendMessage("Q@Cserve.quakenet.org", "invite");
    }
}
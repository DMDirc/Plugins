/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.qauth;

import com.dmdirc.Invite;
import com.dmdirc.Query;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.QueryMessageEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerInviteReceivedEvent;
import com.dmdirc.events.ServerNoticeEvent;
import com.dmdirc.events.UserInfoResponseEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.parser.events.UserInfoEvent;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginMetaData;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QAuthManagerTest {

    @Mock private AggregateConfigProvider aggregateConfigProvider;
    @Mock private ConfigProvider configProvider;
    @Mock private PreferencesDialogModel preferencesDialogModel;
    @Mock private EventBus eventBus;
    @Mock private PluginInfo pluginInfo;
    @Mock private PluginMetaData pluginMetaData;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder configBinder;
    @Mock private ServerConnectedEvent serverConnectedEvent;
    @Mock private ServerInviteReceivedEvent serverInviteReceivedEvent;
    @Mock private ServerNoticeEvent serverNoticeEvent;
    @Mock private QueryMessageEvent queryMessageEvent;
    @Mock private ClientPrefsOpenedEvent clientPrefsOpenedEvent;
    @Mock private User user;
    @Mock private User localUser;
    @Mock private Query query;
    @Mock private Invite invite;
    @Mock private Connection connection;
    @Mock private PreferencesCategory preferencesCategory;
    @Mock private UserInfoResponseEvent userInfoResponseEvent;
    @Captor private ArgumentCaptor<PreferencesCategory> preferencesCategoryArgumentCaptor;
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
        when(connection.getLocalUser()).thenReturn(Optional.of(localUser));
        when(config.getBinder()).thenReturn(configBinder);
        when(configBinder.withDefaultDomain("pluginDomain")).thenReturn(configBinder);
        when(pluginInfo.getMetaData()).thenReturn(pluginMetaData);
        when(pluginMetaData.getFriendlyName()).thenReturn("Plugin");
        when(clientPrefsOpenedEvent.getModel()).thenReturn(preferencesDialogModel);
        when(preferencesDialogModel.getConfigManager()).thenReturn(aggregateConfigProvider);
        when(preferencesDialogModel.getIdentity()).thenReturn(configProvider);
        when(preferencesDialogModel.getCategory("Plugins")).thenReturn(preferencesCategory);
        when(userInfoResponseEvent.getConnection()).thenReturn(connection);
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

    @Test
    public void testAuthWithWhois() {
        instance.handleWhois(true);
        instance.handleConnect(serverConnectedEvent);
        verify(connection).requestUserInfo(localUser);
        verify(connection, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testAuthWithoutWhois() {
        instance.handleWhois(false);
        instance.handleConnect(serverConnectedEvent);
        verify(connection, never()).requestUserInfo(localUser);
        verify(connection).sendMessage(anyString(), anyString());
    }

    @Test
    public void testHandlePrefsEvent() throws Exception {
        when(aggregateConfigProvider.getOptionBool("domain", "whoisonquery")).thenReturn(true);
        instance.showConfig(clientPrefsOpenedEvent);
        verify(preferencesCategory).addSubCategory(preferencesCategoryArgumentCaptor.capture());
        assertTrue(preferencesCategoryArgumentCaptor.getValue().getSettings().size() > 1);
    }

    @Test
    public void testWhoisReply_NotWaiting() throws Exception {
        instance.handleUserInfoResponse(userInfoResponseEvent);
        verifyNoMoreInteractions(connection);
    }

    @Test
    public void testWhoisReply_Waiting_Authed() throws Exception {
        when(userInfoResponseEvent.getInfo(UserInfoEvent.UserInfoType.ACCOUNT_NAME))
                .thenReturn(Optional.of("RAR"));
        when(userInfoResponseEvent.getUser()).thenReturn(localUser);
        instance.handleWhois(true);
        instance.handleConnect(serverConnectedEvent);
        instance.handleUserInfoResponse(userInfoResponseEvent);
        verify(connection, never()).sendMessage(anyString(), anyString());
    }

    @Test
    public void testWhoisReply_Waiting_NotAuthed() throws Exception {
        when(userInfoResponseEvent.getInfo(UserInfoEvent.UserInfoType.ACCOUNT_NAME))
                .thenReturn(Optional.empty());
        when(userInfoResponseEvent.getUser()).thenReturn(localUser);
        instance.handleWhois(true);
        instance.handleConnect(serverConnectedEvent);
        instance.handleUserInfoResponse(userInfoResponseEvent);
        verify(connection).sendMessage(anyString(), anyString());
    }
}
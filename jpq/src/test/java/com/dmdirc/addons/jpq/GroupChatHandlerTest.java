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

package com.dmdirc.addons.jpq;

import com.dmdirc.config.ConfigBinder;
import com.dmdirc.events.ChannelJoinEvent;
import com.dmdirc.events.ChannelPartEvent;
import com.dmdirc.events.ChannelQuitEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupChatHandlerTest {

    @Mock private GroupChat groupChat;
    @Mock private WindowModel windowModel;
    @Mock private AggregateConfigProvider configProvider;
    @Mock private ConfigBinder configBinder;
    @Mock private EventBus eventBus;
    @Mock private ChannelJoinEvent channelJoinEvent;
    @Mock private ChannelPartEvent channelPartEvent;
    @Mock private ChannelQuitEvent channelQuitEvent;
    private GroupChatHandler instance;

    @Before
    public void setUp() throws Exception {
        when(groupChat.getEventBus()).thenReturn(eventBus);
        when(groupChat.getWindowModel()).thenReturn(windowModel);
        when(windowModel.getConfigManager()).thenReturn(configProvider);
        when(configProvider.getBinder()).thenReturn(configBinder);
        when(configBinder.withDefaultDomain("domain")).thenReturn(configBinder);
        when(channelJoinEvent.getChannel()).thenReturn(groupChat);
        when(channelPartEvent.getChannel()).thenReturn(groupChat);
        when(channelQuitEvent.getChannel()).thenReturn(groupChat);
        instance = new GroupChatHandler("domain", groupChat);
    }

    @Test
    public void testLoad() throws Exception {
        instance.load();
        verify(configBinder).bind(instance, GroupChatHandler.class);
        verify(eventBus).subscribe(instance);
    }

    @Test
    public void testUnload() throws Exception {
        instance.unload();
        verify(configBinder).unbind(instance);
        verify(eventBus).unsubscribe(instance);
    }

    @Test
    public void testHandleJoin_On() throws Exception {
        instance.load();
        instance.handleSettingChange(true);
        instance.handleJoin(channelJoinEvent);
        verify(channelJoinEvent).setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
    }

    @Test
    public void testHandlePart_On() throws Exception {
        instance.load();
        instance.handleSettingChange(true);
        instance.handlePart(channelPartEvent);
        verify(channelPartEvent).setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
    }

    @Test
    public void testHandleQuit_On() throws Exception {
        instance.load();
        instance.handleSettingChange(true);
        instance.handleQuit(channelQuitEvent);
        verify(channelQuitEvent).setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
    }

    @Test
    public void testHandleJoin_Off() throws Exception {
        instance.load();
        instance.handleSettingChange(false);
        instance.handleJoin(channelJoinEvent);
        verify(channelJoinEvent, never()).setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
    }

    @Test
    public void testHandlePart_Off() throws Exception {
        instance.load();
        instance.handleSettingChange(false);
        instance.handlePart(channelPartEvent);
        verify(channelPartEvent, never()).setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
    }

    @Test
    public void testHandleQuit_Off() throws Exception {
        instance.load();
        instance.handleSettingChange(false);
        instance.handleJoin(channelJoinEvent);
        verify(channelJoinEvent, never()).setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
    }
}
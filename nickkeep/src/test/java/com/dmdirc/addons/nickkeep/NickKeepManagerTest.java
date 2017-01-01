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

package com.dmdirc.addons.nickkeep;

import com.dmdirc.Channel;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.events.ChannelNickChangeEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.EventBus;

import com.google.common.collect.Lists;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NickKeepManagerTest {

    @Mock private EventBus eventBus;
    @Mock private ChannelNickChangeEvent event;
    @Mock private Channel channel;
    @Mock private Connection connection;
    @Mock private Profile profile;
    private NickKeepManager instance;

    @Before
    public void setUp() throws Exception {
        instance = new NickKeepManager(eventBus);
        when(event.getChannel()).thenReturn(channel);
        when(channel.getConnection()).thenReturn(Optional.of(connection));
        when(connection.getProfile()).thenReturn(profile);
        when(profile.getNicknames()).thenReturn(Lists.newArrayList("one", "two", "three"));
    }

    @Test
    public void testLoad() throws Exception {
        instance.load();
        verify(eventBus).subscribe(instance);
    }

    @Test
    public void testUnload() throws Exception {
        instance.unload();
        verify(eventBus).unsubscribe(instance);
    }

    @Test
    public void testHandleNickChange_NoConnection() throws Exception {
        when(channel.getConnection()).thenReturn(Optional.empty());
        instance.handleNickChange(event);
        verify(connection, never()).setNickname("one");
    }

    @Test
    public void testHandleNickChange_NoCurrentNickname() throws Exception {
        when(connection.getNickname()).thenReturn(Optional.empty());
        instance.handleNickChange(event);
        verify(connection, never()).setNickname("one");
    }

    @Test
    public void testHandleNickChange_NoNicknames() throws Exception {
        when(profile.getNicknames()).thenReturn(Lists.newArrayList());
        instance.handleNickChange(event);
        verify(connection, never()).setNickname(anyString());
    }

    @Test
    public void testHandleNickChange_NotDesired() throws Exception {
        when(event.getOldNick()).thenReturn("RAR");
        when(connection.getNickname()).thenReturn(Optional.of("one"));
        instance.handleNickChange(event);
        verify(connection, never()).setNickname("one");
    }

    @Test
    public void testHandleNickChange_Desired() throws Exception {
        when(event.getOldNick()).thenReturn("one");
        when(connection.getNickname()).thenReturn(Optional.of("RAR"));
        instance.handleNickChange(event);
        verify(connection).setNickname("one");
    }
}
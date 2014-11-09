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

package com.dmdirc.addons.awaycolours;

import com.dmdirc.ChannelClientProperty;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.events.ChannelUserAwayEvent;
import com.dmdirc.events.ChannelUserBackEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.util.colours.Colour;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AwayColoursManagerTest {

    @Mock private DMDircMBassador eventBus;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder binder;
    @Mock private ChannelUserAwayEvent awayEvent;
    @Mock private ChannelUserBackEvent backEvent;
    @Mock private ChannelClientInfo user;
    @Mock private Map<Object, Object> map;
    private AwayColoursManager instance;
    private Colour colour;

    @Before
    public void setUp() throws Exception {
        colour = Colour.RED;
        instance = new AwayColoursManager(eventBus, config, "test");
        when(awayEvent.getUser()).thenReturn(user);
        when(backEvent.getUser()).thenReturn(user);
        when(user.getMap()).thenReturn(map);
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
    public void testHandleColourChange() throws Exception {
        instance.handleColourChange(colour);
        instance.handleNicklistChange(true);
        instance.handleTextChange(true);
        instance.handleAwayEvent(awayEvent);
        verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, colour);
        verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, colour);
        instance.handleColourChange(Colour.BLACK);
        instance.handleNicklistChange(true);
        instance.handleTextChange(true);
        instance.handleAwayEvent(awayEvent);
        verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, Colour.BLACK);
        verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, Colour.BLACK);
    }

    @Test
    public void testHandleNicklistChange() throws Exception {
        instance.handleColourChange(colour);
        instance.handleNicklistChange(true);
        instance.handleTextChange(true);
        instance.handleAwayEvent(awayEvent);
        verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, colour);
        verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, colour);
        instance.handleColourChange(Colour.BLACK);
        instance.handleNicklistChange(false);
        instance.handleTextChange(true);
        instance.handleAwayEvent(awayEvent);
        verify(map, never()).put(ChannelClientProperty.NICKLIST_FOREGROUND, Colour.BLACK);
        verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, Colour.BLACK);
    }

    @Test
    public void testHandleTextChange() throws Exception {
        instance.handleColourChange(colour);
        instance.handleNicklistChange(true);
        instance.handleTextChange(true);
        instance.handleAwayEvent(awayEvent);
        verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, colour);
        verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, colour);
        instance.handleColourChange(Colour.BLACK);
        instance.handleNicklistChange(true);
        instance.handleTextChange(false);
        instance.handleAwayEvent(awayEvent);
        verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, Colour.BLACK);
        verify(map, never()).put(ChannelClientProperty.TEXT_FOREGROUND, Colour.BLACK);
    }

    @Test
    public void testHandleAwayEvent() throws Exception {
        instance.handleNicklistChange(true);
        instance.handleTextChange(true);
        instance.handleBackEvent(backEvent);
        verify(map).remove(ChannelClientProperty.NICKLIST_FOREGROUND);
        verify(map).remove(ChannelClientProperty.TEXT_FOREGROUND);
    }

    @Test
    public void testHandleBackEvent() throws Exception {
        instance.handleColourChange(colour);
        instance.handleNicklistChange(true);
        instance.handleTextChange(true);
        instance.handleAwayEvent(awayEvent);
        verify(map).put(ChannelClientProperty.NICKLIST_FOREGROUND, colour);
        verify(map).put(ChannelClientProperty.TEXT_FOREGROUND, colour);
    }
}
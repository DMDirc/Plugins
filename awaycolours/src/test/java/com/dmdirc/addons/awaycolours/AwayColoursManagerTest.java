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

package com.dmdirc.addons.awaycolours;

import com.dmdirc.Channel;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.events.ChannelUserAwayEvent;
import com.dmdirc.events.ChannelUserBackEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.eventbus.EventBus;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.colours.Colour;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AwayColoursManagerTest {

    @Mock private EventBus eventBus;
    @Mock private AggregateConfigProvider config;
    @Mock private ConfigBinder binder;
    @Mock private ChannelUserAwayEvent awayEvent;
    @Mock private ChannelUserBackEvent backEvent;
    @Mock private GroupChatUser user;
    @Mock private Channel channel;
    @Mock private ColourManager colourManager;
    private AwayColoursManager instance;
    private String red;
    private Colour redColour;
    private String black;
    private Colour blackColour;

    @Before
    public void setUp() throws Exception {
        red = "red";
        black = "black";
        redColour = Colour.RED;
        blackColour = Colour.BLACK;
        when(awayEvent.getUser()).thenReturn(user);
        when(awayEvent.getChannel()).thenReturn(channel);
        when(backEvent.getUser()).thenReturn(user);
        when(backEvent.getChannel()).thenReturn(channel);
        when(config.getBinder()).thenReturn(binder);
        when(binder.withDefaultDomain(anyString())).thenReturn(binder);
        when(colourManager.getColourFromString(red, Colour.GRAY)).thenReturn(redColour);
        when(colourManager.getColourFromString(black, Colour.GRAY)).thenReturn(blackColour);
        instance = new AwayColoursManager(eventBus, config, "test", colourManager);
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
        instance.handleColourChange(red);
        instance.handleAwayEvent(awayEvent);
        verify(user).setDisplayProperty(DisplayProperty.FOREGROUND_COLOUR, redColour);
        instance.handleColourChange(black);
        instance.handleAwayEvent(awayEvent);
        verify(user).setDisplayProperty(DisplayProperty.FOREGROUND_COLOUR, blackColour);
    }

    @Test
    public void testHandleAwayEvent() throws Exception {
        instance.handleBackEvent(backEvent);
        verify(user).removeDisplayProperty(DisplayProperty.FOREGROUND_COLOUR);
    }

    @Test
    public void testHandleBackEvent() throws Exception {
        instance.handleColourChange(red);
        instance.handleAwayEvent(awayEvent);
        verify(user).setDisplayProperty(DisplayProperty.FOREGROUND_COLOUR, redColour);
    }
}
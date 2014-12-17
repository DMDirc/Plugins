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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.ClientFocusGainedEvent;
import com.dmdirc.events.ClientFocusLostEvent;

import java.awt.event.WindowEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EventTriggeringFocusListenerTest {

    @Mock private DMDircMBassador eventBus;
    @Mock private WindowEvent windowEvent;
    private EventTriggeringFocusListener instance;

    @Before
    public void setUp() throws Exception {
        instance = new EventTriggeringFocusListener(eventBus);
    }

    @Test
    public void testWindowGainedFocus() throws Exception {
        instance.windowGainedFocus(windowEvent);
        verify(eventBus).publish(any(ClientFocusGainedEvent.class));
    }

    @Test
    public void testWindowLostFocus() throws Exception {
        instance.windowLostFocus(windowEvent);
        verify(eventBus).publish(any(ClientFocusLostEvent.class));
    }
}
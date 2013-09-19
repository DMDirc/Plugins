/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.urlcatcher;

import com.dmdirc.FrameContainer;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.ui.messages.Styliser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UrlCatcherPluginTest {

    @Mock private FrameContainer container;
    @Mock private ConfigManager manager;
    @Mock private CommandController controller;

    @Before
    public void setupClass() {
        when(container.getConfigManager()).thenReturn(manager);
        final Styliser styliser = new Styliser(container);
        when(container.getStyliser()).thenReturn(styliser);
    }

    @Test
    public void testURLCounting() throws InvalidIdentityFileException {
        final UrlCatcherPlugin plugin = new UrlCatcherPlugin(controller);

        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                container, "This is a message - http://www.google.com/ foo");
        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                container, "This is a message - http://www.google.com/ foo");
        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                container, "This is a message - http://www.google.com/ foo");

        assertEquals(1, plugin.getURLS().size());
        assertEquals(3, (int) plugin.getURLS().get("http://www.google.com/"));
    }

    @Test
    public void testURLCatching() throws InvalidIdentityFileException {
        final UrlCatcherPlugin plugin = new UrlCatcherPlugin(controller);

        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                container, "http://www.google.com/ www.example.com foo://bar.baz");
        plugin.processEvent(CoreActionType.CHANNEL_MESSAGE, null,
                container, "No URLs here, no sir!");

        assertEquals(3, plugin.getURLS().size());
        assertTrue(plugin.getURLS().containsKey("http://www.google.com/"));
        assertTrue(plugin.getURLS().containsKey("www.example.com"));
        assertTrue(plugin.getURLS().containsKey("foo://bar.baz"));
    }

}

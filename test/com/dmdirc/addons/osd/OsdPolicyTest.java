/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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

package com.dmdirc.addons.osd;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class OsdPolicyTest {

    private OsdManager manager;

    @Before
    public void setup() {
        final ArrayList<OsdWindow> windows = new ArrayList<OsdWindow>();

        OsdWindow window = mock(OsdWindow.class);
        when(window.getY()).thenReturn(75);
        when(window.getHeight()).thenReturn(20);
        when(window.isVisible()).thenReturn(false);
        windows.add(window);

        window = mock(OsdWindow.class);
        when(window.getY()).thenReturn(100);
        when(window.getHeight()).thenReturn(20);
        when(window.isVisible()).thenReturn(true);
        windows.add(window);

        window = mock(OsdWindow.class);
        when(window.getY()).thenReturn(125);
        when(window.getHeight()).thenReturn(20);
        when(window.isVisible()).thenReturn(true);
        windows.add(window);

        window = mock(OsdWindow.class);
        when(window.getY()).thenReturn(150);
        when(window.getHeight()).thenReturn(20);
        when(window.isVisible()).thenReturn(true);
        windows.add(window);

        window = mock(OsdWindow.class);
        when(window.getY()).thenReturn(175);
        when(window.getHeight()).thenReturn(20);
        when(window.isVisible()).thenReturn(false);
        windows.add(window);

        manager = mock(OsdManager.class);
        when(manager.getWindowList()).thenReturn(windows);
    }

    @Test
    public void testOnTop() {
        assertEquals(42, OsdPolicy.ONTOP.getYPosition(manager, 42));
    }

    @Test
    public void testClose() {
        assertEquals(42, OsdPolicy.CLOSE.getYPosition(manager, 42));
        verify(manager).closeAll();
    }

    @Test
    public void testDown() {
        assertTrue(OsdPolicy.DOWN.getYPosition(manager, 75) > 170);
    }

    @Test
    public void testUp() {
        assertTrue(OsdPolicy.UP.getYPosition(manager, 175) < 80);
    }

}

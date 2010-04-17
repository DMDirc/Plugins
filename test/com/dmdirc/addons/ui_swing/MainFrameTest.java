/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.harness.ui.DMDircUITestCase;
import org.junit.Test;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class MainFrameTest extends DMDircUITestCase {
    
    private static Window window;

    static {
        SwingController controller = getMockedController();

        window = new Window(new MainFrame(controller));
        window.containsMenuBar().check();
    }
    
    @Test
    public void testNewServerDialog() {
        Window popup = WindowInterceptor.run(window.getMenuBar()
                .getMenu("Server").getSubMenu("New Server...").triggerClick());
        popup.titleEquals("DMDirc: Connect to a new server").check();
    }

    @Test
    public void testAboutDialog() {
        Window popup = WindowInterceptor.run(window.getMenuBar()
                .getMenu("Help").getSubMenu("About").triggerClick());
        popup.titleEquals("DMDirc: About").check();
    }

    @Test
    public void testFeedbackDialog() {
        Window popup = WindowInterceptor.run(window.getMenuBar()
                .getMenu("Help").getSubMenu("Send Feedback").triggerClick());
        popup.titleEquals("DMDirc: Feedback").check();
    }

    @Test
    public void testPreferencesDialog() {
        Window popup = WindowInterceptor.run(window.getMenuBar()
                .getMenu("Settings").getSubMenu("Preferences").triggerClick());
        popup.titleEquals("DMDirc: Preferences").check();
    }

    @Test
    public void testProfileManagerDialog() {
        Window popup = WindowInterceptor.run(window.getMenuBar()
                .getMenu("Settings").getSubMenu("Profile Manager").triggerClick());
        popup.titleEquals("DMDirc: Profile Editor").check();
    }

    @Test
    public void testActionsManagerDialog() {
        Window popup = WindowInterceptor.run(window.getMenuBar()
                .getMenu("Settings").getSubMenu("Actions Manager").triggerClick());
        popup.titleEquals("DMDirc: Actions Manager").check();
    }

    @Test
    public void testAliasManagerDialog() {
        Window popup = WindowInterceptor.run(window.getMenuBar()
                .getMenu("Settings").getSubMenu("Alias Manager").triggerClick());
        popup.titleEquals("DMDirc: Alias manager").check();
    }

    @Test
    public void testChannelServerSettings() {
        assertFalse(window.getMenuBar().getMenu("Channel").getSubMenu("Channel Settings").isEnabled());
    }

    @Test
    public void testServerServerSettings() {
        assertFalse(window.getMenuBar().getMenu("Server").getSubMenu("Server Settings").isEnabled());
    }
}

/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;

import javax.swing.JLabel;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.updater.Version;
import org.junit.Test;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.finder.ComponentMatcher;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ActionGroupInformationPanelTest extends UISpecTestCase {

    @Test
    public void testShouldDisplayNull() {
        assertFalse(new ActionGroupInformationPanel(null).shouldDisplay());
    }

    @Test
    public void testShouldDisplayNoDescription() {
        assertFalse(new ActionGroupInformationPanel(mock(ActionGroup.class)).shouldDisplay());
    }

    @Test
    public void testShouldDisplay() {
        final ActionGroup group = mock(ActionGroup.class);
        when(group.getDescription()).thenReturn("description");

        assertTrue(new ActionGroupInformationPanel(group).shouldDisplay());
    }

    @Test
    public void testAllLabels() {
        final ActionGroup group = mock(ActionGroup.class);
        when(group.getDescription()).thenReturn("description");
        when(group.getVersion()).thenReturn(new Version(17));
        when(group.getAuthor()).thenReturn("foo <bar@baz>");
        
        final Panel panel = new Panel(new ActionGroupInformationPanel(group));

        final UIComponent[] components = panel.getUIComponents(
                ComponentMatcher.ALL);
        assertEquals("Description", "description", ((TextLabel) components[0]
                .getAwtComponent()).getText().split("</?body.*?>")[1].trim());
        assertEquals("Author label", "Author: ", ((JLabel) components[1]
                .getAwtComponent()).getText());
        assertEquals("Author", "foo <bar@baz>", ((JLabel) components[2]
                .getAwtComponent()).getText());
        assertEquals("Version label", "Version: ", ((JLabel) components[3]
                .getAwtComponent()).getText());
        assertEquals("version", "17", ((JLabel) components[4].getAwtComponent())
                .getText());
    }

    @Test
    public void testNoAuthor() {
        final ActionGroup group = mock(ActionGroup.class);
        when(group.getDescription()).thenReturn("description");
        when(group.getVersion()).thenReturn(new Version(17));

        final Panel panel = new Panel(new ActionGroupInformationPanel(group));

        final UIComponent[] components = panel.getUIComponents(
                ComponentMatcher.ALL);
        assertEquals("Description", "description", ((TextLabel) components[0]
                .getAwtComponent()).getText().split("</?body.*?>")[1].trim());
        assertEquals("Version label", "Version: ", ((JLabel) components[3]
                .getAwtComponent()).getText());
        assertEquals("version", "17", ((JLabel) components[4].getAwtComponent())
                .getText());
    }

    @Test
    public void testNoVersion() {
        final ActionGroup group = mock(ActionGroup.class);
        when(group.getDescription()).thenReturn("description");
        when(group.getVersion()).thenReturn(null);
        when(group.getAuthor()).thenReturn("foo <bar@baz>");

        final Panel panel = new Panel(new ActionGroupInformationPanel(group));

        final UIComponent[] components = panel.getUIComponents(
                ComponentMatcher.ALL);
        assertEquals("Description", "description", ((TextLabel) components[0]
                .getAwtComponent()).getText().split("</?body.*?>")[1].trim());
        assertEquals("Author label", "Author: ", ((JLabel) components[1]
                .getAwtComponent()).getText());
        assertEquals("Author", "foo <bar@baz>", ((JLabel) components[2]
                .getAwtComponent()).getText());
    }

}

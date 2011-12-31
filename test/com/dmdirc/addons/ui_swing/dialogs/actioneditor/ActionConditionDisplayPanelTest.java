/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionComparison;
import com.dmdirc.actions.CoreActionComponent;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.components.ImageButton;
import com.dmdirc.addons.ui_swing.components.ImageToggleButton;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.harness.ui.ClassComponentMatcher;
import com.dmdirc.harness.ui.DMDircUITestCase;
import com.dmdirc.ui.IconManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;

import org.junit.Test;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.ToggleButton;
import org.uispec4j.UIComponent;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActionConditionDisplayPanelTest extends DMDircUITestCase {

    Pattern pattern = Pattern.compile(".+<body>(.+)</body>.+", Pattern.DOTALL);

    static {
        ActionManager.getActionManager().initialise();
    }

    public IconManager im() {
        final IconManager im = mock(IconManager.class);
        when(im.getIcon(anyString())).thenReturn(new ImageIcon());
        return im;
    }

    @Test
    public void testBlankInitialising() {
        final ActionConditionDisplayPanel panel = new ActionConditionDisplayPanel(im(),
                new ActionCondition(-1, null, null, ""),
                CoreActionType.CHANNEL_MESSAGE);
        final Panel test = new Panel(panel);

        final Matcher matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The ...", matcher.group(1).trim());
        final UIComponent[] components = test.getUIComponents(new ClassComponentMatcher(
                ActionConditionEditorPanel.class));
        assertNotNull(components);
        assertTrue(components.length == 1);
        assertTrue(components[0].isEnabled());
        assertTrue(components[0].isVisible());
        final ToggleButton toggle = test.getToggleButton(new ClassComponentMatcher(
                ImageToggleButton.class));
        assertTrue(toggle.isEnabled());
        assertTrue(toggle.isVisible());
        assertTrue(toggle.isSelected());
        final Button button = test.getButton(new ClassComponentMatcher(
                ImageButton.class));
        assertTrue(button.isEnabled());
        assertTrue(button.isVisible());
    }

    @Test
    public void testInitialising() {
        final ActionConditionDisplayPanel panel = new ActionConditionDisplayPanel(im(),
                new ActionCondition(2, CoreActionComponent.CHANNEL_NAME,
                CoreActionComparison.STRING_STARTSWITH, "abc"),
                CoreActionType.CHANNEL_MESSAGE);
        final Panel test = new Panel(panel);

        final Matcher matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The message's name starts with 'abc'", matcher.group(1).
                trim());
        final UIComponent[] components = test.getUIComponents(new ClassComponentMatcher(
                ActionConditionEditorPanel.class));
        assertNotNull(components);
        assertTrue(components.length == 1);
        assertTrue(components[0].isEnabled());
        assertFalse(components[0].isVisible());
        final ToggleButton toggle = test.getToggleButton(new ClassComponentMatcher(
                ImageToggleButton.class));
        assertTrue(toggle.isEnabled());
        assertTrue(toggle.isVisible());
        assertFalse(toggle.isSelected());
        final Button button = test.getButton(new ClassComponentMatcher(
                ImageButton.class));
        assertTrue(button.isEnabled());
        assertTrue(button.isVisible());
    }

    @Test
    public void testSetTrigger() {
        final ActionConditionDisplayPanel panel = new ActionConditionDisplayPanel(im(),
                new ActionCondition(2, CoreActionComponent.CHANNEL_NAME,
                CoreActionComparison.STRING_STARTSWITH, "abc"),
                CoreActionType.CHANNEL_MESSAGE);
        final Panel test = new Panel(panel);

        panel.setTrigger(null);

        Matcher matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("...", matcher.group(1).trim());
        UIComponent[] components = test.getUIComponents(new ClassComponentMatcher(
                ActionConditionEditorPanel.class));
        assertNotNull(components);
        assertTrue(components.length == 1);
        assertFalse(components[0].isEnabled());
        assertTrue(components[0].isVisible());
        ToggleButton toggle = test.getToggleButton(new ClassComponentMatcher(
                ImageToggleButton.class));
        assertTrue(toggle.isEnabled());
        assertTrue(toggle.isVisible());
        assertTrue(toggle.isSelected());

        panel.setTrigger(CoreActionType.CHANNEL_MESSAGE);

        matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The message's name starts with 'abc'", matcher.group(1).
                trim());
        components = test.getUIComponents(new ClassComponentMatcher(
                ActionConditionEditorPanel.class));
        assertNotNull(components);
        assertTrue(components.length == 1);
        assertTrue(components[0].isEnabled());
        assertFalse(components[0].isVisible());
        toggle = test.getToggleButton(new ClassComponentMatcher(
                ImageToggleButton.class));
        assertTrue(toggle.isEnabled());
        assertTrue(toggle.isVisible());
        assertFalse(toggle.isSelected());
    }

    @Test
    public void testDeleteCondition() {
        final ActionConditionDisplayPanel panel = new ActionConditionDisplayPanel(im(),
                new ActionCondition(2, CoreActionComponent.CHANNEL_NAME,
                CoreActionComparison.STRING_STARTSWITH, "abc"),
                CoreActionType.CHANNEL_MESSAGE);
        final Panel test = new Panel(panel);
        final ActionConditionRemovalListener listener = mock(
                ActionConditionRemovalListener.class);
        panel.addConditionListener(listener);

        final Button button = test.getButton(new ClassComponentMatcher(
                ImageButton.class));
        assertTrue(button.isEnabled());
        assertTrue(button.isVisible());
        button.click();

        verify(listener).conditionRemoved(panel);
    }

    @Test
    public void testEditCondition() {
        final ActionConditionDisplayPanel panel = new ActionConditionDisplayPanel(im(),
                new ActionCondition(2, CoreActionComponent.CHANNEL_NAME,
                CoreActionComparison.STRING_STARTSWITH, "abc"),
                CoreActionType.CHANNEL_MESSAGE);
        final Panel test = new Panel(panel);

        final UIComponent[] components = test.getUIComponents(new ClassComponentMatcher(
                ActionConditionEditorPanel.class));
        assertNotNull(components);
        assertTrue(components.length == 1);
        assertTrue(components[0].isEnabled());
        assertFalse(components[0].isVisible());

        final ToggleButton toggle = test.getToggleButton(new ClassComponentMatcher(
                ImageToggleButton.class));
        assertTrue(toggle.isEnabled());
        assertTrue(toggle.isVisible());
        toggle.click();

        assertTrue(components[0].isEnabled());
        assertTrue(components[0].isVisible());

        toggle.click();

        assertTrue(components[0].isEnabled());
        assertFalse(components[0].isVisible());
    }

    @Test
    public void testSetEnabled() {
        final ActionConditionDisplayPanel panel = new ActionConditionDisplayPanel(im(),
                new ActionCondition(2, CoreActionComponent.CHANNEL_NAME,
                CoreActionComparison.STRING_STARTSWITH, "abc"),
                CoreActionType.CHANNEL_MESSAGE);
        final Panel test = new Panel(panel);

        final UIComponent[] components = test.getUIComponents(new ClassComponentMatcher(
                ActionConditionEditorPanel.class));
        assertNotNull(components);
        assertTrue(components.length == 1);
        assertTrue(components[0].isEnabled());
        final ToggleButton toggle = test.getToggleButton(new ClassComponentMatcher(
                ImageToggleButton.class));
        assertTrue(toggle.isEnabled());
        final Button button = test.getButton(new ClassComponentMatcher(
                ImageButton.class));
        assertTrue(button.isEnabled());

        panel.setEnabled(false);
        assertFalse(components[0].isEnabled());
        assertFalse(toggle.isEnabled());
        assertFalse(button.isEnabled());
        panel.setEnabled(true);
        assertTrue(components[0].isEnabled());
        assertTrue(toggle.isEnabled());
        assertTrue(button.isEnabled());
    }

    @Test
    public void testUpdateSentence() {
        final ActionConditionDisplayPanel panel = new ActionConditionDisplayPanel(im(),
                new ActionCondition(-1, null, null, ""), null);
        final Panel test = new Panel(panel);

        Matcher matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("...", matcher.group(1).trim());
        panel.setTrigger(CoreActionType.CHANNEL_MESSAGE);
        matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The ...", matcher.group(1).trim());
        panel.setCondition(new ActionCondition(0, null, null, ""));
        matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The channel's ...", matcher.group(1).trim());
        panel.setCondition(new ActionCondition(0,
                CoreActionComponent.CHANNEL_NAME, null, ""));
        matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The channel's name ...", matcher.group(1).trim());
        panel.setCondition(new ActionCondition(0,
                CoreActionComponent.CHANNEL_NAME,
                CoreActionComparison.STRING_STARTSWITH, ""));
        matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The channel's name starts with ''", matcher.group(1).
                trim());
        panel.setCondition(new ActionCondition(0,
                CoreActionComponent.CHANNEL_NAME,
                CoreActionComparison.STRING_STARTSWITH, "abc"));
        matcher = pattern.matcher(test.getTextBox(new ClassComponentMatcher(
                TextLabel.class)).getText());
        matcher.find();
        assertEquals("The channel's name starts with 'abc'", matcher.group(1).
                trim());
    }
}

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

package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.harness.ui.ClassFinder;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.addons.ui_swing.dialogs.actioneditor.ActionEditorDialog;

import java.awt.Dialog;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.edt.GuiTask;
import org.fest.swing.finder.WindowFinder;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.junit.testcase.FestSwingJUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ActionsManagerDialogTest extends FestSwingJUnitTestCase {
    
    private DialogFixture window;

    @BeforeClass
    public static void setUpClass() throws InvalidIdentityFileException {
        IdentityManager.load();
        Main.setUI(new SwingController());
        ActionManager.init();
        ActionManager.loadActions();
    }

    @Override
    @Before
    public void onSetUp() {
        removeGroups();
    }

    @Override
    @After
    public void onTearDown() {
        GuiActionRunner.execute(new GuiTask() {

            @Override
            protected void executeInEDT() throws Throwable {
                close();
            }
        });

        removeGroups();
    }

    protected void close() {
        if (window != null) {
            window.cleanUp();
        }
    }
    
    protected void removeGroups() {
        if (ActionManager.getGroups().containsKey("amd-ui-test1")) {
            ActionManager.removeGroup("amd-ui-test1");
        }
        
        if (ActionManager.getGroups().containsKey("amd-ui-test2")) {
            ActionManager.removeGroup("amd-ui-test2");
        }
    }
    
    @Test
    public void testAddGroup() throws InterruptedException {
        setupWindow();
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Add")).click();
        
        DialogFixture newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
                
        newwin.requireVisible();
        assertEquals("DMDirc: New action group", newwin.target.getTitle());
        
        newwin.button(JButtonMatcher.withText("Cancel")).click();
        
        newwin.requireNotVisible();
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Add")).click();
        
        newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
        
        newwin.requireVisible();
        newwin.button(JButtonMatcher.withText("OK")).requireDisabled();
        
        newwin.textBox(new ClassFinder<JTextField>(JTextField.class,
                null)).enterText("amd-ui-test1");
        
        newwin.button(JButtonMatcher.withText("OK")).requireEnabled().click();
        
        window.list().requireSelectedItems("amd-ui-test1");

        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test1"));
    }
    
    @Test
    public void testDeleteGroup() {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
               
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Delete")).requireEnabled().click();

        DialogFixture newwin = WindowFinder.findDialog(StandardQuestionDialog.
                class).withTimeout(5000).using(window.robot);
        JButtonFixture button = newwin.button(JButtonMatcher.withText("No")).click();
        
        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test1"));
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Delete")).click();

        newwin = WindowFinder.findDialog(StandardQuestionDialog.
                class).withTimeout(5000).using(window.robot);
        button = newwin.button(JButtonMatcher.withText("Yes")).click();
        
        assertTrue(window.list().selection().length != 1 ||   
                !window.list().selection()[0].equals("amd-ui-test1"));
                
        assertFalse(ActionManager.getGroups().containsKey("amd-ui-test1"));
    }
    
    @Test
    public void testEnablingGroupButtons() {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
        
        window.list().selectItem("performs").requireSelectedItems("performs");
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Delete")).requireDisabled();
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Edit")).requireDisabled();
        
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Delete")).requireEnabled();
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Edit")).requireEnabled();
    }

    @Test
    public void testAddAction() {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        window.panel(new ClassFinder<ActionsGroupPanel>(ActionsGroupPanel.class, null))
                .button(JButtonMatcher.withText("Add")).click();

        DialogFixture newwin = WindowFinder.findDialog(ActionEditorDialog.class)
                .withTimeout(5000).using(window.robot);
        newwin.requireVisible();
    }
    
    public void editGroupCheck(final String button) {
        ActionManager.makeGroup("amd-ui-test1");
        setupWindow();
        
        window.list().selectItem("amd-ui-test1").requireSelectedItems("amd-ui-test1");
        window.panel(new ClassFinder<JPanel>(JPanel.class, "Groups"))
                .button(JButtonMatcher.withText("Edit")).requireEnabled().click();
        
        DialogFixture newwin = WindowFinder.findDialog(StandardInputDialog.class)
                .withTimeout(5000).using(window.robot);
                
        newwin.requireVisible();
        assertEquals("DMDirc: Edit action group", newwin.target.getTitle());
        
        assertEquals("amd-ui-test1", 
                newwin.textBox(new ClassFinder<JTextField>(JTextField.class,
                null)).target.getText());
        
        newwin.textBox(new ClassFinder<JTextField>(JTextField.class,
                null)).deleteText().enterText("amd-ui-test2");
        newwin.button(JButtonMatcher.withText(button)).requireEnabled().click();
    }
    
    @Test
    public void testEditGroupCancel() {
        editGroupCheck("Cancel");
        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test1"));
        assertFalse(ActionManager.getGroups().containsKey("amd-ui-test2"));
    }
    
    @Test
    public void testEditGroupOK() {
        editGroupCheck("OK");
        assertFalse(ActionManager.getGroups().containsKey("amd-ui-test1"));
        assertTrue(ActionManager.getGroups().containsKey("amd-ui-test2"));
    }
    
    protected void setupWindow() {
        final Dialog d = GuiActionRunner.execute(new GuiQuery<Dialog>() {
            @Override
            protected Dialog executeInEDT() throws Throwable {
                return ActionsManagerDialog.getActionsManagerDialog(null, null);
            }
        });
        robot().waitForIdle();

        window = new DialogFixture(robot(), d);
        window.show();
    }

}

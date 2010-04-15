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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.Main;
import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.ui_swing.ClassComponentMatcher;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.InvalidIdentityFileException;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.components.TextAreaInputField;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.messages.IRCDocument;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import static org.mockito.Mockito.*;

public class InputTextFrameTest extends UISpecTestCase {

    static InputTextFrame tf;
    static String text;

    static {
        try {
            IdentityManager.load();
        } catch (InvalidIdentityFileException ex) {
            //Ignore
        }
        IdentityManager.getAddonIdentity().setOption("test", "windowMenuItems",
                "1");
        IdentityManager.getAddonIdentity().setOption("test",
                "windowMenuScrollInterval", "1");
        IdentityManager.getAddonIdentity().setOption("test", "debugEDT", "false");
        IdentityManager.getAddonIdentity().setOption("test",
                "textpanebackground", "");
        IdentityManager.getAddonIdentity().setOption("test", "desktopbackground",
                "");
        Main.ensureExists(PluginManager.getPluginManager(), "tabcompletion");

        final IRCDocument document = mock(IRCDocument.class);
        @SuppressWarnings("unchecked")
        final WritableFrameContainer<CustomInputFrame> container = mock(
                WritableFrameContainer.class);
        final ConfigManager config = mock(ConfigManager.class);
        when(container.getDocument()).thenReturn(document);
        when(container.getConfigManager()).thenReturn(config);
        when(config.getOption(anyString(), anyString())).thenReturn("mirc");
        final SwingController controller = mock(SwingController.class);
        when(controller.getDomain()).thenReturn("test");
        final SwingWindowFactory wf = new SwingWindowFactory(controller);
        when(controller.getWindowFactory()).thenReturn(wf);
        final MainFrame mainFrame = new MainFrame(controller);
        when(controller.getMainFrame()).thenReturn(mainFrame);

        tf = new CustomInputFrame(controller, container);
        text = "line1\nline2";
    }

    @Test
    public void testPasteDialogContents() throws InterruptedException {
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertTrue(dialog.getTextBox(new ClassComponentMatcher(dialog,
                TextAreaInputField.class)).getText().equals(text));
    }

    @Test
    public void testPasteDialogWithTextBefore() throws InterruptedException {
        tf.getInputField().setText("testing:");
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertTrue(dialog.getTextBox(new ClassComponentMatcher(dialog,
                TextAreaInputField.class)).getText().equals("testing:" + text));
    }

    @Test
    public void testPasteDialogWithTextAfter() throws InterruptedException {
        tf.getInputField().setText(":testing");
        tf.getInputField().setCaretPosition(0);
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertTrue(dialog.getTextBox(new ClassComponentMatcher(dialog,
                TextAreaInputField.class)).getText().equals(text + ":testing"));
    }

    @Test
    public void testPasteDialogWithTextAround() throws InterruptedException {
        tf.getInputField().setText("testing::testing");
        tf.getInputField().setCaretPosition(8);
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertEquals("testing:" + text + ":testing",
                dialog.getTextBox(new ClassComponentMatcher(dialog,
                TextAreaInputField.class)).getText());
    }

    @Test
    public void testPasteDialogWithSelection() {
        tf.getInputField().setText("testing:SELECTED:testing");
        tf.getInputField().setSelectionStart(8);
        tf.getInputField().setSelectionEnd(16);
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertEquals("testing:" + text + ":testing",
                dialog.getTextBox(new ClassComponentMatcher(dialog,
                TextAreaInputField.class)).getText());
    }

    /**
     * Creates a new paste dialog with the specified text for the specified
     * frame.
     *
     * @param frame Parent frame
     * @param text Text to "paste"
     *
     * @return Wrapped Dialog
     */
    private Window getDialog() {
        return WindowInterceptor.run(new PasteDialogTrigger(tf, text));
    }

    /**
     * Creates a new paste dialog for the specified frame with the specified text.
     */
    private class PasteDialogTrigger implements Trigger {

        private InputTextFrame frame;
        private String text;

        /**
         * Creates a new paste dialog for the specified frame with the specified text.
         *
         * @param frame Parent frame
         * @param text Text to "paste"
         */
        public PasteDialogTrigger(final InputTextFrame frame, final String text) {
            this.frame = frame;
            this.text = text;
        }

        /** {@inheritDoc} */
        @Override
        public void run() throws Exception {
            frame.doPaste(text);
        }
    }

}

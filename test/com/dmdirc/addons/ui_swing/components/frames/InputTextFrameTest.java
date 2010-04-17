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

import com.dmdirc.harness.ui.ClassComponentMatcher;
import com.dmdirc.harness.ui.DMDircUITestCase;
import com.dmdirc.addons.ui_swing.components.TextAreaInputField;
import org.junit.Test;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class InputTextFrameTest extends DMDircUITestCase {

    static InputTextFrame tf;
    static String text;

    static {
        tf = new CustomInputFrame(getMockedControllerAndMainFrame(),
                getMockedContainer());
        text = "line1\nline2";
    }

    @Test
    public void testPasteDialogContents() throws InterruptedException {
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertTrue(dialog.getTextBox(new ClassComponentMatcher(
                TextAreaInputField.class)).getText().equals(text));
    }

    @Test
    public void testPasteDialogWithTextBefore() throws InterruptedException {
        tf.getInputField().setText("testing:");
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertTrue(dialog.getTextBox(new ClassComponentMatcher(
                TextAreaInputField.class)).getText().equals("testing:" + text));
    }

    @Test
    public void testPasteDialogWithTextAfter() throws InterruptedException {
        tf.getInputField().setText(":testing");
        tf.getInputField().setCaretPosition(0);
        final Window dialog = getDialog();
        dialog.titleEquals("DMDirc: Multi-line paste").check();
        dialog.getButton("Edit").click();
        assertTrue(dialog.getTextBox(new ClassComponentMatcher(
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
                dialog.getTextBox(new ClassComponentMatcher(
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
                dialog.getTextBox(new ClassComponentMatcher(
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

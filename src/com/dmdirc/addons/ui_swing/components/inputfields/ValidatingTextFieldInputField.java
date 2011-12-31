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

package com.dmdirc.addons.ui_swing.components.inputfields;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.colours.ColourPickerDialog;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.interfaces.ui.InputField;
import com.dmdirc.util.validators.Validator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

/**
 * Extended ValidatingTextField that adds Inputfield support.
 */
public class ValidatingTextFieldInputField extends ValidatingJTextField
        implements InputField {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Colour picker. */
    private ColourPickerDialog colourPicker;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Creates a new text field with the specified validator.
     *
     * @param controller Swing controller
     * @param validator Validator for this textfield
     */
    public ValidatingTextFieldInputField(final SwingController controller,
            final Validator<String> validator) {
        this(controller, new JTextField(), validator);
    }

    /**
     * Creates a new text field with the specified validator.
     *
     * @param controller Swing controller
     * @param validator Validator for this textfield
     * @param textField Textfield to use as a base
     */
    public ValidatingTextFieldInputField(final SwingController controller,
            final JTextField textField, final Validator<String> validator) {
        super(controller.getIconManager(), textField, validator);
        this.controller = controller;
    }

    /** {@inheritDoc} */
    @Override
    public void addActionListener(final ActionListener listener) {
        // Ignore request - we don't handle returns for text areas
    }

    /** {@inheritDoc} */
    @Override
    public void removeActionListener(final ActionListener listener) {
        // Ignore request - we don't handle returns for text areas
    }

    /** {@inheritDoc} */
    @Override
    public void showColourPicker(final boolean irc, final boolean hex) {
        if (controller.getGlobalConfig().getOptionBool("general",
                "showcolourdialog")) {
            colourPicker = new ColourPickerDialog(controller.getIconManager(),
                    irc, hex);
            colourPicker.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    try {
                        getDocument().insertString(getCaretPosition(),
                                actionEvent.getActionCommand(), null);
                    } catch (final BadLocationException ex) {
                        //Ignore, wont happen
                    }
                    colourPicker.dispose();
                    colourPicker = null;
                }
            });
            colourPicker.display();
            colourPicker.setLocation((int) getLocationOnScreen().getX(),
                    (int) getLocationOnScreen().getY() -
                    colourPicker.getHeight());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void hideColourPicker() {
        if (colourPicker != null) {
            colourPicker.dispose();
            colourPicker = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getCaretPosition() {
        return getTextField().getCaretPosition();
    }

    /** {@inheritDoc} */
    @Override
    public void setCaretPosition(final int position) {
        getTextField().setCaretPosition(position);
    }

}

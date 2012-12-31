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

package com.dmdirc.addons.ui_swing.components.inputfields;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.colours.ColourPickerDialog;
import com.dmdirc.interfaces.ui.InputField;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.text.BadLocationException;

/**
 * JTextField implementing InputField.
 */
public class TextFieldInputField extends JTextField implements InputField,
        PropertyChangeListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Colour picker. */
    protected ColourPickerDialog colourPicker;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Creates a new text field input field.
     *
     * @param controller Swing controller
     */
    public TextFieldInputField(final SwingController controller) {
        super();

        this.controller = controller;
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener(this);
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
                    (int) getLocationOnScreen().getY()
                    - colourPicker.getHeight());
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
    public void propertyChange(final PropertyChangeEvent evt) {
        if (!isFocusOwner()) {
            hideColourPicker();
        }
    }
}

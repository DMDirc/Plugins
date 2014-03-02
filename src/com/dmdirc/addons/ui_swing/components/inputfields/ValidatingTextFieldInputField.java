/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.components.colours.ColourPickerDialog;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.InputField;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.validators.Validator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    /** The icon manager to use for validation and dialog icons. */
    private final IconManager iconManager;
    /** The manager to use for colour input. */
    private final ColourManager colourManager;
    /** The config to read settings from. */
    private final AggregateConfigProvider globalConfig;

    /**
     * Creates a new text field with the specified validator.
     *
     * @param iconManager   The icon manager to use for validation and dialog icons.
     * @param colourManager The manager to use for colour input.
     * @param globalConfig  The config to read settings from.
     * @param validator     Validator for this textfield
     */
    public ValidatingTextFieldInputField(
            final IconManager iconManager,
            final ColourManager colourManager,
            final AggregateConfigProvider globalConfig,
            final Validator<String> validator) {
        this(iconManager, colourManager, globalConfig, "", validator);
    }

    /**
     * Creates a new text field with the specified validator.
     *
     * @param iconManager   The icon manager to use for validation and dialog icons.
     * @param colourManager The manager to use for colour input.
     * @param globalConfig  The config to read settings from.
     * @param validator     Validator for this textfield
     * @param text          Text to use
     */
    public ValidatingTextFieldInputField(
            final IconManager iconManager,
            final ColourManager colourManager,
            final AggregateConfigProvider globalConfig,
            final String text,
            final Validator<String> validator) {
        super(iconManager, text, validator);
        this.iconManager = iconManager;
        this.colourManager = colourManager;
        this.globalConfig = globalConfig;
    }

    
    @Override
    public void addActionListener(final ActionListener listener) {
        // Ignore request - we don't handle returns for text areas
    }

    
    @Override
    public void removeActionListener(final ActionListener listener) {
        // Ignore request - we don't handle returns for text areas
    }

    
    @Override
    public void showColourPicker(final boolean irc, final boolean hex) {
        if (globalConfig.getOptionBool("general", "showcolourdialog")) {
            colourPicker = new ColourPickerDialog(this, colourManager, iconManager, irc, hex);
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
            colourPicker.setVisible(true);
            colourPicker.setLocation((int) getLocationOnScreen().getX(),
                    (int) getLocationOnScreen().getY() - colourPicker.getHeight());
        }
    }

    
    @Override
    public void hideColourPicker() {
        if (colourPicker != null) {
            colourPicker.dispose();
            colourPicker = null;
        }
    }

}

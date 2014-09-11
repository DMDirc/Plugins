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
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.InputField;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

/**
 * JTextArea implementing InputField
 */
public class TextAreaInputField extends JTextArea implements InputField,
        PropertyChangeListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Colour picker. */
    protected ColourPickerDialog colourPicker;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Global config. */
    private final AggregateConfigProvider config;

    /**
     * Creates a new text area with the specified number of rows and columns.
     *
     * @param iconManager Icon manager
     * @param config      Config to read settings from
     * @param rows        The number of rows to use
     * @param columns     The number of columns to use
     */
    public TextAreaInputField(final IconManager iconManager,
            final AggregateConfigProvider config, final int rows, final int columns) {
        super(rows, columns);
        this.iconManager = iconManager;
        this.config = config;
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener(this);
    }

    /**
     * Creates a new text area containing the specified text.
     *
     * @param iconManager Icon manager
     * @param config      Config to read settings from
     * @param text        The text to contain initially
     */
    public TextAreaInputField(final IconManager iconManager,
            final AggregateConfigProvider config, final String text) {
        super(text);
        this.iconManager = iconManager;
        this.config = config;
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener(this);
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
        if (config.getOptionBool("general", "showcolourdialog")) {
            colourPicker = new ColourPickerDialog(TextAreaInputField.this,
                    new ColourManager(config), iconManager, irc, hex);
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

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (!isFocusOwner()) {
            hideColourPicker();
        }
    }

}

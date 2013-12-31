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

package com.dmdirc.addons.ui_swing.components.colours;

import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

/**
 * Colour picker dialog.
 */
public class ColourPickerDialog extends JDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Colour chooser panel. */
    private final ColourPickerPanel colourChooser;
    /** Object to center dialog on. */
    private final Component centerObject;

    /**
     * Creates a new instance of ColourPickerDialog.
     *
     * @param centerObject Object to center dialog on
     * @param colourManager The colour manager to use to parse colours.
     * @param iconManager Icon manager
     * @param showIRC show irc colours
     * @param showHex show hex colours
     */
    public ColourPickerDialog(final Component centerObject,
            final ColourManager colourManager,
            final IconManager iconManager,
            final boolean showIRC, final boolean showHex) {
        this(centerObject, colourManager, iconManager, showIRC, showHex, null);
    }

    /**
     * Creates a new instance of ColourPickerDialog.
     *
     * @param centerObject Object to center dialog on
     * @param colourManager The colour manager to use to parse colours.
     * @param iconManager Icon manager
     * @param showIRC show irc colours
     * @param showHex show hex colours
     * @param window Parent window
     *
     * @since 0.6
     */
    public ColourPickerDialog(final Component centerObject,
            final ColourManager colourManager, final IconManager iconManager,
            final boolean showIRC, final boolean showHex, final Window window) {
        super(window, ModalityType.MODELESS);
        this.centerObject = centerObject;
        setIconImage(iconManager.getImage("icon"));

        colourChooser = new ColourPickerPanel(colourManager, showIRC, showHex);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        add(colourChooser);
        setResizable(false);
        setFocusableWindowState(false);

        setWindow(window);
    }

    /**
     * Adds an actions listener to this dialog.
     *
     * @param listener the listener to add
     */
    public void addActionListener(final ActionListener listener) {
        colourChooser.addActionListener(listener);
    }

    /**
     * Sets the Parent window.
     *
     * @param window Parent window
     */
    public void setWindow(final Window window) {
        if (window != null) {
            window.addWindowListener(new WindowAdapter() {

                /** {@inheritDoc} */
                @Override
                public void windowClosed(final WindowEvent e) {
                    dispose();
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        if (visible) {
            setSize(colourChooser.getPreferredSize());
            setLocationRelativeTo(centerObject);
        }
    }
}

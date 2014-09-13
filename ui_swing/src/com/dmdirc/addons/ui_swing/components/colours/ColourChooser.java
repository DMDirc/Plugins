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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.util.colours.Colour;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import net.miginfocom.swing.MigLayout;

/**
 * Colour chooser widget.
 */
public class ColourChooser extends JPanel implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Edit button. */
    private final JButton editButton;
    /** Panel to show the colour preview. */
    private final JPanel previewPanel;
    /** Event listeners. */
    private final EventListenerList listeners;
    /** Icon manager. */
    private final IconManager iconManager;
    /** The colour manager to use to parse colours. */
    private final ColourManager colourManager;
    /** Colours picking dialog. */
    private ColourPickerDialog cpd;
    /** show irc colours. */
    private final boolean showIRC;
    /** show hex colours. */
    private final boolean showHex;
    /** The value of this component. */
    private String value;
    /** Action command. */
    private String command;
    /** Parent window. */
    private Window window;

    /**
     * Creates a new instance of ColourChooser.
     *
     * @param colourManager The colour manager to use to parse colours.
     * @param iconManager   Icon manager
     * @param initialColour initial colour
     * @param ircColours    show irc colours
     * @param hexColours    show hex colours
     *
     * @since 0.6
     */
    public ColourChooser(
            final ColourManager colourManager, final IconManager iconManager,
            final String initialColour, final boolean ircColours,
            final boolean hexColours) {

        this.colourManager = colourManager;
        this.iconManager = iconManager;
        showIRC = ircColours;
        showHex = hexColours;
        value = initialColour;
        listeners = new EventListenerList();
        command = "";

        editButton = new JButton("Edit");
        if (UIUtilities.isWindowsUI()) {
            editButton.setMargin(new Insets(2, 4, 2, 4));
        } else {
            editButton.setMargin(new Insets(0, 2, 0, 2));
        }

        editButton.addActionListener(this);

        previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(40, 10));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        setOpaque(false);

        setLayout(new MigLayout("ins 0, fill"));

        add(previewPanel, "growx, pushx, sgy all");
        add(editButton, "sgy all");

        updateColour(initialColour);
    }

    /**
     * Returns the selected colour from this component.
     *
     * @return This components colour, as a string
     */
    public String getColour() {
        return value;
    }

    /**
     * Sets the selected colour for this component.
     *
     * @param newValue New colour
     */
    public void setColour(final String newValue) {
        value = newValue;
        updateColour(value);
    }

    /** Sets the colour back to white. */
    public void clearColour() {
        value = "ffffff";
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setToolTipText("");
    }

    /**
     * Updates the colour panel.
     *
     * @param newColour The new colour to use.
     */
    private void updateColour(final String newColour) {
        if (newColour == null || newColour.isEmpty()) {
            previewPanel.setBackground(Color.WHITE);
            previewPanel.setToolTipText("");
        } else {
            previewPanel.setBackground(UIUtilities.convertColour(
                    colourManager.getColourFromString(newColour, Colour.WHITE)));
            previewPanel.setToolTipText(newColour);
        }
    }

    /**
     * {@inheritDoc}.
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == editButton) {
            cpd = new ColourPickerDialog(editButton, colourManager, iconManager, showIRC,
                    showHex, window);
            cpd.addActionListener(this);
            cpd.setVisible(true);
        } else {
            value = e.getActionCommand();
            updateColour(e.getActionCommand());
            fireActionPerformed();
            cpd.dispose();
        }
    }

    /**
     * Sets this colour choosers action command.
     *
     * @param command New action command
     */
    public void setActionCommand(final String command) {
        this.command = command;
    }

    /**
     * Adds a ActionListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addActionListener(final ActionListener listener) {
        synchronized (listeners) {
            if (listener == null) {
                return;
            }
            listeners.add(ActionListener.class, listener);
        }
    }

    /**
     * Removes a ActionListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeActionListener(final ActionListener listener) {
        listeners.remove(ActionListener.class, listener);
    }

    /**
     * Fires the action performed method on all listeners.
     */
    protected void fireActionPerformed() {
        final Object[] localListenerList = listeners.getListenerList();
        for (int i = 0; i < localListenerList.length; i += 2) {
            if (localListenerList[i] == ActionListener.class) {
                ((ActionListener) localListenerList[i + 1]).actionPerformed(
                        new ActionEvent(this,
                                ActionEvent.ACTION_PERFORMED, command));
            }
        }
    }

    /**
     * Sets the Parent window.
     *
     * @param window Parent window
     */
    public void setWindow(final Window window) {
        this.window = window;
    }

}

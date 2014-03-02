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
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.collections.ListenerList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Colour chooser widget.
 */
public class OptionalColourChooser extends JPanel implements
        ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    private String value;
    /** Our listeners. */
    private final ListenerList listeners = new ListenerList();
    /** Icon manager. */
    private final IconManager iconManager;
    /** The colour manager to use to parse colours. */
    private final ColourManager colourManager;
    /** Enabled checkbox. */
    private final JCheckBox enabled;
    /** Edit button. */
    private final JButton editButton;
    /** Panel to show the colour preview. */
    private final JPanel previewPanel;
    /** Colours picking dialog. */
    private ColourPickerDialog cpd;
    /** show IRC colours. */
    private final boolean showIRC;
    /** show hex colours. */
    private final boolean showHex;
    /** Parent window. */
    private Window window = null;

    /**
     * Creates a new instance of ColourChooser.
     *
     * @param iconManager   Icon manager
     * @param colourManager The colour manager to use to parse colours.
     * @param initialColour Initial colour
     * @param initialState  Initial state
     * @param ircColours    Show IRC colours
     * @param hexColours    Show hex colours
     *
     * @since 0.6
     */
    public OptionalColourChooser(
            final IconManager iconManager, final ColourManager colourManager,
            final String initialColour,
            final boolean initialState, final boolean ircColours,
            final boolean hexColours) {
        this.iconManager = iconManager;
        this.colourManager = colourManager;
        showIRC = ircColours;
        showHex = hexColours;

        editButton = new JButton("Edit");
        if (UIUtilities.isWindowsUI()) {
            editButton.setMargin(new Insets(2, 4, 2, 4));
        } else {
            editButton.setMargin(new Insets(0, 2, 0, 2));
        }
        editButton.addActionListener(this);
        if (!initialState) {
            editButton.setEnabled(false);
        }

        previewPanel = new JPanel();
        previewPanel.setPreferredSize(new Dimension(40, 10));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        enabled = new JCheckBox();
        enabled.setPreferredSize(new Dimension(40, 40));
        enabled.setSelected(initialState);
        enabled.addActionListener(this);
        enabled.setOpaque(false);
        setOpaque(false);

        setLayout(new MigLayout("fill, ins 0"));

        add(enabled, "sgy all");
        add(previewPanel, "growx, pushx, sgy all");
        add(editButton, "sgy all");

        if (initialColour.contains(":")) {
            value = initialColour.substring(initialColour.indexOf(':') + 1);
        } else {
            value = initialColour;
        }
        updateColour(value);
    }

    /** Sets the colour back to white. */
    public void clearColour() {
        value = "ffffff";
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setToolTipText("");
    }

    
    @Override
    public boolean isEnabled() {
        return enabled.isSelected();
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
     * Updates the colour panel.
     *
     * @param newColour The new colour to use.
     */
    private void updateColour(final String newColour) {
        if (newColour.isEmpty()) {
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
        } else if (e.getSource() == enabled) {
            editButton.setEnabled(enabled.isSelected());
            fireActionEvent();
        } else {
            value = e.getActionCommand();
            updateColour(e.getActionCommand());
            fireActionEvent();
            cpd.dispose();
        }
    }

    /**
     * Adds an action listener to this optional colour chooser. Action listeners are notified
     * whenever the state changes in some way.
     *
     * @param l The listener to be added
     */
    public void addActionListener(final ActionListener l) {
        listeners.add(ActionListener.class, l);
    }

    /**
     * Informs all action listeners that an action has occurred.
     */
    protected void fireActionEvent() {
        for (final ActionListener listener : listeners
                .get(ActionListener.class)) {
            listener.actionPerformed(new ActionEvent(this, 1, "stuffChanged"));
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

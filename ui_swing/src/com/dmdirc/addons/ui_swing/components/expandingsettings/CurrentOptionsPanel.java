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

package com.dmdirc.addons.ui_swing.components.expandingsettings;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.ImageButton;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Current options panel.
 */
public class CurrentOptionsPanel extends JPanel implements
        ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Parent settings panel. */
    private final SettingsPanel parent;
    /** Current options to display. */
    private final List<JComponent> settings;
    /** Icon manager. */
    private final IconManager iconManager;

    /**
     * Creates a new instance of CurrentOptionsPanel.
     *
     * @param iconManager IconManager
     * @param parent      Parent settings panel.
     */
    protected CurrentOptionsPanel(final IconManager iconManager, final SettingsPanel parent) {

        this.iconManager = iconManager;
        this.parent = parent;

        setOpaque(UIUtilities.getTabbedPaneOpaque());
        settings = new ArrayList<>();
    }

    /** Clears all the current options. */
    protected void clearOptions() {
        settings.clear();
        populateCurrentSettings();
    }

    /**
     * Adds a current option.
     *
     * @param setting Setting to add
     */
    protected void addOption(final JComponent setting) {
        settings.add(setting);

        populateCurrentSettings();
    }

    /**
     * Deletes a current option.
     *
     * @param setting Setting to remove
     */
    protected void delOption(final JComponent setting) {
        settings.remove(setting);
        populateCurrentSettings();
    }

    /**
     * Adds an option to the current options pane.
     *
     * @param configName  config option name
     * @param displayName config option display name
     * @param panel       parent panel
     * @param component   Option component to add
     */
    private void addCurrentOption(final JComponent component) {
        final JLabel label = new JLabel();
        final ImageButton<JComponent> button = new ImageButton<>(
                component.getName(), iconManager.getIcon("close-inactive"),
                iconManager.getIcon("close-active"));
        button.setObject(component);

        label.setText(component.getName() + ": ");
        label.setLabelFor(component);

        button.addActionListener(this);

        add(label);
        add(component, "");
        add(button, "wrap");
    }

    /** Populates the current settings. */
    protected void populateCurrentSettings() {
        setVisible(false);

        setLayout(new MigLayout("fillx, aligny top, wmax 100%"));

        removeAll();

        for (final JComponent setting : settings) {
            addCurrentOption(setting);
        }

        setVisible(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(final ActionEvent e) {
        final JComponent setting = ((ImageButton<JComponent>) e.getSource()).getObject();
        delOption(setting);
        parent.addAddableOption(setting);
    }

}

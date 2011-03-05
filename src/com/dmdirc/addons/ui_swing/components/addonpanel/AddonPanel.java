/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.components.addonpanel;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.addonbrowser.BrowserWindow;
import com.dmdirc.addons.ui_swing.components.renderers.AddonCellRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.config.prefs.PreferencesInterface;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

/**
 * Addon panel, base class for displaying and managing addons.
 */
public abstract class AddonPanel extends JPanel implements
        ActionListener, ListSelectionListener, PreferencesInterface {

    /** Button to enable/disable addon. */
    protected JButton toggleButton;
    /** List of addons. */
    protected JTable addonList;
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** Parent Window. */
    private final Window parentWindow;
    /** Swing Controller. */
    private final SwingController controller;
    /** Addon list scroll pane. */
    private JScrollPane scrollPane;
    /** Currently selected addon. */
    private int selectedAddon;
    /** Blurb label. */
    private TextLabel blurbLabel;

    /**
     * Creates a new instance of AddonPanel
     *
     * @param parentWindow Parent window
     * @param controller Swing Controller
     */
    public AddonPanel(final Window parentWindow,
            final SwingController controller) {
        super();

        this.parentWindow = parentWindow;
        this.controller = controller;

        initComponents();
        addListeners();
        layoutComponents();

        addonList.getSelectionModel().setSelectionInterval(0, 0);
        selectedAddon = 0;
    }

    /** Initialises the components. */
    private void initComponents() {
        addonList = new JTable(new DefaultTableModel(
                new Object[]{"Addon", }, 0)) {

            /** Serial Version UID. */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }

        };
        addonList.setDefaultRenderer(Object.class,
                new AddonCellRenderer());
        addonList.setTableHeader(null);
        addonList.setShowGrid(false);
        addonList.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        addonList.getSelectionModel().clearSelection();

        scrollPane = new JScrollPane(new JLabel("Loading " + getTypeName()
                + "..."));
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        toggleButton = new JButton("Enable");
        toggleButton.setEnabled(false);

        blurbLabel = new TextLabel(getTypeName().substring(0, 1).toUpperCase()
                + getTypeName().substring(1) + " allow you to extend the "
                + "functionality of DMDirc.");

        /** {@inheritDoc}. */
        new LoggingSwingWorker<Object, Object>() {

            /** {@inheritDoc}. */
            @Override
            protected Object doInBackground() {
                return populateList(addonList);
            }

            /** {@inheritDoc}. */
            @Override
            protected void done() {
                super.done();
                scrollPane.setViewportView(addonList);
            }
        }.executeInExecutor();
    }

    /** Lays out the dialog. */
    private void layoutComponents() {
        if (controller == null) {
            setLayout(new MigLayout("ins 0, fill, hmax " + 300));
        } else {
            setLayout(new MigLayout("ins 0, fill, hmax "
                    + controller.getPrefsDialog().getPanelHeight()));
        }

        add(blurbLabel, "wrap 10, growx, pushx");

        add(scrollPane, "wrap 5, grow, push");

        add(toggleButton, "split 2, growx, pushx, sg button");

        final JButton button = new JButton("Get more " + getTypeName());
        button.addActionListener(this);
        add(button, "growx, pushx, sg button");
    }

    /**
     * Populates the addon list returning it when complete.
     *
     * @return Populated table
     */
    protected abstract JTable populateList(final JTable table);

    /**
     * Returns the name of the type of addon being handled.
     *
     * @return Addon type name
     */
    protected abstract String getTypeName();

    /** Adds listeners to components. */
    private void addListeners() {
        toggleButton.addActionListener(this);
        addonList.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e The event related to this action.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == toggleButton && selectedAddon >= 0) {
            final AddonToggle addonToggle = (AddonToggle) ((AddonCell)
                    addonList.getModel().getValueAt(addonList
                    .getSelectedRow(), 0)).getObject();

            addonToggle.toggle();

            if (addonToggle.getState()) {
                toggleButton.setText("Disable");
            } else {
                toggleButton.setText("Enable");
            }

            addonList.repaint();
        } else if (e.getSource() != toggleButton) {
            new BrowserWindow(parentWindow);
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            return;
        }
        final int selected = addonList.getSelectionModel()
                .getLeadSelectionIndex();
        if (selected == -1) {
            return;
        }
        final AddonToggle addonToggle = (AddonToggle) ((AddonCell)
                ((List) ((DefaultTableModel) addonList.getModel())
                .getDataVector().elementAt(selected)).get(0)).getObject();
        toggleButton.setEnabled(true);

        if (addonToggle.getState()) {
            toggleButton.setEnabled(addonToggle.isUnloadable());
            toggleButton.setText("Disable");
        } else {
            toggleButton.setText("Enable");
        }
        selectedAddon = selected;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        if (addonList.getRowCount() == 0) {
            return;
        }
        for (int i = 1; i < addonList.getRowCount(); i++) {
            addonList.getModel().getColumnCount();
            ((AddonToggle) ((AddonCell) addonList.getModel()
                    .getValueAt(i, 0)).getObject()).apply();
        }
    }
}

/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.pluginpanel;

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.addonbrowser.DownloaderWindow;
import com.dmdirc.addons.ui_swing.components.renderers.AddonCellRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
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
 * Plugin manager dialog. Allows the user to manage their plugins.
 */
public final class PluginPanel extends JPanel implements
        ActionListener, ListSelectionListener, PreferencesInterface,
        com.dmdirc.interfaces.ActionListener {

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
    /** List of plugins. */
    private JTable pluginList;
    /** plugin list scroll pane. */
    private JScrollPane scrollPane;
    /** Button to enable/disable plugin. */
    private JButton toggleButton;
    /** Currently selected plugin. */
    private int selectedPlugin;
    /** Blurb label. */
    private TextLabel blurbLabel;
    /** Do we refresh on plugin reload? */
    private boolean pluginRefresh = true;

    /**
     * Creates a new instance of PluginDialog.
     *
     * @param parentWindow Parent window
     * @param controller Swing Controller
     */
    public PluginPanel(final Window parentWindow,
            final SwingController controller) {
        super();

        this.parentWindow = parentWindow;
        this.controller = controller;

        initComponents();
        addListeners();
        layoutComponents();

        pluginList.getSelectionModel().setSelectionInterval(0, 0);
        selectedPlugin = 0;
    }

    /** Initialises the components. */
    private void initComponents() {
        pluginList = new JTable(new DefaultTableModel(
                new Object[]{"Plugin", }, 0)) {

            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
            
        };
        pluginList.setDefaultRenderer(Object.class,
                new AddonCellRenderer());
        pluginList.setTableHeader(null);
        pluginList.setShowGrid(false);
        pluginList.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        pluginList.getSelectionModel().clearSelection();

        scrollPane = new JScrollPane(new JLabel("Loading plugins..."));
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        toggleButton = new JButton("Enable");
        toggleButton.setEnabled(false);

        blurbLabel = new TextLabel(
                "Plugins allow you to extend the functionality of DMDirc.");

        /** {@inheritDoc}. */
        new LoggingSwingWorker<Object, Object>() {

            /** {@inheritDoc}. */
            @Override
            protected Object doInBackground() {
                return populateList();
            }

            /** {@inheritDoc}. */
            @Override
            protected void done() {
                super.done();
                scrollPane.setViewportView(pluginList);
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

        final JButton button = new JButton("Get more plugins");
        button.addActionListener(this);
        add(button, "growx, pushx, sg button");
    }

    /**
     * Populates the plugins list with plugins from the plugin manager.
     *
     * @return Populated list
     */
    private JTable populateList() {
        pluginRefresh = false;
        final List<PluginInfo> list =
                PluginManager.getPluginManager().getPossiblePluginInfos(true);
        pluginRefresh = true;
        Collections.sort(list);

        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ((DefaultTableModel) pluginList.getModel()).setNumRows(0);
                for (PluginInfo plugin : list) {
                    ((DefaultTableModel) pluginList.getModel()).addRow(
                            new AddonCell[]{
                        new AddonCell(new PluginInfoToggle(plugin)), });
                }
                pluginList.repaint();
            }
        });
        return pluginList;
    }

    /** Adds listeners to components. */
    private void addListeners() {
        toggleButton.addActionListener(this);
        pluginList.getSelectionModel().addListSelectionListener(this);
        ActionManager.addListener(this, CoreActionType.PLUGIN_REFRESH);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e The event related to this action.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == toggleButton && selectedPlugin >= 0) {
            final PluginInfoToggle pluginInfo = (PluginInfoToggle) ((AddonCell) 
                    pluginList.getModel().getValueAt(pluginList
                    .getSelectedRow(), 0)).getObject();

            pluginInfo.toggle();

            if (pluginInfo.getState()) {
                toggleButton.setText("Disable");
            } else {
                toggleButton.setText("Enable");
            }

            pluginList.repaint();
        } else if (e.getSource() != toggleButton) {
            new DownloaderWindow(parentWindow, controller);
        }
    }

    /** {@inheritDoc}. */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            return;
        }
        final int selected = pluginList.getSelectionModel()
                .getLeadSelectionIndex();
        if (selected == -1) {
            return;
        }
        final PluginInfoToggle pluginInfo = (PluginInfoToggle) ((AddonCell)
                ((List) ((DefaultTableModel) pluginList.getModel())
                .getDataVector().elementAt(selected)).get(0)).getObject();
        toggleButton.setEnabled(true);

        if (pluginInfo.getState()) {
            toggleButton.setEnabled(pluginInfo.getPluginInfo().isUnloadable());
            toggleButton.setText("Disable");
        } else {
            toggleButton.setText("Enable");
        }
        selectedPlugin = selected;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        if (pluginList.getRowCount() == 0) {
            return;
        }
        for (int i = 1; i < pluginList.getRowCount(); i++) {
            pluginList.getModel().getColumnCount();
            ((PluginInfoToggle) ((AddonCell) pluginList.getModel()
                    .getValueAt(i, 0)).getObject()).apply();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (pluginRefresh) {
            populateList();
        }
    }
}

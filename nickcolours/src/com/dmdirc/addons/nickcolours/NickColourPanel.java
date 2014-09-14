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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used for the custom nick colour settings component in the plugin's config dialog.
 */
public class NickColourPanel extends JPanel implements ActionListener,
        PreferencesInterface, ListSelectionListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The table headings. */
    private static final String[] HEADERS
            = {"Network", "Nickname", "Text colour", "Nicklist colour"};
    /** The table used for displaying the options. */
    private final JTable table;
    /** The identity to write settings to. */
    private final ConfigProvider configIdentity;
    /** Edit button. */
    private final JButton editButton;
    /** Delete button. */
    private final JButton deleteButton;
    /** Parent window that will own any new dialogs. */
    private final Window parentWindow;
    /** Icon manager to retrieve icons from. */
    private final IconManager iconManager;
    /** Colour manage to use to parse colours. */
    private final ColourManager colourManager;
    /** Config provider to read settings from. */
    private final AggregateConfigProvider config;
    /** The plugin's config domain. */
    private final String domain;

    /**
     * Creates a new instance of NickColourPanel.
     *
     * @param parentWindow  Parent window that will own any new dialogs.
     * @param iconManager   Icon manager to load icons from.
     * @param colourManager The colour manager to use to parse colours.
     * @param userSettings  The provider to write user settings to.
     * @param config        The config provider to read settings from.
     * @param domain        The plugin's config domain
     */
    public NickColourPanel(
            final Window parentWindow, final IconManager iconManager,
            final ColourManager colourManager,
            final ConfigProvider userSettings,
            final AggregateConfigProvider config, final String domain) {
        this.parentWindow = parentWindow;
        this.iconManager = iconManager;
        this.colourManager = colourManager;
        this.configIdentity = userSettings;
        this.config = config;
        this.domain = domain;

        final Object[][] data = NickColourManager.getData(config, domain);

        table = new JTable(new DefaultTableModel(data, HEADERS)) {
            /** A version number for this class. */
            private static final long serialVersionUID = 1;
            /** The colour renderer we're using for colour cells. */
            private final ColourRenderer colourRenderer = new ColourRenderer(colourManager);

            @Override
            public TableCellRenderer getCellRenderer(final int row,
                    final int column) {
                if (column == 2 || column == 3) {
                    return colourRenderer;
                } else {
                    return super.getCellRenderer(row, column);
                }
            }

            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }
        };

        final JScrollPane scrollPane = new JScrollPane(table);

        table.getSelectionModel().addListSelectionListener(this);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Color.class, new ColourRenderer(colourManager));

        setLayout(new MigLayout("ins 0, fillx, hmax 500"));
        add(scrollPane, "grow, push, wrap, spanx");

        final JButton addButton = new JButton("Add");
        addButton.addActionListener(this);
        add(addButton, "sg button, growx, pushx");

        editButton = new JButton("Edit");
        editButton.addActionListener(this);
        add(editButton, "sg button, growx, pushx");

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this);
        add(deleteButton, "sg button, growx, pushx");

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final int row = table.getSelectedRow();
        switch (e.getActionCommand()) {
            case "Add":
                new NickColourInputDialog(parentWindow, colourManager, iconManager, this);
                break;
            case "Edit":
                final DefaultTableModel model = ((DefaultTableModel) table.getModel());
                final String network = (String) model.getValueAt(row, 0);
                final String nickname = (String) model.getValueAt(row, 1);
                String textcolour = (String) model.getValueAt(row, 2);
                String nickcolour = (String) model.getValueAt(row, 3);
                if (textcolour == null) {
                    textcolour = "";
                }
                if (nickcolour == null) {
                    nickcolour = "";
                }
                new NickColourInputDialog(parentWindow, colourManager, iconManager, this,
                        row, nickname, network, textcolour, nickcolour);
                break;
            case "Delete":
                if (row > -1) {
                    ((DefaultTableModel) table.getModel()).removeRow(row);
                }
                break;
        }
    }

    /**
     * Removes a row from the table.
     *
     * @param row The row to be removed
     */
    public void removeRow(final int row) {
        ((DefaultTableModel) table.getModel()).removeRow(row);
    }

    /**
     * Adds a row to the table.
     *
     * @param network    The network setting
     * @param nickname   The nickname setting
     * @param textcolour The textpane colour setting
     * @param nickcolour The nick list colour setting
     */
    public void addRow(final String network, final String nickname,
            final String textcolour, final String nickcolour) {
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());
        model.addRow(new Object[]{network, nickname, textcolour, nickcolour});
    }

    /**
     * Retrieves the current data in use by this panel.
     *
     * @return This panel's current data.
     */
    private List<Object[]> getData() {
        final List<Object[]> res = new ArrayList<>();
        final DefaultTableModel model = ((DefaultTableModel) table.getModel());

        @SuppressWarnings("unchecked")
        final List<List<?>> rows = (List<List<?>>) model.getDataVector();
        for (List<?> row : rows) {
            res.add(new Object[]{row.get(0), row.get(1), row.get(2), row.get(3)});
        }

        return res;
    }

    @Override
    public void save() {
        // Remove all old config entries
        for (Object[] parts : NickColourManager.getData(config, domain)) {
            configIdentity.unsetOption(domain, "color:" + parts[0] + ":" + parts[1]);
        }

        // And write the new ones
        for (Object[] row : getData()) {
            configIdentity.
                    setOption(domain, "color:" + row[0] + ":" + row[1], row[2] + ":" + row[3]);
        }
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        final boolean enable = table.getSelectedRow() > -1
                && table.getModel().getRowCount() > 0;

        editButton.setEnabled(enable);
        deleteButton.setEnabled(enable);
    }

}

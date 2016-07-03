/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.components.GenericTableModel;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.colours.Colour;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Panel used for the custom nick colour settings component in the plugin's config dialog.
 */
public class NickColourPanel extends JPanel implements ActionListener,
        PreferencesInterface, ListSelectionListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The table used for displaying the options. */
    private final JTable table;
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
    private final NickColourManager manager;
    private final GenericTableModel<NickColourEntry> model;

    /**
     * Creates a new instance of NickColourPanel.
     *
     * @param parentWindow  Parent window that will own any new dialogs.
     * @param iconManager   Icon manager to load icons from.
     * @param colourManager The colour manager to use to parse colours.
     * @param nickColours
     */
    public NickColourPanel(final Window parentWindow, final IconManager iconManager,
            final ColourManager colourManager, final NickColourManager manager,
            final Map<String, Color> nickColours) {
        this.parentWindow = parentWindow;
        this.iconManager = iconManager;
        this.colourManager = colourManager;
        this.manager = manager;
        model = new GenericTableModel<>(NickColourEntry.class,
                "getUser", "getNetwork", "getColor");
        nickColours.forEach((description, colour) -> model.addValue(NickColourEntry
                .create(description.split(":")[0], description.split(":")[1], colour)));
        model.setHeaderNames("User", "Network", "Colour");
        table = new JTable(model);
        table.setDefaultRenderer(Color.class, new ColourRenderer());
        final JScrollPane scrollPane = new JScrollPane(table);
        table.getSelectionModel().addListSelectionListener(this);
        table.setFillsViewportHeight(true);

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
                new NickColourInputDialog(parentWindow, colourManager, iconManager, model);
                break;
            case "Edit":
                final NickColourEntry entry = model.getValue(row);
                final String network = entry.getNetwork();
                final String nickname = entry.getUser();
                final Color textcolour = entry.getColor();
                new NickColourInputDialog(parentWindow, colourManager, iconManager, model,
                        row, nickname, network, textcolour);
                break;
            case "Delete":
                if (row > -1) {
                    model.removeValue(model.getValue(row));
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
        model.removeValue(model.getValue(row));
    }

    /**
     * Adds a row to the table.
     *
     * @param network    The network setting
     * @param nickname   The nickname setting
     * @param textcolour The textpane colour setting
     */
    public void addRow(final String network, final String nickname,
            final String textcolour) {
        final Colour colour = colourManager.getColourFromString(textcolour, null);
        model.addValue(NickColourEntry.create(network, nickname, new Color(colour.getRed(),
                colour.getGreen(), colour.getBlue())));
    }

    @Override
    public void save() {
        final Map<String, Color> values = new HashMap<>();
        for (int i =0; i < model.getRowCount(); i++) {
            final NickColourEntry entry = model.getValue(i);
            values.put(entry.getNetwork()+ ':' +entry.getUser(), entry.getColor());
        }
        manager.saveNickColourStore(values);
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        final boolean enable = table.getSelectedRow() > -1
                && table.getModel().getRowCount() > 0;

        editButton.setEnabled(enable);
        deleteButton.setEnabled(enable);
    }

}

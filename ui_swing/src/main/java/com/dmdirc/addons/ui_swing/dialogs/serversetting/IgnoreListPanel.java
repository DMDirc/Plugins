/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.common.IgnoreList;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.RegexValidator;
import com.dmdirc.util.validators.ValidatorChain;
import com.dmdirc.util.validators.ValidatorChainBuilder;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Ignore list panel.
 */
public final class IgnoreListPanel extends JPanel implements ActionListener, ListSelectionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Parent connection. */
    private final Connection connection;
    /** Parent window. */
    private final Window parentWindow;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Add button. */
    private JButton addButton;
    /** Remove button. */
    private JButton delButton;
    /** View toggle. */
    private JCheckBox viewToggle;
    /** Size label. */
    private JLabel sizeLabel;
    /** Ignore list. */
    private JList<String> list;
    /** Cached ignore list. */
    private IgnoreList cachedIgnoreList;
    /** Ignore list model . */
    private IgnoreListModel listModel;

    /**
     * Creates a new instance of IgnoreList.
     *
     * @param iconManager  Icon manager
     * @param connection   The connection whose ignore list should be displayed.
     * @param parentWindow Parent window
     */
    public IgnoreListPanel(final IconManager iconManager,
            final Connection connection, final Window parentWindow) {

        this.iconManager = iconManager;
        this.connection = connection;
        this.parentWindow = parentWindow;

        setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
        addListeners();
        populateList();
    }

    /** Initialises the components. */
    private void initComponents() {
        cachedIgnoreList = new IgnoreList(connection.getIgnoreList().getRegexList());

        listModel = new IgnoreListModel(cachedIgnoreList);
        list = new JList<>(listModel);

        final JScrollPane scrollPane = new JScrollPane(list);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        addButton = new JButton("Add");
        delButton = new JButton("Remove");

        sizeLabel = new JLabel("0 entries");
        viewToggle = new JCheckBox("Use advanced expressions");
        viewToggle.setOpaque(UIUtilities.getTabbedPaneOpaque());
        viewToggle.setSelected(!cachedIgnoreList.canConvert());
        viewToggle.setEnabled(cachedIgnoreList.canConvert());

        setLayout(new MigLayout("fill, wrap 1"));
        add(scrollPane, "grow, push");
        add(sizeLabel, "split 2, pushx, growx");
        add(viewToggle, "alignx center");
        add(addButton, "split 2, width 50%");
        add(delButton, "width 50%");
    }

    /** Updates the size label. */
    private void updateSizeLabel() {
        sizeLabel.setText(cachedIgnoreList.count() + " entr" + (cachedIgnoreList.count()
                == 1 ? "y" : "ies"));
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        addButton.addActionListener(this);
        delButton.addActionListener(this);
        viewToggle.addActionListener(this);
        list.getSelectionModel().addListSelectionListener(this);
    }

    /** Populates the ignore list. */
    private void populateList() {
        if (list.getSelectedIndex() == -1) {
            delButton.setEnabled(false);
        }

        updateSizeLabel();
    }

    /** Updates the list. */
    private void updateList() {
        listModel.notifyUpdated();

        if (cachedIgnoreList.canConvert()) {
            viewToggle.setEnabled(true);
        } else {
            viewToggle.setEnabled(false);
            viewToggle.setSelected(true);
        }
    }

    /** Saves the ignore list. */
    public void saveList() {
        connection.getIgnoreList().clear();
        connection.getIgnoreList().addAll(cachedIgnoreList.getRegexList());
        connection.saveIgnoreList();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addButton) {
            final ValidatorChainBuilder<String> validatorBuilder = ValidatorChain.builder();
            validatorBuilder.addValidator(new NotEmptyValidator());
            if (viewToggle.isSelected()) {
                validatorBuilder.addValidator(new RegexValidator());
            }

            new StandardInputDialog(parentWindow,
                    ModalityType.MODELESS, iconManager, "New ignore list entry",
                    "Please enter the new ignore list entry", validatorBuilder.build(),
                    this::addNewIgnoreEntry).display();
        } else if (e.getSource() == delButton && list.getSelectedIndex() != -1) {
            new StandardQuestionDialog(parentWindow,
                    ModalityType.APPLICATION_MODAL,
                    "Confirm deletion",
                    "Are you sure you want to delete this item?",
                    () -> {
                        cachedIgnoreList.remove(list.getSelectedIndex());
                        updateList();
                    }).display();
        } else if (e.getSource() == viewToggle) {
            listModel.setIsSimple(!viewToggle.isSelected());
        }
    }

    private void addNewIgnoreEntry(final String text) {
        if (viewToggle.isSelected()) {
            cachedIgnoreList.add(text);
        } else {
            cachedIgnoreList.addSimple(text);
        }
        updateList();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (list.getSelectedIndex() == -1) {
            delButton.setEnabled(false);
        } else {
            delButton.setEnabled(true);
        }
    }

}

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

package com.dmdirc.addons.ui_swing.dialogs.aliases;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.ConsumerDocumentListener;
import com.dmdirc.addons.ui_swing.components.GenericListModel;
import com.dmdirc.addons.ui_swing.components.renderers.PropertyListCellRenderer;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableListSelectionModel;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.interfaces.ui.AliasDialogModel;
import com.dmdirc.interfaces.ui.AliasDialogModelListener;
import com.dmdirc.ui.IconManager;

import java.awt.Dialog;
import java.beans.PropertyVetoException;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Links the Alias Manager Dialog with its controller and model.
 */
public class AliasManagerLinker implements AliasDialogModelListener {

    private final AliasDialogModel model;
    private final AliasManagerDialog dialog;
    private final IconManager iconManager;
    private GenericListModel<Alias> commandModel;
    private VetoableListSelectionModel selectionModel;
    private JList<Alias> commandList;
    private JScrollPane responseScroll;
    private JTextArea response;
    private JTextField command;
    private JButton okButton;
    private JButton deleteAlias;
    private JSpinner argumentsNumber;

    public AliasManagerLinker(
            final AliasDialogModel model,
            final AliasManagerDialog dialog,
            final IconManager iconManager) {
        this.model = model;
        this.dialog = dialog;
        this.iconManager = iconManager;
    }

    public void init() {
        model.addListener(this);
    }

    public void bindCommandList(final JList<Alias> commandList) {
        this.commandList = commandList;
        commandModel = new GenericListModel<>();
        selectionModel = new VetoableListSelectionModel();
        commandList.setCellRenderer(new PropertyListCellRenderer<>(commandList.getCellRenderer(),
                Alias.class, "name"));
        commandList.setModel(commandModel);
        commandList.setSelectionModel(selectionModel);
        commandList.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            final int index = commandList.getSelectedIndex();
            if (index == -1 || commandModel.getSize() == 0) {
                model.setSelectedAlias(Optional.<Alias>empty());
            } else if (index >= commandModel.getSize()) {
                model.setSelectedAlias(Optional.ofNullable(commandModel.
                        getElementAt(index - 1)));
            } else {
                model.setSelectedAlias(Optional.ofNullable(commandModel.getElementAt(index)));
            }
        });
        selectionModel.addVetoableSelectionListener(evt -> {
            if (!model.isChangeAliasAllowed()) {
                throw new PropertyVetoException("Currently selected alias is invalid.", evt);
            }
        });
    }

    public void bindCommand(final JTextField command) {
        this.command = command;
        command.setEnabled(false);
        command.getDocument().addDocumentListener(
                new ConsumerDocumentListener(model::setSelectedAliasName));
    }

    public void bindArgumentsNumber(final JSpinner argumentsNumber) {
        this.argumentsNumber = argumentsNumber;
        argumentsNumber.setEnabled(false);
        argumentsNumber.setModel(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        argumentsNumber.addChangeListener(
                e -> model.setSelectedAliasMinimumArguments((Integer) argumentsNumber.getValue()));
    }

    public void bindResponse(final JTextArea response, final JScrollPane responseScroll) {
        this.response = response;
        this.responseScroll = responseScroll;
        response.setEnabled(false);
        response.getDocument().addDocumentListener(
                new ConsumerDocumentListener(model::setSelectedAliasSubstitution));
    }

    public void bindAddAlias(final JButton addAlias) {
        addAlias.addActionListener(e -> new StandardInputDialog(dialog,
                Dialog.ModalityType.DOCUMENT_MODAL, iconManager,
                "Add Alias", "Enter the alias name", model.getNewCommandValidator(),
                (String s) -> model.addAlias(s, 0, s)).display());
    }

    public void bindDeleteAlias(final JButton deleteAlias) {
        this.deleteAlias = deleteAlias;
        deleteAlias.setEnabled(false);
        deleteAlias.addActionListener(e -> {
            final Optional<Alias> alias = model.getSelectedAlias();
            if (alias.isPresent()) {
                model.removeAlias(alias.get().getName());
            }
        });
    }

    public void bindOKButton(final JButton okButton) {
        this.okButton = okButton;
        okButton.addActionListener(e -> {
            model.save();
            dialog.dispose();
        });
    }

    public void bindCancelButton(final JButton cancelButton) {
        cancelButton.setEnabled(true);
        cancelButton.addActionListener(e -> dialog.dispose());
    }

    @Override
    public void aliasAdded(final Alias alias) {
        commandModel.add(alias);
        commandList.getSelectionModel().setSelectionInterval(
                commandModel.indexOf(alias), commandModel.indexOf(alias));
    }

    @Override
    public void aliasRemoved(final Alias alias) {
        final int index = commandModel.indexOf(alias);
        commandModel.remove(alias);
        if (index >= commandModel.getSize()) {
            model.setSelectedAlias(Optional.ofNullable(
                    commandModel.getElementAt(commandModel.getSize() - 1)));
        } else if (index == -1 && !commandModel.isEmpty()) {
            model.setSelectedAlias(Optional.ofNullable(commandModel.get(0)));
        } else {
            model.setSelectedAlias(Optional.ofNullable(commandModel.get(index)));
        }
    }

    @Override
    public void aliasEdited(final Alias oldAlias, final Alias newAlias) {
        commandModel.replace(oldAlias, newAlias);
    }

    @Override
    public void aliasRenamed(final Alias oldAlias, final Alias newAlias) {
        commandModel.replace(oldAlias, newAlias);
    }

    @Override
    public void aliasSelectionChanged(final Optional<Alias> alias) {
        deleteAlias.setEnabled(model.getSelectedAlias().isPresent());
        argumentsNumber.setEnabled(model.isMinimumArgumentsValid());
        argumentsNumber.setValue(model.getSelectedAliasMinimumArguments());
        command.setEnabled(model.isCommandValid());
        command.setText(model.getSelectedAliasName());
        final int index;
        if (alias.isPresent()) {
            index = commandModel.indexOf(alias.get());
        } else {
            index = -1;
        }
        if (index != selectionModel.getLeadSelectionIndex()) {
            selectionModel.setLeadSelectionIndex(index);
        }
        response.setEnabled(model.isSubstitutionValid());
        response.setText(model.getSelectedAliasSubstitution());
        UIUtilities.resetScrollPane(responseScroll);
    }

    @Override
    public void selectedAliasEdited(final String name, final int minArguments,
            final String substitution) {
        okButton.setEnabled(model.isSelectedAliasValid());
    }

}

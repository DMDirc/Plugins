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

package com.dmdirc.addons.ui_swing.dialogs.newaliases;

import com.dmdirc.addons.ui_swing.components.GenericTableModel;
import com.dmdirc.commandparser.aliases.Alias;

import com.google.common.base.Optional;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class AliasManagerLinker {

    private final AliasManagerController controller;
    private final AliasManagerModel model;
    private final AliasManagerDialog dialog;

    public AliasManagerLinker(final AliasManagerController controller,
            final AliasManagerModel model,
            final AliasManagerDialog dialog) {
        this.controller = controller;
        this.model = model;
        this.dialog = dialog;
    }

    public void bindCommandList(final JTable commandList) {
        final GenericTableModel<Alias> commandModel = new GenericTableModel<>(
                Alias.class, "getName", "getMinArguments", "getSubstitution");
        commandModel.setHeaderNames("Name", "Minimum Arguments", "Substitution");
        commandList.setModel(commandModel);
        commandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                final int index = commandList.getSelectedRow();
                if (index == -1) {
                    model.setSelectedAlias(Optional.<Alias>absent());
                } else {
                    model.setSelectedAlias(Optional.fromNullable(commandModel.getValue(index)));
                }
            }
        });
        model.addPropertyChangeListener("aliases", new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                for (Alias alias : model.getAliases()) {
                    commandModel.addValue(alias);
                }
            }
        });
    }

    public void bindCommand(final JTextField command) {
        model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                final Optional<Alias> selectedAlias = model.getSelectedAlias();
                command.setEnabled(selectedAlias.isPresent());
                if (selectedAlias.isPresent()) {
                    command.setText(selectedAlias.get().getName());
                } else {
                    command.setText("");
                }
            }
        });
    }

    public void bindArgumentsNumber(final JSpinner argumentsNumber) {
        model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                final Optional<Alias> selectedAlias = model.getSelectedAlias();
                argumentsNumber.setEnabled(selectedAlias.isPresent());
                if (selectedAlias.isPresent()) {
                    argumentsNumber.setValue(model.getSelectedAlias().get().getMinArguments());
                } else {
                    argumentsNumber.setValue(0);
                }
            }
        });
    }

    public void bindResponse(final JTextArea response) {
        model.addPropertyChangeListener("selectedAlias", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                final Optional<Alias> selectedAlias = model.getSelectedAlias();
                response.setEnabled(selectedAlias.isPresent());
                if (selectedAlias.isPresent()) {
                    response.setText(model.getSelectedAlias().get().getSubstitution());
                } else {
                    response.setText("");
                }
            }
        });
    }

    public void bindAddAlias(final JButton addAlias) {
        addAlias.setEnabled(false);
    }

    public void bindDeleteAlias(final JButton deleteAlias) {
        deleteAlias.setEnabled(false);
    }

    public void bindOKButton(final JButton okButton) {
        okButton.setEnabled(false);
    }

    public void bindCancelButton(final JButton cancelButton) {
        cancelButton.setEnabled(true);
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                controller.discardAndCloseDialog();
            }
        });
    }

}

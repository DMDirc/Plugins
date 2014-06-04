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

package com.dmdirc.addons.ui_swing.dialogs.newserver;

import com.dmdirc.addons.ui_swing.components.renderers.PropertyListCellRenderer;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableComboBoxModel;
import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.ui.NewServerDialogModel;
import com.dmdirc.ui.core.newserver.NewServerDialogModelAdapter;

import com.google.common.base.Optional;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class NewServerLinker {

    private final NewServerDialogModel model;
    private final NewServerDialog dialog;

    NewServerLinker(final NewServerDialogModel model, final NewServerDialog dialog) {
        this.model = model;
        this.dialog = dialog;
    }

    void bindHostname(final JTextField hostnameField) {
        hostnameField.setText(model.getHostname().isPresent() ? model.getHostname().get() : "");
        hostnameField.getDocument().addDocumentListener(new DocumentListener() {

            private void update() {
                model.setHostname(Optional.fromNullable(hostnameField.getText()));
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                update();
            }
        });
        model.addListener(new NewServerDialogModelAdapter() {

            @Override
            public void serverDetailsChanged(final Optional<String> hostname,
                    final Optional<Integer> port, final Optional<String> password,
                    final boolean ssl, final boolean saveAsDefault) {
                if (!hostname.equals(Optional.fromNullable(hostnameField.getText()))) {
                    hostnameField.setText(hostname.isPresent() ? hostname.get() : "");
                }
            }
        });
    }

    void bindPort(final JTextField portField) {
        portField.setText(model.getPort().isPresent() ? Integer.toString(model.getPort().get())
                : "");
        portField.getDocument().addDocumentListener(new DocumentListener() {

            private void update() {
                try {
                    model.setPort(Optional.fromNullable(Integer.valueOf(portField.getText())));
                } catch (NumberFormatException ex) {
                    //Do nothing, it'll have to be corrected and its handled by the validator.
                }
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                update();
            }
        });
    }

    void bindPassword(final JTextField passwordField) {
        passwordField.setText(model.getPassword().isPresent() ? model.getPassword().get() : "");
        passwordField.getDocument().addDocumentListener(new DocumentListener() {

            private void update() {
                model.setPassword(Optional.fromNullable(passwordField.getText()));
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                update();
            }
        });
    }

    void bindProfiles(final JComboBox<ConfigProvider> profilesCombobox) {
        final VetoableComboBoxModel<ConfigProvider> comboBoxModel = new VetoableComboBoxModel<>();
        profilesCombobox.setModel(comboBoxModel);
        profilesCombobox.setRenderer(new PropertyListCellRenderer<>(profilesCombobox.getRenderer(),
                ConfigProvider.class, "name"));
        for (ConfigProvider profile : model.getProfileList()) {
            comboBoxModel.addElement(profile);
        }
        if (model.getSelectedProfile().isPresent()) {
            comboBoxModel.setSelectedItem(model.getSelectedProfile().get());
        } else if (comboBoxModel.getSize() > 0) {
            comboBoxModel.setSelectedItem(comboBoxModel.getElementAt(0));
        }
        comboBoxModel.addVetoableSelectionListener(new VetoableChangeListener() {

            @Override
            public void vetoableChange(final PropertyChangeEvent evt) throws PropertyVetoException {
                if (evt.getNewValue() == null) {
                    throw new PropertyVetoException("Selection cannot be null", evt);
                }
            }
        });
        profilesCombobox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    model.setSelectedProfile(Optional.fromNullable((ConfigProvider) e.getItem()));
                }
            }
        });
    }

    void bindEditProfiles(final JButton edit,
            final DialogProvider<ProfileManagerDialog> profileManagerDialog) {
        edit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                profileManagerDialog.displayOrRequestFocus();
            }
        });
        model.addListener(new NewServerDialogModelAdapter() {

            @Override
            public void selectedProfileChanged(Optional<ConfigProvider> oldProfile,
                    Optional<ConfigProvider> newProfile) {
                edit.setEnabled(model.isProfileListValid()
                        && model.getSelectedProfile().isPresent());
            }
        });
    }

    void bindSSL(final JCheckBox sslCheckbox) {
        sslCheckbox.setSelected(model.getSSL());
        sslCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                model.setSSL(sslCheckbox.isSelected());
            }
        });
    }

    void bindSaveAsDefault(final JCheckBox saveAsDefaultCheckbox) {
        saveAsDefaultCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                model.setSaveAsDefault(saveAsDefaultCheckbox.isSelected());
            }
        });
    }

    void bindOKButton(final JButton okButton) {
        okButton.setEnabled(model.isSaveAllowed());
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                model.save();
                dialog.dispose();
            }
        });
        model.addListener(new NewServerDialogModelAdapter() {

            @Override
            public void serverDetailsChanged(final Optional<String> hostname,
                    final Optional<Integer> port, final Optional<String> password,
                    final boolean ssl, final boolean saveAsDefault) {
                okButton.setEnabled(model.isSaveAllowed());
            }
        });
    }

    void bindCancelButton(final JButton cancelButton) {
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                dialog.dispose();
            }
        });
    }

}

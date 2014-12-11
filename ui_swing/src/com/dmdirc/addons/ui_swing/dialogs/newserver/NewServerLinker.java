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

import com.dmdirc.addons.ui_swing.components.ConsumerDocumentListener;
import com.dmdirc.addons.ui_swing.components.renderers.PropertyListCellRenderer;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableComboBoxModel;
import com.dmdirc.addons.ui_swing.dialogs.profile.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.ui.NewServerDialogModel;
import com.dmdirc.interfaces.ui.NewServerDialogModelListener;

import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 * Links the New Server Dialog with its model.
 */
public class NewServerLinker implements NewServerDialogModelListener {

    private final NewServerDialogModel model;
    private final NewServerDialog dialog;
    private JButton okButton;
    private JButton edit;
    private JTextField hostnameField;
    private VetoableComboBoxModel<Profile> comboBoxModel;

    public NewServerLinker(final NewServerDialogModel model, final NewServerDialog dialog) {
        this.model = model;
        this.dialog = dialog;
    }

    public void init(final JButton edit, final JButton okButton,
            final JTextField hostnameField) {
        this.edit = edit;
        this.hostnameField = hostnameField;
        this.okButton = okButton;
        comboBoxModel = new VetoableComboBoxModel<>();
        model.addListener(this);
    }

    public void bindHostname(final JTextField hostnameField) {
        hostnameField.getDocument().addDocumentListener(new ConsumerDocumentListener(
                s -> model.setHostname(Optional.ofNullable(s))
        ));
        hostnameField.setText(model.getHostname().isPresent() ? model.getHostname().get() : "");
    }

    public void bindPort(final JTextField portField) {
        portField.getDocument().addDocumentListener(new ConsumerDocumentListener(
                s -> {
                    try {
                        model.setPort(Optional.ofNullable(Integer.valueOf(s)));
                    } catch (NumberFormatException ex) {
                        //Do nothing, it'll have to be corrected and its handled by the validator.
                    }
                }
        ));
        portField.setText(
                model.getPort().isPresent() ? Integer.toString(model.getPort().get()) : "");
    }

    public void bindPassword(final JTextField passwordField) {
        passwordField.getDocument().addDocumentListener(new ConsumerDocumentListener(
                s -> model.setPassword(Optional.ofNullable(s))
        ));
        passwordField.setText(model.getPassword().isPresent() ? model.getPassword().get() : "");
    }

    public void bindProfiles(final JComboBox<Profile> profilesCombobox) {
        profilesCombobox.setModel(comboBoxModel);
        if (model.getSelectedProfile().isPresent()) {
            comboBoxModel.setSelectedItem(model.getSelectedProfile().get());
        } else if (comboBoxModel.getSize() > 0) {
            comboBoxModel.setSelectedItem(comboBoxModel.getElementAt(0));
        }
        comboBoxModel.addVetoableSelectionListener(evt -> {
            if (evt.getNewValue() == null) {
                throw new PropertyVetoException("Selection cannot be null", evt);
            }
        });
        profilesCombobox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                model.setSelectedProfile(Optional.ofNullable((Profile) e.getItem()));
            }
        });
        profilesCombobox.setRenderer(new PropertyListCellRenderer<>(profilesCombobox.getRenderer(),
                Profile.class, "name"));
        model.getProfileList().forEach(comboBoxModel::addElement);
    }

    public void bindEditProfiles(final JButton edit,
            final DialogProvider<ProfileManagerDialog> profileManagerDialog) {
        edit.addActionListener(e -> profileManagerDialog.displayOrRequestFocus());
    }

    public void bindSSL(final JCheckBox sslCheckbox) {
        sslCheckbox.addActionListener(e -> model.setSSL(sslCheckbox.isSelected()));
        sslCheckbox.setSelected(model.getSSL());
    }

    public void bindSaveAsDefault(final JCheckBox saveAsDefaultCheckbox) {
        saveAsDefaultCheckbox.addActionListener(
                e -> model.setSaveAsDefault(saveAsDefaultCheckbox.isSelected()));
        saveAsDefaultCheckbox.setSelected(model.getSaveAsDefault());
    }

    public void bindOKButton(final JButton okButton) {
        okButton.addActionListener(e -> {
            model.save();
            dialog.dispose();
        });
        okButton.setEnabled(model.isSaveAllowed());
    }

    public void bindCancelButton(final JButton cancelButton) {
        cancelButton.addActionListener(e -> dialog.dispose());
    }

    @Override
    public void selectedProfileChanged(final Optional<Profile> oldProfile,
            final Optional<Profile> newProfile) {
        edit.setEnabled(model.isProfileListValid() && model.getSelectedProfile().isPresent());
    }

    @Override
    public void profileListChanged(final List<Profile> profiles) {
        comboBoxModel.removeAllElements();
        profiles.forEach(comboBoxModel::addElement);
        edit.setEnabled(model.isProfileListValid() && model.getSelectedProfile().isPresent());
        okButton.setEnabled(model.isSaveAllowed());
    }

    @Override
    public void serverDetailsChanged(final Optional<String> hostname, final Optional<Integer> port,
            final Optional<String> password, final boolean ssl, final boolean saveAsDefault) {
        if (!hostname.equals(Optional.ofNullable(hostnameField.getText()))) {
            hostnameField.setText(hostname.isPresent() ? hostname.get() : "");
        }
        okButton.setEnabled(model.isSaveAllowed());
    }
}

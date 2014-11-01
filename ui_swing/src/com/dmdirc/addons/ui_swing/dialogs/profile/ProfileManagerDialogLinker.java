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

package com.dmdirc.addons.ui_swing.dialogs.profile;

import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableListSelectionModel;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.interfaces.ui.ProfilesDialogModel;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.core.profiles.MutableProfile;
import com.dmdirc.ui.core.profiles.ProfilesDialogModelAdapter;

import com.google.common.collect.Lists;

import java.awt.Dialog;
import java.beans.PropertyVetoException;
import java.util.Optional;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ProfileManagerDialogLinker {

    private final ProfileManagerDialog dialog;
    private final ProfilesDialogModel model;
    private final IconManager iconManager;

    public ProfileManagerDialogLinker(final ProfileManagerDialog dialog,
            final ProfilesDialogModel model,
            final IconManager iconManager) {
        this.dialog = dialog;
        this.model = model;
        this.iconManager = iconManager;
    }

    public void bindAddNickname(final JButton addNickname) {
        addNickname.setEnabled(!model.getProfileList().isEmpty());
        model.addListener(new ProfilesDialogModelAdapter() {

            @Override
            public void profileRemoved(final MutableProfile profile) {
                addNickname.setEnabled(model.isProfileListValid());
            }

            @Override
            public void profileAdded(final MutableProfile profile) {
                addNickname.setEnabled(model.isProfileListValid());
            }
        });
        addNickname.addActionListener(e -> new StandardInputDialog(dialog,
                Dialog.ModalityType.DOCUMENT_MODAL, iconManager, "Profile Manager: Add Nickname",
                "Enter nickname to add", model::addSelectedProfileNickname
        ).display());
    }

    public void bindAddProfile(final JButton addProfile) {
        addProfile.addActionListener(e -> new StandardInputDialog(dialog,
                Dialog.ModalityType.DOCUMENT_MODAL, iconManager, "Profile Manager: Add Profile",
                "Enter the new profile's name",
                (String s) -> model.addProfile(s, s, s, Lists.newArrayList(s))
        ).display());
    }

    public void bindCancelButton(final JButton cancelButton) {
        cancelButton.addActionListener(l -> dialog.dispose());
    }

    public void bindOKButton(final JButton okButton) {
        okButton.setEnabled(model.isSaveAllowed());
        model.addListener(new ProfilesDialogModelAdapter() {

            @Override
            public void profileAdded(final MutableProfile profile) {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void profileRemoved(final MutableProfile profile) {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void profileEdited(final MutableProfile profile) {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void profileSelectionChanged(final Optional<MutableProfile> profile) {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void selectedNicknameChanged(final Optional<String> nickname) {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void selectedProfileNicknameEdited(final String oldNickname,
                    final String newNickname) {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void selectedProfileNicknameAdded(final String nickname) {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void selectedProfileNicknameRemoved(final String nickname) {
                okButton.setEnabled(model.isSaveAllowed());
            }
        });
        okButton.addActionListener(l -> {
            model.save();
            dialog.dispose();
        });
    }

    public void bindDeleteNickname(final JButton deleteNickname) {
        deleteNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
        deleteNickname.addActionListener(l -> model.getSelectedProfileSelectedNickname()
                .ifPresent(model::removeSelectedProfileNickname));
        model.addListener(new ProfilesDialogModelAdapter() {
            @Override
            public void selectedNicknameChanged(final Optional<String> nickname) {
                deleteNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
            }
        });
    }

    public void bindDeleteProfile(final JButton deleteProfile) {
        deleteProfile.setEnabled(model.getSelectedProfile().isPresent());
        deleteProfile.addActionListener(l -> model.getSelectedProfile()
                .ifPresent(p -> model.removeProfile(p.getName())));
        model.addListener(new ProfilesDialogModelAdapter() {
            @Override
            public void profileSelectionChanged(final Optional<MutableProfile> profile) {
                deleteProfile.setEnabled(model.getSelectedProfile().isPresent());
            }
        });
    }

    public void bindEditNickname(final JButton editNickname) {
        editNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
        editNickname.addActionListener(l -> model.getSelectedProfileSelectedNickname().ifPresent(
                (String oldName) -> new StandardInputDialog(dialog,
                        Dialog.ModalityType.DOCUMENT_MODAL, iconManager,
                        "Profile Manager: Edit Nickname", "Enter new nickname",
                        (String newName) -> model.editSelectedProfileNickname(oldName, newName))
                        .display()));
        model.addListener(new ProfilesDialogModelAdapter() {
            @Override
            public void selectedNicknameChanged(final Optional<String> nickname) {
                editNickname.setEnabled(model.getSelectedProfileSelectedNickname().isPresent());
            }
        });
    }

    public void bindProfileList(final JList<MutableProfile> profileList) {
        final DefaultListModel<MutableProfile> listModel
                = (DefaultListModel<MutableProfile>) profileList.getModel();
        final VetoableListSelectionModel selectionModel = new VetoableListSelectionModel();
        profileList.setSelectionModel(selectionModel);
        model.getProfileList().forEach(listModel::addElement);
        model.addListener(new ProfilesDialogModelAdapter() {

            @Override
            public void profileRemoved(final MutableProfile profile) {
                listModel.removeElement(profile);
            }

            @Override
            public void profileAdded(final MutableProfile profile) {
                listModel.addElement(profile);
            }

            @Override
            public void profileSelectionChanged(final Optional<MutableProfile> profile) {
                if (profile.isPresent()) {
                    final int index = listModel.indexOf(profile.get());
                    selectionModel.setLeadSelectionIndex(index);
                } else {
                    selectionModel.setLeadSelectionIndex(-1);
                }
            }
        });
        selectionModel.addVetoableSelectionListener(e -> {
            if (!model.canSwitchProfiles()) {
                throw new PropertyVetoException("Cannot switch with invalid profile", e);
            }
        });
        profileList.addListSelectionListener(l -> model.setSelectedProfile(
                Optional.ofNullable(profileList.getSelectedValue())));
    }

    public void bindProfileIdent(final JTextField ident) {
        ident.setEnabled(model.getSelectedProfileIdent().isPresent());
        model.addListener(new ProfilesDialogModelAdapter() {

            @Override
            public void profileSelectionChanged(final Optional<MutableProfile> profile) {
                ident.setEnabled(model.getSelectedProfile().isPresent());
                ident.setText(model.getSelectedProfileIdent().orElse(""));
            }
        });
        ident.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(final DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                changed();
            }

            private void changed() {
                if (model.getSelectedProfile().isPresent()) {
                    model.setSelectedProfileIdent(Optional.of(ident.getText()));
                }
            }
        });
    }

    public void bindProfileName(final JTextField name) {
        name.setEnabled(model.getSelectedProfileName().isPresent());
        model.addListener(new ProfilesDialogModelAdapter() {

            @Override
            public void profileSelectionChanged(final Optional<MutableProfile> profile) {
                name.setEnabled(model.getSelectedProfileIdent().isPresent());
                name.setText(model.getSelectedProfileName().orElse(""));
            }
        });
        name.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(final DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                changed();
            }

            private void changed() {
                if (model.getSelectedProfile().isPresent()) {
                    model.setSelectedProfileName(Optional.ofNullable(name.getText()));
                }
            }
        });
    }

    public void bindProfileNicknames(final ReorderableJList<String> nicknames) {
        nicknames.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nicknames.setEnabled(model.getSelectedProfileNicknames().isPresent());
        model.addListener(new ProfilesDialogModelAdapter() {

            @Override
            public void profileSelectionChanged(final Optional<MutableProfile> profile) {
                nicknames.setEnabled(model.getSelectedProfileNicknames().isPresent());
                nicknames.getModel().clear();
                if (model.getSelectedProfileNicknames().isPresent()) {
                    model.getSelectedProfileNicknames().get().forEach(p ->
                            nicknames.getModel().addElement(p)
                    );
                }
            }

            @Override
            public void selectedProfileNicknameAdded(final String nickname) {
                nicknames.getModel().addElement(nickname);
            }

            @Override
            public void selectedProfileNicknameRemoved(final String nickname) {
                nicknames.getModel().removeElement(nickname);
            }

            @Override
            public void selectedProfileNicknameEdited(final String oldNickname,
                    final String newNickname) {
                nicknames.getModel().setElementAt(newNickname,
                        nicknames.getModel().indexOf(oldNickname));
            }
        });
        nicknames.addListSelectionListener(l -> model.setSelectedProfileSelectedNickname(
                Optional.ofNullable(nicknames.getSelectedValue())));
    }

    public void bindProfileRealnames(final JTextField realname) {
        realname.setEnabled(model.getSelectedProfileRealname().isPresent());
        model.addListener(new ProfilesDialogModelAdapter() {

            @Override
            public void profileSelectionChanged(final Optional<MutableProfile> profile) {
                realname.setEnabled(model.getSelectedProfileRealname().isPresent());
                realname.setText(model.getSelectedProfileRealname().orElse(""));
            }
        });
        realname.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(final DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                changed();
            }

            private void changed() {
                if (model.getSelectedProfile().isPresent()) {
                    model.setSelectedProfileRealname(Optional.of(realname.getText()));
                }
            }
        });
    }
}

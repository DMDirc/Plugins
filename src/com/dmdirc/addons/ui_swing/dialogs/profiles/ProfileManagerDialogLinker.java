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

package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.addons.ui_swing.components.validating.ValidatableJTextField;
import com.dmdirc.addons.ui_swing.components.validating.ValidatableReorderableJList;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Links the UI code with the model and controller.
 */
public class ProfileManagerDialogLinker {

    /** Dialog controller. */
    private final ProfileManagerController controller;
    /** Dialog model. */
    private final ProfileManagerModel model;
    /** Dialog. */
    private final ProfileManagerDialog dialog;

    public ProfileManagerDialogLinker(final ProfileManagerController controller,
            final ProfileManagerModel model, final ProfileManagerDialog dialog) {
        this.controller = controller;
        this.model = model;
        this.dialog = dialog;
    }

    /**
     * Binds the profile list to the model.
     *
     * @param list List to bind
     */
    public void bindProfileList(final JList list) {
        list.setModel(new DefaultListModel());
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                model.setSelectedProfile(list.getSelectedValue());
            }
        });
        model.addPropertyChangeListener("profiles", new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                ((DefaultListModel) list.getModel()).clear();
                for (Profile profile : model.getProfiles()) {
                    ((DefaultListModel) list.getModel()).addElement(profile);
                }
                if (list.getSelectedValue() != model.getSelectedProfile()) {
                    list.setSelectedValue(model.getSelectedProfile(), true);
                }
            }

        });
        model.addPropertyChangeListener("name", new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                list.repaint();
            }

        });
        for (Profile profile : model.getProfiles()) {
            ((DefaultListModel) list.getModel()).addElement(profile);
        }
    }

    /**
     * Binds the nicknames list to the model.
     *
     * @param nicknames list to bind
     */
    public void bindProfileNicknames(final ValidatableReorderableJList nicknames) {
        nicknames.setModel(new DefaultListModel());
        nicknames.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nicknames.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                model.setSelectedNickname(nicknames.getSelectedValue());
            }
        });
        model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                nicknames.getModel().clear();
                for (String nickname : model.getNicknames()) {
                    nicknames.getModel().addElement(nickname);
                }
                nicknames.setSelectedValue(model.getSelectedNickname(), true);
                nicknames.setEnabled(model.isManipulateProfileAllowed());
                nicknames.setValidation(model.isNicknamesValid());
            }
        });
        model.addPropertyChangeListener("nicknames", new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                nicknames.getModel().clear();
                for (String nickname : model.getNicknames()) {
                    nicknames.getModel().addElement(nickname);
                }
                nicknames.setSelectedValue(model.getSelectedNickname(), true);
                nicknames.setEnabled(model.isManipulateProfileAllowed());
                nicknames.setValidation(model.isNicknamesValid());
            }
        });
    }

    /**
     * Binds the add nickname button to the controller.
     *
     * @param addNickname Button to bind
     */
    public void bindAddNickname(final JButton addNickname) {
        addNickname.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                new StandardInputDialog(dialog, ModalityType.DOCUMENT_MODAL,
                        "Add nickname", "Enter nickname to add:", new AddNicknameValidator(model)) {

                    @Override
                    public boolean save() {
                        controller.addNickname(getText());
                        return true;
                    }

                    @Override
                    public void cancelled() {
                    }
                }.display();
            }
        });
        model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                addNickname.setEnabled(model.isManipulateProfileAllowed());
            }
        });
        model.addPropertyChangeListener("profiles", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                addNickname.setEnabled(model.isManipulateProfileAllowed());
            }
        });
    }

    /**
     * Binds the edit nickname button to the controller.
     *
     * @param editNickname Button to bind
     */
    public void bindEditNickname(final JButton editNickname) {
        editNickname.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                final StandardInputDialog inputDialog = new StandardInputDialog(dialog,
                        ModalityType.DOCUMENT_MODAL, "Add nickname", "Enter edited nickname:",
                        new EditNicknameValidator(model)) {

                    @Override
                    public boolean save() {
                        controller.editNickname(getText());
                        return true;
                    }

                    @Override
                    public void cancelled() {
                    }
                };
                inputDialog.setText((String) model.getSelectedNickname());
                inputDialog.display();
            }
        });
        model.addPropertyChangeListener("nicknames", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                editNickname.setEnabled(model.isManipulateNicknameAllowed());
            }
        });
    }

    /**
     * Binds the delete nickname button to the controller.
     *
     * @param deleteNickname Button to bind
     */
    public void bindDeleteNickname(final JButton deleteNickname) {
        deleteNickname.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new StandardQuestionDialog(dialog, ModalityType.DOCUMENT_MODAL,
                        "Delete nickname?", "Are you sure you want to delete this nickname?") {

                    @Override
                    public boolean save() {
                        controller.deleteNickname();
                        return true;
                    }

                    @Override
                    public void cancelled() {
                    }
                }.display();
            }
        });
        model.addPropertyChangeListener("nicknames", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                deleteNickname.setEnabled(model.isManipulateNicknameAllowed());
            }
        });
    }

    /**
     * Binds the profile name to the model.
     *
     * @param profileName Text field to bind
     */
    public void bindProfileName(final ValidatableJTextField profileName) {
        final AtomicBoolean listenAction = new AtomicBoolean(false);
        profileName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setName(profileName.getText());
                profileName.setEnabled(model.isManipulateProfileAllowed());
                profileName.setValidation(model.isNameValid());
                listenAction.set(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setName(profileName.getText());
                profileName.setEnabled(model.isManipulateProfileAllowed());
                profileName.setValidation(model.isNameValid());
                listenAction.set(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listenAction.set(true);
                if (!model.isNameValid().isFailure()) {
                    model.setName(profileName.getText());
                }
                profileName.setEnabled(model.isManipulateProfileAllowed());
                profileName.setValidation(model.isNameValid());
                listenAction.set(false);
            }
        });
        model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!listenAction.get()) {
                    profileName.setText(model.getName());
                    profileName.setEnabled(model.isManipulateProfileAllowed());
                    profileName.setValidation(model.isNameValid());
                }
            }
        });
    }

    /**
     * Binds the profile realname to the model.
     *
     * @param profileRealname Textfield to bind
     */
    public void bindProfileRealnames(final ValidatableJTextField profileRealname) {
        final AtomicBoolean listenAction = new AtomicBoolean(false);
        profileRealname.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setRealname(profileRealname.getText());
                profileRealname.setEnabled(model.isManipulateProfileAllowed());
                profileRealname.setValidation(model.isRealnameValid());
                listenAction.set(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setRealname(profileRealname.getText());
                profileRealname.setEnabled(model.isManipulateProfileAllowed());
                profileRealname.setValidation(model.isRealnameValid());
                listenAction.set(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setRealname(profileRealname.getText());
                profileRealname.setEnabled(model.isManipulateProfileAllowed());
                profileRealname.setValidation(model.isRealnameValid());
                listenAction.set(false);
            }
        });
        model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!listenAction.get()) {
                    profileRealname.setText(model.getRealname());
                    profileRealname.setEnabled(model.isManipulateProfileAllowed());
                    profileRealname.setValidation(model.isRealnameValid());
                }
            }
        });
    }

    /**
     * Binds the profile ident to the model.
     *
     * @param profileIdent Textfield to bind
     */
    public void bindProfileIdent(final ValidatableJTextField profileIdent) {
        final AtomicBoolean listenAction = new AtomicBoolean(false);
        profileIdent.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setIdent(profileIdent.getText());
                profileIdent.setEnabled(model.isManipulateProfileAllowed());
                profileIdent.setValidation(model.isIdentValid());
                listenAction.set(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setIdent(profileIdent.getText());
                profileIdent.setEnabled(model.isManipulateProfileAllowed());
                profileIdent.setValidation(model.isIdentValid());
                listenAction.set(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listenAction.set(true);
                model.setIdent(profileIdent.getText());
                profileIdent.setEnabled(model.isManipulateProfileAllowed());
                profileIdent.setValidation(model.isIdentValid());
                listenAction.set(false);
            }
        });
        model.addPropertyChangeListener("selectedprofile", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!listenAction.get()) {
                    profileIdent.setEnabled(model.isManipulateProfileAllowed());
                    profileIdent.setValidation(model.isIdentValid());
                }
            }
        });
    }

    /**
     * Binds the add profile button to the controller.
     *
     * @param addProfile Button to bind
     */
    public void bindAddProfile(final JButton addProfile) {
        addProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                controller.addProfile();
            }
        });
    }

    /**
     * Binds the delete profile button to the controller.
     *
     * @param deleteProfile Button to bind
     */
    public void bindDeleteProfile(final JButton deleteProfile) {
        deleteProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new StandardQuestionDialog(dialog, ModalityType.DOCUMENT_MODAL,
                        "Delete profile?", "Are you sure you want to delete this profile?") {

                    @Override
                    public boolean save() {
                        controller.deleteProfile();
                        return true;
                    }

                    @Override
                    public void cancelled() {
                    }
                }.display();
            }
        });
        model.addPropertyChangeListener("profiles", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                deleteProfile.setEnabled(model.isManipulateProfileAllowed());
            }
        });
    }

    /**
     * Binds the OK button to the controller.
     *
     * @param okButton Button to bind
     */
    public void bindOKButton(final JButton okButton) {
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.saveAndCloseDialog();
            }
        });
        model.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                okButton.setEnabled(model.isOKAllowed());
            }
        });
    }

    /**
     * Binds the cancel button to the controller.
     *
     * @param cancelButton Button to bind
     */
    public void bindCancelButton(final JButton cancelButton) {
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.closeDialog();
            }
        });
    }
}

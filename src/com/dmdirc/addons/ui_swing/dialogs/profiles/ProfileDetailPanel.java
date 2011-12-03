/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
import com.dmdirc.addons.ui_swing.components.validating.NoDuplicatesInListValidator;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.util.validators.FileNameValidator;
import com.dmdirc.util.validators.IdentValidator;
import com.dmdirc.util.validators.NicknameValidator;
import com.dmdirc.util.validators.NotEmptyValidator;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.ValidatorChain;

import java.awt.Color;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/** Profile detail panel. */
public final class ProfileDetailPanel extends JPanel implements ActionListener,
        ListSelectionListener, ListDataListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Swing controller. */
    private final SwingController controller;
    /** Parent window. */
    private final Window parentWindow;
    /** Displayed profile. */
    private Profile profile;
    /** The profile list model. */
    private final ProfileListModel model;
    /** The nickname list model. */
    private final DefaultListModel nicknameModel;
    /** Duplicate nickname validator. */
    private final ValidatorChain<String> validator;
    /** Name text field. */
    private ValidatingJTextField name;
    /** Realname text field. */
    private ValidatingJTextField realname;
    /** Ident text field. */
    private ValidatingJTextField ident;
    /** Nicknames list. */
    private ReorderableJList nicknames;
    /** Add button. */
    private JButton addButton;
    /** Delete button. */
    private JButton delButton;
    /** Edit button. */
    private JButton editButton;
    /** List border. */
    private Border passBorder;
    /** Validation failed border. */
    private Border failBorder;

    /**
     * Creates a new profile detail panel.
     *
     * @param model The list model to use to validate names
     * @param controller Swing controller
     * @param parentWindow Parent window
     */
    @SuppressWarnings("unchecked")
    public ProfileDetailPanel(final ProfileListModel model,
            final SwingController controller, final Window parentWindow) {
        super();
        this.controller = controller;
        this.parentWindow = parentWindow;

        this.model = model;
        this.nicknameModel = new DefaultListModel();

        initMainComponents();

        validator = new ValidatorChain(new NoDuplicatesInListValidator(
                false, nicknames, nicknameModel), new NicknameValidator());

        layoutComponents();

        clearProfile();
    }

    /** Initialises the components in the main panel. */
    private void initMainComponents() {
        name = new ValidatingJTextField(controller.getIconManager(),
                new ProfileNameValidator());
        realname = new ValidatingJTextField(controller.getIconManager(),
                new NotEmptyValidator());
        ident = new ValidatingJTextField(controller.getIconManager(),
                new IdentValidator());
        nicknames = new ReorderableJList(nicknameModel);

        passBorder = nicknames.getBorder();
        failBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.RED, 10),
                nicknames.getBorder()
                );

        addButton = new JButton("Add");
        delButton = new JButton("Delete");
        editButton = new JButton("Edit");

        nicknames.setVisibleRowCount(4);

        nicknames.addListSelectionListener(this);
        nicknames.getModel().addListDataListener(this);

        addButton.addActionListener(this);
        delButton.addActionListener(this);
        editButton.addActionListener(this);
    }

    /** Lays out the components in the panel. */
    private void layoutComponents() {
        setLayout(new MigLayout("fillx"));

        add(new JLabel("Name:"));
        add(name, "growx, pushx, wrap");

        add(new JLabel("Nicknames:"));
        add(new JScrollPane(nicknames), "grow, pushx, wrap");
        add(addButton, "skip 1, split 3, sg button, growx");
        add(editButton, "sg button, growx");
        add(delButton, "sg button, growx, wrap");

        add(new JLabel("Realname:"));
        add(realname, "growx, pushx, wrap");

        add(new JLabel("Ident:"));
        add(ident, "growx, pushx, wrap");
    }

    /**
     * Sets the profile for the detail panel.
     *
     * @param profile new Profile for the detail panel
     */
    public void setProfile(final Profile profile) {
            this.profile = profile;
            updateProfile();
    }

    /** Updates this detail panel. */
    public void updateProfile() {
        if (profile == null) {
            clearProfile();
            return;
        }

        name.setText(profile.getName());
        realname.setText(profile.getRealname());
        ident.setText(profile.getIdent());

        nicknames.getModel().clear();
        for (String nickname : profile.getNicknames()) {
            nicknames.getModel().addElement(nickname);
        }

        name.setEnabled(true);
        realname.setEnabled(true);
        ident.setEnabled(true);
        nicknames.setEnabled(true);
        addButton.setEnabled(true);
    }

    /** Clears this detail panel. */
    public void clearProfile() {
        name.setText("");
        realname.setText("");
        ident.setText("");
        nicknames.getModel().clear();

        name.setEnabled(false);
        realname.setEnabled(false);
        ident.setEnabled(false);
        nicknames.setEnabled(false);
        addButton.setEnabled(false);
        delButton.setEnabled(false);
        editButton.setEnabled(false);
    }

    /** Saves the detail panels details to the profile. */
    public void save() {
        if (profile == null) {
            return;
        }

        profile.setName(name.getText());
        profile.setRealname(realname.getText());
        profile.setIdent(ident.getText());
        profile.setNicknames(new ArrayList<String>());
        final Enumeration<?> enumeration =
                nicknames.getModel().elements();
        while (enumeration.hasMoreElements()) {
            profile.addNickname((String) enumeration.nextElement());
        }
    }

    /**
     * Validates the current details.
     *
     * @return Validation Result
     */
    public boolean validateDetails() {
        if (nicknames.getModel().getSize() <= 0) {
           nicknames.setBorder(failBorder);
        } else {
            nicknames.setBorder(passBorder);
        }
        if (!ident.validateText() || !realname.validateText() ||
                !name.validateText() || nicknames.getModel().getSize() <= 0) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == addButton) {
            final int index = nicknames.getSelectedIndex();
            nicknames.getSelectionModel().clearSelection();
            new StandardInputDialog(controller, ProfileManagerDialog
                    .getProfileManagerDialog(controller),
                    ModalityType.DOCUMENT_MODAL, "New Nickname",
                    "Please enter the new nickname", validator) {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 2;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    nicknames.setSelectedIndex(index);
                    nicknames.getModel().addElement(getText());
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    nicknames.setSelectedIndex(index);
                }
            }.display();
        } else if (e.getSource() == editButton) {
            final StandardInputDialog dialog = new StandardInputDialog(
                    controller, ProfileManagerDialog.getProfileManagerDialog(controller),
                    ModalityType.DOCUMENT_MODAL, "Edit Nickname",
                    "Please enter the new nickname", validator) {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 2;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    nicknames.getModel().setElementAt(
                            getText(), nicknames.getSelectedIndex());
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    //Ignore
                }
            };
            dialog.setText((String) nicknames.getSelectedValue());
            dialog.display();
        } else if (e.getSource() == delButton) {
            new StandardQuestionDialog(parentWindow, ModalityType.DOCUMENT_MODAL,
                    "Delete Confirmaton",
                    "Are you sure you want to delete this nickname?") {

                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
                 */
                private static final long serialVersionUID = 1;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    if (nicknames.getSelectedIndex() != -1) {
                        nicknames.getModel().removeElementAt(nicknames.getSelectedIndex());
                    }
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    //Ignore
                }
            }.display();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (nicknames.getSelectedIndex() == -1) {
            editButton.setEnabled(false);
            delButton.setEnabled(false);
        } else {
            editButton.setEnabled(true);
            delButton.setEnabled(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void intervalAdded(final ListDataEvent e) {
        validateDetails();
    }

    /** {@inheritDoc} */
    @Override
    public void intervalRemoved(final ListDataEvent e) {
        validateDetails();
    }

    /** {@inheritDoc} */
    @Override
    public void contentsChanged(final ListDataEvent e) {
        validateDetails();
    }

    /**
     * Ensures profile names are unique.
     */
    private class ProfileNameValidator extends FileNameValidator {

        /** {@inheritDoc} */
        @Override
        public ValidationResponse validate(final String object) {
            for (Profile targetprofile : model) {
                if (targetprofile != profile && targetprofile.getName().
                        equalsIgnoreCase(object)) {
                    return new ValidationResponse("Profile names must be unique");
                }
            }

            return super.validate(object);
        }
    }
}

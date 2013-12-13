/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.components.GenericListModel;
import com.dmdirc.addons.ui_swing.components.renderers.ProfileListCellRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidatableJTextField;
import com.dmdirc.addons.ui_swing.components.validating.ValidatableReorderableJList;
import com.dmdirc.addons.ui_swing.dialogs.DialogManager;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.ui.IconManager;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/** Profile editing dialog. */
@SuppressWarnings("unused")
public class ProfileManagerDialog extends StandardDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 4;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Dialog manager. */
    private final DialogManager dialogManager;
    /** Model used to store state. */
    private ProfileManagerModel model;
    /** Dialog controller, used to perform actions. */
    private ProfileManagerController controller;
    /** List of profiles. */
    private JList<Profile> profileList;
    /** List model. */
    private GenericListModel<Profile> profileListModel;
    /** List of nicknames for a profile. */
    private ValidatableReorderableJList<String> nicknames;
    /** Nicknames model. */
    private GenericListModel<String> nicknamesModel;
    /** Adds a new nickname to the active profile. */
    private JButton addNickname;
    /** Edits the active nickname in the active profile. */
    private JButton editNickname;
    /** Deletes the selected nickname from the active profile. */
    private JButton deleteNickname;
    /** Edits the name of the active profile. */
    private ValidatableJTextField name;
    /** Edits the realname for the active profile. */
    private ValidatableJTextField realname;
    /** Edits the ident for the active profile. */
    private ValidatableJTextField ident;
    /** Adds a new profile to the list. */
    private JButton addProfile;
    /** Deletes the active profile. */
    private JButton deleteProfile;
    /** Saves and closes the dialog. */
    private JButton okButton;
    /** Closes the dialog without saving. */
    private JButton cancelButton;

    /**
     * Creates a new instance of ProfileEditorDialog.
     *
     * @param dialogManager Dialog manager
     * @param parentWindow Parent window
     * @param iconManager Icon manager
     * @param identityFactory Identity factory
     * @param identityManager Identity manager
     */
    public ProfileManagerDialog(final DialogManager dialogManager, final Window parentWindow,
            final IconManager iconManager, final IdentityFactory identityFactory,
            final IdentityController identityManager) {
        super(dialogManager, parentWindow, ModalityType.MODELESS);
        this.iconManager = iconManager;
        this.dialogManager = dialogManager;
        this.model = new ProfileManagerModel(identityManager, identityFactory);
        this.controller = new ProfileManagerController(this, model, identityFactory);
        initComponents();
        layoutComponents();
        model.addPropertyChangeListener("profiles", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                profileListModel.clear();
                profileListModel.addAll(model.getProfiles());
            }
        });
        model.addPropertyChangeListener("profile", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getOldValue() == null) {
                    profileListModel.add((Profile) evt.getNewValue());
                } else if (evt.getNewValue() == null) {
                    profileListModel.remove((Profile) evt.getNewValue());
                } else {
                    profileListModel.clear();
                    profileListModel.addAll(model.getProfiles());
                }
            }
        });
        model.addPropertyChangeListener("nicknames", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                nicknamesModel.clear();
                nicknamesModel.addAll(model.getNicknames());
            }
        });
        model.addPropertyChangeListener("nickname", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getOldValue() == null) {
                    nicknamesModel.add((String) evt.getNewValue());
                } else if (evt.getNewValue() == null) {
                    nicknamesModel.remove((String) evt.getOldValue());
                } else {
                    nicknamesModel.clear();
                    nicknamesModel.addAll(model.getNicknames());
                }
            }
        });
        model.addPropertyChangeListener("editNickname", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                final int index = nicknamesModel.indexOf((String) evt.getOldValue());
                nicknamesModel.remove((String) evt.getOldValue());
                nicknamesModel.add(index, (String) evt.getNewValue());
            }
        });
        model.addPropertyChangeListener("selectedProfile", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                profileList.setSelectedValue(evt.getNewValue(), true);
                nicknamesModel.clear();
                nicknamesModel.addAll(model.getNicknames());
                nicknames.setSelectedValue(model.getSelectedNickname(), true);
                name.setText(model.getName());
                realname.setText(model.getRealname());
                ident.setText(model.getIdent());
            }
        });
        model.addPropertyChangeListener("selectedNickname", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                nicknames.setSelectedValue(evt.getNewValue(), true);
            }
        });
    }

    /** Initialises the components. */
    private void initComponents() {
        profileListModel = new GenericListModel<>(model.getProfiles());
        profileList = new JList<>(profileListModel);
        profileList.setSelectionModel(new AlwaysSelectedListSelectionModel<>(profileListModel));
        nicknamesModel = new GenericListModel<>();
        nicknames = new ValidatableReorderableJList<>(nicknamesModel);
        nicknames.setSelectionModel(new AlwaysSelectedListSelectionModel<>(nicknamesModel));
        addNickname = new JButton("Add");
        editNickname = new JButton("Edit");
        deleteNickname = new JButton("Delete");
        addProfile = new JButton("Add");
        deleteProfile = new JButton("Delete");
        realname = new ValidatableJTextField(iconManager);
        ident = new ValidatableJTextField(iconManager);
        name = new ValidatableJTextField(iconManager);
        okButton = getOkButton();
        cancelButton = getCancelButton();
        profileList.setCellRenderer(new ProfileListCellRenderer(profileList.getCellRenderer()));

        profileList.setSelectedValue(model.getSelectedProfile(), true);
        nicknamesModel.addAll(model.getNicknames());
        nicknames.setSelectedValue(model.getSelectedNickname(), true);
        realname.setText(model.getRealname());
        ident.setText(model.getIdent());
        name.setText(model.getName());

        addProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.addProfile();
            }
        });
        deleteNickname.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.deleteNickname();
            }
        });
        editNickname.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new EditNicknameDialog(dialogManager, getParentWindow(), model, controller, (String) model.getSelectedNickname()).display();
            }
        });
        addNickname.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AddNicknameDialog(dialogManager, getParentWindow(), model, controller).display();
            }
        });
        profileList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    model.setSelectedProfile(profileList.getSelectedValue());
                }
            }
        });
        nicknames.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    model.setSelectedNickname(nicknames.getSelectedValue());
                }
            }
        });
    }

    /** Lays out the components. */
    private void layoutComponents() {

        setLayout(new MigLayout("fill, wmin 700, wmax 700, flowy"));

        add(new TextLabel("Profiles describe the information needed to connect to a server.  "
                + "You can use a different profile for each connection."), "spanx 3");
        add(new JScrollPane(profileList), "spany 3, growy, wmin 200, wmax 200");
        add(addProfile, "grow");
        add(deleteProfile, "grow, wrap");
        add(new JLabel("Name: "), "align label, span 2, split 2, flowx, sgx label");
        add(name, "growx, pushx, sgx textinput");
        add(new JLabel("Nicknames: "), "align label, span 2, split 2, flowx, sgx label, aligny 50%");
        add(new JScrollPane(nicknames), "grow, push");
        add(Box.createGlue(), "flowx, span 4, split 4, sgx label");
        add(addNickname, "grow");
        add(editNickname, "grow");
        add(deleteNickname, "grow");
        add(new JLabel("Realname: "), "align label, span 2, split 2, flowx, sgx label");
        add(realname, "growx, pushx, sgx textinput");
        add(new JLabel("Ident: "), "align label, span 2, split 2, flowx, sgx label");
        add(ident, "growx, pushx, sgx textinput");
        add(getLeftButton(), "flowx, split 2, right, sg button");
        add(getRightButton(), "right, sg button");
    }
}

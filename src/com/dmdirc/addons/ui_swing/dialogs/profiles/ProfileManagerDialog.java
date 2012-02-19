/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
import com.dmdirc.addons.ui_swing.cinch.ConfirmAction;
import com.dmdirc.addons.ui_swing.cinch.InputAction;
import com.dmdirc.addons.ui_swing.cinch.SwingUIBindings;
import com.dmdirc.addons.ui_swing.cinch.ValidatesIf;
import com.dmdirc.addons.ui_swing.components.renderers.ProfileListCellRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidatableJTextField;
import com.dmdirc.addons.ui_swing.components.validating.ValidatableReorderableJList;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import com.palantir.ptoss.cinch.core.Bindable;
import com.palantir.ptoss.cinch.core.Bindings;
import com.palantir.ptoss.cinch.swing.Action;
import com.palantir.ptoss.cinch.swing.Bound;
import com.palantir.ptoss.cinch.swing.BoundSelection;
import com.palantir.ptoss.cinch.swing.EnabledIf;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;

/** Profile editing dialog. */
@SuppressWarnings("unused")
public class ProfileManagerDialog extends StandardDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 3;
    /** Previously created instance of ProfileEditorDialog. */
    private static volatile ProfileManagerDialog me;
    /** Model used to store state. */
    private final ProfileManagerModel model;
    /** Dialog controller, used to perform actions. */
    @Bindable
    private final ProfileManagerController controller;
    /** List of profiles. */
    @Bound(to = "profiles")
    @BoundSelection(to = "selectedProfile", multi = false)
    private final JList profileList = new JList();
    /** List of nicknames for a profile. */
    @Bound(to = "nicknames")
    @BoundSelection(to = "selectedNickname", multi = false)
    @EnabledIf(to = "manipulateProfileAllowed")
    @ValidatesIf(to = "nicknamesValid")
    private final ValidatableReorderableJList nicknames
            = new ValidatableReorderableJList();
    /** Adds a new nickname to the active profile. */
    @InputAction(
            call = "addNickname",
            validator = AddNicknameValidator.class,
            message = "Enter nickname to add:")
    @EnabledIf(to = "manipulateProfileAllowed")
    private final JButton addNickname = new JButton("Add");
    /** Edits the active nickname in the active profile. */
    @InputAction(
            call = "editNickname",
            validator = EditNicknameValidator.class,
            message = "Enter edited nickname:",
            content = "getSelectedNickname")
    @EnabledIf(to = "manipulateNicknameAllowed")
    private final JButton editNickname = new JButton("Edit");
    /** Deletes the selected nickname from the active profile. */
    @ConfirmAction(call = "deleteNickname")
    @EnabledIf(to = "manipulateNicknameAllowed")
    private final JButton deleteNickname = new JButton("Delete");
    /** Edits the name of the active profile. */
    @Bound(to = "name")
    @EnabledIf(to = "manipulateProfileAllowed")
    @ValidatesIf(to = "nameValid")
    private final ValidatableJTextField name;
    /** Edits the realname for the active profile. */
    @Bound(to = "realname")
    @EnabledIf(to = "manipulateProfileAllowed")
    @ValidatesIf(to = "realnameValid")
    private final ValidatableJTextField realname;
    /** Edits the ident for the active profile. */
    @Bound(to = "ident")
    @EnabledIf(to = "manipulateProfileAllowed")
    @ValidatesIf(to = "identValid")
    private final ValidatableJTextField ident;
    /** Adds a new profile to the list. */
    @Action(call = "addProfile")
    private final JButton addProfile = new JButton("Add");
    /** Deletes the active profile. */
    @ConfirmAction(call = "deleteProfile")
    @EnabledIf(to = "manipulateProfileAllowed")
    private final JButton deleteProfile = new JButton("Delete");
    /** Saves and closes the dialog. */
    @Action(call = "saveAndCloseDialog")
    @EnabledIf(to = "OKAllowed")
    private final JButton okButton = getOkButton();
    /** Closes the dialog without saving. */
    @Action(call = "closeDialog")
    private final JButton cancelButton = getCancelButton();

    /**
     * Creates a new instance of ProfileEditorDialog.
     *
     * @param controller Swing controller
     */
    public ProfileManagerDialog(final SwingController controller) {
        super(controller, ModalityType.MODELESS);
        this.model = new ProfileManagerModel(controller.getIdentityManager());
        this.controller = new ProfileManagerController(this, model);
        final Bindings bindings = SwingUIBindings.extendedBindings();
        realname = new ValidatableJTextField(controller.getIconManager());
        ident = new ValidatableJTextField(controller.getIconManager());
        name = new ValidatableJTextField(controller.getIconManager());
        initComponents();
        bindings.bind(this);
    }

    /** Initialises the components. */
    private void initComponents() {
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nicknames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setCellRenderer(new ProfileListCellRenderer());

        setLayout(new MigLayout("fill, wmin 700, wmax 700, flowy"));

        add(new TextLabel("Profiles describe the information needed to connect "
                + "to a server.  You can use a different profile for each "
                + "connection."), "spanx 3");
        add(new JScrollPane(profileList), "spany 3, growy, "
                + "wmin 200, wmax 200");
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

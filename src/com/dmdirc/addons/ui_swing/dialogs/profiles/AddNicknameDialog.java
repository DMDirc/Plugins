package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.addons.ui_swing.dialogs.DialogManager;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;

import java.awt.Window;

public class AddNicknameDialog extends StandardInputDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Dialog controller. */
    private final ProfileManagerController controller;

    /**
     * Adds a new nickname to a profile.
     *
     * @param dialogManager Dialog manager
     * @param parentWindow Parent window
     * @param model Model to validate against
     * @param controller Controller to act upon
     */
    public AddNicknameDialog(final DialogManager dialogManager, final Window parentWindow,
            final ProfileManagerModel model, final ProfileManagerController controller) {
        super(dialogManager, parentWindow, ModalityType.DOCUMENT_MODAL, "Add Nickname", "Please enter the new nickname", new AddNicknameValidator(model));
        this.controller = controller;
    }

    /** {@inheritDoc} */
    @Override
    public boolean save() {
        controller.addNickname(getText());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelled() {
        //NOOP
    }
}

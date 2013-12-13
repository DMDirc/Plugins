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

import com.dmdirc.addons.ui_swing.dialogs.DialogManager;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;

import java.awt.Window;

public class EditNicknameDialog extends StandardInputDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Dialog controller. */
    private final ProfileManagerController controller;

    /**
     * Edits the selected nickname in a profile.
     *
     * @param dialogManager Dialog manager
     * @param parentWindow Parent window
     * @param model Model to validate data with
     * @param controller Controller to edit nickname with
     * @param nickname Nickname to edit
     */
    public EditNicknameDialog(final DialogManager dialogManager, final Window parentWindow,
            final ProfileManagerModel model, final ProfileManagerController controller,
            final String nickname) {
        super(dialogManager, parentWindow, ModalityType.DOCUMENT_MODAL, "Edit nickname",
                "Enter the new nickname", new EditNicknameValidator(model));
        this.controller = controller;
        setText(nickname);
    }

    /** {@inheritDoc} */
    @Override
    public boolean save() {
        controller.editNickname(getText());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelled() {
        //NOOP
    }

}

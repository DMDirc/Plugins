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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

/**
 * A dialog to prompt the user for a channel and then join that channel.
 */
@Factory(inject = true)
public class ChannelJoinDialog extends StandardInputDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;

    /**
     * Creates a new dialog which prompts a user and then joins the channel they specify.
     *
     * @param mainFrame   Main frame
     * @param activeFrameManager The active window manager
     * @param iconManager The icon manager to use for validating text fields.
     * @param title       Window title
     * @param message     Window message
     */
    public ChannelJoinDialog(
            final MainFrame mainFrame,
            final ActiveFrameManager activeFrameManager,
            @SuppressWarnings("qualifiers") @GlobalConfig final IconManager iconManager,
            @Unbound final String title,
            @Unbound final String message) {
        super(mainFrame, ModalityType.APPLICATION_MODAL, iconManager, title, message);

        this.activeFrameManager = activeFrameManager;
    }

    @Override
    public boolean save() {
        activeFrameManager.getActiveFrame().getContainer().getConnection()
                .join(new ChannelJoinRequest(getText()));
        return true;
    }

    @Override
    public void cancelled() {
        //Ignore
    }

}

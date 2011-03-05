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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.parser.common.ChannelJoinRequest;

import java.awt.Dialog.ModalityType;

/**
 * A dialog to prompt the user for a channel and then join that channel.
 */
public class ChannelJoinDialog extends StandardInputDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;

    /** The main frame that owns this dialog. */
    private final MainFrame mainFrame;

    /**
     * Creates a new dialog which prompts a user and then joins the channel
     * they specify.
     *
     * @param owner Parent window
     * @param modality Window modality
     * @param title Window title
     * @param message Window message
     */
    public ChannelJoinDialog(final MainFrame owner, final ModalityType modality,
            final String title, final String message) {
        super(owner, modality, title, message);

        this.mainFrame = owner;
    }

    /** {@inheritDoc} */
    @Override
    public boolean save() {
        mainFrame.getActiveFrame().getContainer().getServer()
                .join(new ChannelJoinRequest(getText()));
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelled() {
        //Ignore
    }
}

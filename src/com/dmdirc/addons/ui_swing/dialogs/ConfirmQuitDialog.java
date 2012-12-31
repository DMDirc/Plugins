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
package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.addons.ui_swing.SwingController;

/**
 * A simple dialog to confirm and handle the quitting of the client.
 */
public abstract class ConfirmQuitDialog extends StandardQuestionDialog {

    /**
     * A version number for this class. It should be changed
     * whenever the class structure is changed (or anything else
     * that would prevent serialized objects being unserialized
     * with the new class).
     */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new client quit confirmation dialog.
     *
     * @param controller Swing controller
     */
    public ConfirmQuitDialog(final SwingController controller) {
        super(controller, ModalityType.APPLICATION_MODAL, "Quit confirm",
                    "You are about to quit DMDirc, are you sure?");
    }

    /** {@inheritDoc} */
    @Override
    public boolean save() {
        handleQuit();
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelled() {
        // Do nothing
    }

    /**
     * Called when the user confirms they wish to quit the client.
     */
    protected abstract void handleQuit();
}

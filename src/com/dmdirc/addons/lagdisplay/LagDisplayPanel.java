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

package com.dmdirc.addons.lagdisplay;

import com.dmdirc.addons.ui_swing.components.statusbar.StatusbarPopupPanel;
import com.dmdirc.addons.ui_swing.components.statusbar.StatusbarPopupWindow;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JLabel;

/**
 * Shows the user's lag in the status bar, and reveals details of all servers when the user hovers
 * over it.
 */
@Singleton
public class LagDisplayPanel extends StatusbarPopupPanel<JLabel> {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 2;
    /** Factory of dialogs. */
    private final ServerInfoDialogFactory dialogFactory;

    /**
     * Creates a new instace of {@link LagDisplayPanel}.
     *
     * @param dialogFactory The factory to use to create dialogs.
     */
    @Inject
    public LagDisplayPanel(final ServerInfoDialogFactory dialogFactory) {
        super(new JLabel());
        this.dialogFactory = dialogFactory;
    }

    /** {@inheritDoc} */
    @Override
    protected StatusbarPopupWindow getWindow() {
        return dialogFactory.getServerInfoDialog(this);
    }

}

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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.Invite;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;

/**
 * Invite action.
 */
class InviteAction extends AbstractAction {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Invite. */
    private final Invite invite;

    /**
     * Instantiates a new invite action.
     *
     * @param invite Invite for the action
     */
    public InviteAction(final Invite invite) {
        super(invite.getChannel() + " (" + invite.getSource()[0] + ")");

        this.invite = invite;
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        invite.accept();
    }

    @Override
    public String toString() {
        return invite.getChannel() + "(" + Arrays.toString(invite.getSource())
                + ")";
    }

}

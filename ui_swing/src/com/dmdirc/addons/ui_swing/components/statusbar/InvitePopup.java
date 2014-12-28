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
import com.dmdirc.interfaces.Connection;
import com.dmdirc.util.DateUtils;

import java.awt.Window;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Shows information about received invites.
 *
 * @since 0.6.3m1
 */
public class InvitePopup extends StatusbarPopupWindow {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The connection to show invites for. */
    private final Optional<Connection> connection;

    /**
     * Creates a new InvitePopup for the specified panel and connection.
     *
     * @param parent       The parent of this popup
     * @param connection   The connection to show invites for
     * @param parentWindow Parent window
     */
    public InvitePopup(final JPanel parent,
            final Optional<Connection> connection, final Window parentWindow) {
        super(parent, parentWindow);
        this.connection = connection;
    }

    @Override
    protected void initContent(final JPanel panel) {
        if (connection.isPresent()) {
            for (final Invite invite : connection.get().getInvites()) {
                panel.add(new JLabel(invite.getChannel()), "growx, pushx");
                panel.add(new JLabel(invite.getSource().getNickname(), SwingConstants.CENTER),
                        "growx, pushx, al center");
                panel.add(new JLabel(DateUtils.formatDuration(
                                (int) (System.currentTimeMillis() - invite.getTimestamp()) / 1000)
                                + " ago", SwingConstants.RIGHT),
                        "growx, pushx, al right, wrap");
            }
        }
    }
}

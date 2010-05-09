/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.serverlist;

import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog to show and edit server lists.
 */
public final class ServerListDialog extends StandardDialog implements
        ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Server list model. */
    private final ServerListModel model;
    /** Connect button. */
    private final JButton connectButton;
    /** Previously created instance of dialog. */
    private static volatile ServerListDialog me = null;

    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param parentWindow Parent window
     */
    public static void showServerListDialog(final Window parentWindow) {
        me = getServerListDialog(parentWindow);

        me.display();
        me.requestFocusInWindow();
    }

    /**
     * Returns the current instance of the ServerListDialog.
     *
     * @param parentWindow Parent window
     *
     * @return The current ServerListDialog instance
     */
    public static ServerListDialog getServerListDialog(
            final Window parentWindow) {
        synchronized (ServerListDialog.class) {
            if (me == null) {
                me = new ServerListDialog(parentWindow, ModalityType.MODELESS);
            }
        }

        return me;
    }

    /**
     * Creates a new server list dialog.
     *
     * @param window Parent window
     * @param modalityType Desired modality
     */
    private ServerListDialog(final Window window,
            final ModalityType modalityType) {
        super(window, modalityType);
        setTitle("Server List");
        model = new ServerListModel();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        connectButton = new JButton("Connect");

        setLayout(new MigLayout("fill, wrap 2, wmin 600, wmax 600"));

        add(new Tree(model), "grow, spany 4, wmax 150, wmin 150");
        add(new Info(model), "spanx 2, growx, pushx");
        add(new Settings(model), "grow, push, gaptop unrel, gapbottom unrel");
        add(new Perform(model), "grow, push");
        add(new Profiles(model), "growx, pushx, spanx 2");
        add(connectButton, "skip 1, split 3, right, gapright unrel*2, "
                + "sgx button");
        add(getLeftButton(), "right, sgx button");
        add(getRightButton(), "right, sgx button");

        addListeners();
    }

    /**
     * Adds requires listeners to objects.
     */
    private void addListeners() {
        connectButton.addActionListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            model.saveChanges();
            dispose();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        } else if (e.getSource() == connectButton) {
            model.getSelectedItem().connect();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (ServerListDialog.class) {
            if (me == null) {
                return;
            }
            super.dispose();
            me = null;
        }
    }
}

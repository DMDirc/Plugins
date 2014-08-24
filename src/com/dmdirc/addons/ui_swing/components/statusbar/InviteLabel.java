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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.Invite;
import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.events.ServerConnectErrorEvent;
import com.dmdirc.events.ServerConnectedEvent;
import com.dmdirc.events.ServerDisconnectedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.InviteListener;
import com.dmdirc.interfaces.ui.StatusBarComponent;
import com.dmdirc.ui.IconManager;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import net.engio.mbassy.listener.Handler;

/**
 * A status bar component to show invites to the user and enable them to accept or dismiss them.
 */
public class InviteLabel extends StatusbarPopupPanel<JLabel> implements StatusBarComponent,
        InviteListener, ActionListener, SelectionListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Invite popup menu. */
    private final JPopupMenu menu;
    /** Dismiss invites menu item. */
    private final JMenuItem dismiss;
    /** Accept invites menu item. */
    private final JMenuItem accept;
    /** Parent window that will own popup windows. */
    private final Window parentWindow;
    /** Active connection. */
    private Connection activeConnection;

    /**
     * Instantiates a new invite label.
     *
     * @param eventBus      The event bus to subscribe to events on
     * @param iconManager   The manager to retrieve the invite icon from.
     * @param serverManager The manager to use to iterate servers.
     * @param mainFrame     Main frame
     */
    @Inject
    public InviteLabel(
            final DMDircMBassador eventBus,
            @GlobalConfig final IconManager iconManager,
            final ServerManager serverManager,
            final MainFrame mainFrame) {
        super(new JLabel());

        this.parentWindow = mainFrame;

        setBorder(BorderFactory.createEtchedBorder());
        label.setIcon(iconManager.getIcon("invite"));

        menu = new JPopupMenu();
        dismiss = new JMenuItem("Dismiss all invites");
        dismiss.setActionCommand("dismissAll");
        dismiss.addActionListener(this);
        accept = new JMenuItem("Accept all invites");
        accept.setActionCommand("acceptAll");
        accept.addActionListener(this);

        for (final Connection connection : serverManager.getServers()) {
            connection.addInviteListener(this);
        }

        mainFrame.addSelectionListener(this);
        eventBus.subscribe(this);

        update();
    }

    @Override
    protected StatusbarPopupWindow getWindow() {
        return new InvitePopup(this, activeConnection, parentWindow);
    }

    /**
     * Populates the menu.
     */
    private void popuplateMenu() {
        menu.removeAll();

        final Collection<Invite> invites = activeConnection.getInvites();
        for (final Invite invite : invites) {
            menu.add(new JMenuItem(new InviteAction(invite)));
        }
        menu.add(new JSeparator());
        menu.add(accept);
        menu.add(dismiss);
    }

    /**
     * Updates the invite label for the currently active server.
     */
    private void update() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (activeConnection == null || activeConnection.getInvites().isEmpty()) {
                    setVisible(false);
                    closeDialog();
                } else {
                    refreshDialog();
                    setVisible(true);
                }
            }
        });
    }

    @Override
    public void inviteReceived(final Connection connection, final Invite invite) {
        update();
    }

    @Override
    public void inviteExpired(final Connection connection, final Invite invite) {
        update();
    }

    @Handler
    public void handleServerConnected(final ServerConnectedEvent event) {
        event.getConnection().addInviteListener(this);
    }

    @Handler
    public void handleServerDisconnected(final ServerDisconnectedEvent event) {
        handleServerRemoved(event.getConnection());
    }

    @Handler
    public void handleServerConnectError(final ServerConnectErrorEvent event) {
        handleServerRemoved(event.getConnection());
    }

    private void handleServerRemoved(final Connection connection) {
        connection.removeInviteListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        super.mouseClicked(e);
        popuplateMenu();
        if (menu.getComponentCount() > 0) {
            menu.show(this, e.getX(), e.getY());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        switch (e.getActionCommand()) {
            case "acceptAll":
                activeConnection.acceptInvites();
                break;
            case "dismissAll":
                activeConnection.removeInvites();
                break;
        }
    }

    @Override
    public void selectionChanged(final TextFrame window) {
        activeConnection = window == null ? null : window.getContainer().getConnection();
        update();
    }

}

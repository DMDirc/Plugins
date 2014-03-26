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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.FrameContainer;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.dialogs.NewServerDialog;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.LifecycleController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * A menu providing server related commands to the menu bar.
 */
@Singleton
public class ServerMenu extends JMenu implements ActionListener,
        MenuListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** Lifecycle controller. */
    private final LifecycleController lifecycleController;
    /** Menu items which can be enabled/disabled. */
    private JMenuItem ssd;
    private JMenuItem disconnect;
    /** Provider to use to retrieve NSD instances. */
    private final DialogProvider<NewServerDialog> newServerProvider;
    /** Provider for server settings dialogs. */
    private final KeyedDialogProvider<Connection, ServerSettingsDialog> ssdProvider;

    /**
     * Creates a new Server menu.
     *
     * @param activeFrameManager  Active frame manager.
     * @param lifecycleController Lifecycle controller
     * @param newServerProvider   Provider to use to retrieve NSD instances.
     * @param ssdProvider         Provider to get SSD instances
     */
    @Inject
    public ServerMenu(
            final ActiveFrameManager activeFrameManager,
            final LifecycleController lifecycleController,
            final DialogProvider<NewServerDialog> newServerProvider,
            final KeyedDialogProvider<Connection, ServerSettingsDialog> ssdProvider) {
        super("Server");
        this.activeFrameManager = activeFrameManager;
        this.lifecycleController = lifecycleController;
        this.newServerProvider = newServerProvider;
        this.ssdProvider = ssdProvider;

        setMnemonic('s');
        addMenuListener(this);
        initServerMenu();
        menuSelected(null);
    }

    /**
     * Initialises the server menu.
     */
    private void initServerMenu() {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText("New Server...");
        menuItem.setMnemonic('n');
        menuItem.setActionCommand("NewServer");
        menuItem.addActionListener(this);
        add(menuItem);

        disconnect = new JMenuItem();
        disconnect.setText("Disconnect");
        disconnect.setMnemonic('d');
        disconnect.setActionCommand("Disconnect");
        disconnect.addActionListener(this);
        add(disconnect);

        ssd = new JMenuItem();
        ssd.setMnemonic('s');
        ssd.setText("Server settings");
        ssd.setActionCommand("ServerSettings");
        ssd.addActionListener(this);
        add(ssd);

        if (!Apple.isAppleUI()) {
            menuItem = new JMenuItem();
            menuItem.setText("Exit");
            menuItem.setMnemonic('x');
            menuItem.setActionCommand("Exit");
            menuItem.addActionListener(this);
            add(menuItem);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        switch (e.getActionCommand()) {
            case "NewServer":
                newServerProvider.displayOrRequestFocus();
                break;
            case "Exit":
                lifecycleController.quit();
                break;
            case "ServerSettings":
                ssdProvider.displayOrRequestFocus(
                        activeFrameManager.getActiveFrame().getContainer().getConnection());
                break;
            case "Disconnect":
                activeFrameManager.getActiveFrame().getContainer().getConnection().disconnect();
                break;
        }
    }

    @Override
    public final void menuSelected(final MenuEvent e) {
        final TextFrame activeFrame = activeFrameManager.getActiveFrame();
        final FrameContainer activeWindow = activeFrame == null ? null
                : activeFrame.getContainer();

        final boolean connected = activeWindow != null
                && activeWindow.getConnection() != null
                && activeWindow.getConnection().getState() == ServerState.CONNECTED;

        ssd.setEnabled(connected);
        disconnect.setEnabled(connected);
    }

    @Override
    public void menuDeselected(final MenuEvent e) {
        // Do nothing
    }

    @Override
    public void menuCanceled(final MenuEvent e) {
        // Do nothing
    }

}

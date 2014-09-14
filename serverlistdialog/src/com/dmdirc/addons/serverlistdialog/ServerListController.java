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

package com.dmdirc.addons.serverlistdialog;

import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JMenuItem;

/**
 * Handles registering of the server list menu items, and creation of the server list dialog.
 */
public class ServerListController {

    private final Provider<ServerListDialog> dialogProvider;
    private final MenuBar menuBar;

    @Inject
    public ServerListController(
            final Provider<ServerListDialog> dialogProvider,
            final MenuBar menuBar) {
        this.dialogProvider = dialogProvider;
        this.menuBar = menuBar;
    }

    /**
     * Adds the 'Server lists' menu item to the app's main menu.
     */
    public void addMenu() {
        final JMenuItem item = new JMenuItem("Server lists");
        item.setMnemonic('l');
        item.addActionListener(new MenuActionListener());
        menuBar.addMenuItem("Server", item);
    }

    /**
     * Removes the 'Server lists' menu item from the app's main menu.
     */
    public void removeMenu() {
        menuBar.removeMenuItem("Server", "Server lists");
    }

    /**
     * Listener that gets and displays a server list dialog when an action is performed.
     */
    private class MenuActionListener implements ActionListener {

        @Override
        public void actionPerformed(final ActionEvent e) {
            dialogProvider.get().display();
        }

    }

}

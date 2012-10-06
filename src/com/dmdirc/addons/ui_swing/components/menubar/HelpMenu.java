/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.FeedbackDialog;
import com.dmdirc.addons.ui_swing.dialogs.about.AboutDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * A menu providing help commands to the menu bar.
 */
public class HelpMenu extends JMenu implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Instantiates a new help menu.
     *
     * @param controller Swing controller
     */
    public HelpMenu(final SwingController controller) {
        super("Help");
        this.controller = controller;
        setMnemonic('h');
        initHelpMenu();
    }

    /**
     * Initialises the help menu.
     */
    private void initHelpMenu() {
        JMenuItem menuItem;

        menuItem = new JMenuItem();
        menuItem.setMnemonic('j');
        menuItem.setText("Join Dev channel");
        menuItem.setActionCommand("JoinDevChat");
        menuItem.addActionListener(this);
        add(menuItem);

        menuItem = new JMenuItem();
        menuItem.setMnemonic('f');
        menuItem.setText("Send Feedback");
        menuItem.setActionCommand("feedback");
        menuItem.addActionListener(this);
        add(menuItem);

        if (!Apple.isAppleUI()) {
            menuItem = new JMenuItem();
            menuItem.setMnemonic('a');
            menuItem.setText("About");
            menuItem.setActionCommand("About");
            menuItem.addActionListener(this);
            add(menuItem);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("About")) {
            controller.showDialog(AboutDialog.class);
        } else if (e.getActionCommand().equals("JoinDevChat")) {
            controller.getMain().getServerManager().joinDevChat();
        } else if (e.getActionCommand().equals("feedback")) {
            controller.showDialog(FeedbackDialog.class);
        }
    }
}

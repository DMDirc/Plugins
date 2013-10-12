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

package com.dmdirc.addons.serverlistdialog;

import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.plugins.implementations.BasePlugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

/**
 * Server list dialog plugin.
 */
public class ServerListDialogPlugin extends BasePlugin implements ActionListener {

    /** Swing controller. */
    private final SwingController controller;

    /** The wrapper to use for modifying performs. */
    private final PerformWrapper performWrapper;

    /**
     * Creates a new server list dialog plugin.
     *
     * @param swingController The controller that will own the dialog.
     * @param performWrapper The wrapper to use for modifying performs.
     */
    public ServerListDialogPlugin(
            final SwingController swingController,
            final PerformWrapper performWrapper) {
        this.performWrapper = performWrapper;

        controller = swingController;
        final JMenuItem item = new JMenuItem("Server lists");
        item.setMnemonic('l');
        item.addActionListener(this);
        controller.getMainFrame().getJMenuBar().addMenuItem("Server", item);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        new ServerListDialog(controller, controller.getUrlHandler(), performWrapper).display();
    }
}

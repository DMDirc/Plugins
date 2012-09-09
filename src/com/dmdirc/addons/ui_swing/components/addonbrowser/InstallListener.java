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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.addons.ui_swing.SwingController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Addon info install listener.
 */
public class InstallListener implements ActionListener {

    /** Addon info. */
    private final AddonInfo info;
    /** Parent window. */
    private final BrowserWindow parentWindow;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Instantiates a new install listener.
     *
     * @param controller Swing controller
     * @param info Addoninfo to install
     * @param parentWindow Parent window
     */
    public InstallListener(final SwingController controller,
            final AddonInfo info, final BrowserWindow parentWindow) {
        super();

        this.controller = controller;
        this.info = info;
        this.parentWindow = parentWindow;
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final InstallerWindow installer = new InstallerWindow(controller,
                parentWindow, info);
        installer.display(parentWindow);
        new InstallWorker(info, installer, controller).executeInExecutor();
    }
}

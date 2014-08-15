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

/**
 * Factory for {@link ServerInfoDialog}s.
 */

import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.components.statusbar.StatusbarPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JLabel;

@Singleton
public class ServerInfoDialogFactory {

    private final LagDisplayManager manager;
    private final MainFrame mainFrame;
    private final ServerManager serverManager;

    @Inject
    public ServerInfoDialogFactory(final LagDisplayManager manager, final MainFrame mainFrame,
            final ServerManager serverManager) {
        this.manager = manager;
        this.mainFrame = mainFrame;
        this.serverManager = serverManager;
    }

    public ServerInfoDialog getServerInfoDialog(
            final StatusbarPanel<JLabel> parent) {
        return new ServerInfoDialog(manager, parent, mainFrame, serverManager);
    }

}
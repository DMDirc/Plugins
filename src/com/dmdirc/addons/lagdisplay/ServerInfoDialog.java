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

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.components.statusbar.StatusbarPanel;
import com.dmdirc.addons.ui_swing.components.statusbar.StatusbarPopupWindow;
import com.dmdirc.addons.ui_swing.dialogs.DialogManager;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Shows information about all connected servers.
 */
public class ServerInfoDialog extends StatusbarPopupWindow {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;

    /** The lag display plugin. */
    protected final LagDisplayPlugin plugin;
    /** Swing main frame. */
    private final MainFrame mainFrame;
    /** Server manager to retrieve servers from. */
    private final ServerManager serverManager;

    /**
     * Creates a new ServerInfoDialog.
     *
     * @param plugin The {@link LagDisplayPlugin} we're using for info
     * @param parent The {@link JPanel} to use for positioning
     * @param dialogManager The manager to register this dialog with.
     * @param mainFrame The frame that will own this dialog.
     * @param serverManager The manager to use to iterate servers.
     */
    public ServerInfoDialog(
            final LagDisplayPlugin plugin,
            final StatusbarPanel parent,
            final DialogManager dialogManager,
            final MainFrame mainFrame,
            final ServerManager serverManager) {
        super(dialogManager, parent, mainFrame);

        this.plugin = plugin;
        this.mainFrame = mainFrame;
        this.serverManager = serverManager;
    }

    /** {@inheritDoc} */
    @Override
    protected void initContent(final JPanel panel) {
        final List<Server> servers = serverManager.getServers();

        if (servers.isEmpty()) {
            panel.add(new JLabel("No open servers."));
        } else {
            if (plugin.shouldShowGraph()) {
                panel.add(new PingHistoryPanel(plugin, mainFrame), "span, grow, wrap");
                panel.add(new JSeparator(), "span, grow, wrap");
            }

            for (final Server server : servers) {
                panel.add(new JLabel(server.getName()));
                panel.add(new JLabel(server.getState() == ServerState.CONNECTED
                        ? server.getNetwork() : "---", JLabel.CENTER), "grow");
                panel.add(new JLabel(server.getState() == ServerState.CONNECTED
                        ? plugin.getTime(server) : "---", JLabel.RIGHT), "grow, wrap");
            }
        }
    }


}

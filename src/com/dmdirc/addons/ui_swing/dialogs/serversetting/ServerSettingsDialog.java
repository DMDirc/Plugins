/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.addons.ui_swing.components.modes.UserModesPane;
import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.config.Identity;
import com.dmdirc.config.prefs.PreferencesManager;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify server settings and the ignore list.
 */
public final class ServerSettingsDialog extends StandardDialog implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Server settings dialogs, semi singleton use. */
    private static volatile ServerSettingsDialog me;
    /** Parent server. */
    private final Server server;
    /** User modes panel. */
    private UserModesPane modesPanel;
    /** Ignore list panel. */
    private IgnoreListPanel ignoreList;
    /** Perform panel. */
    private PerformTab performPanel;
    /** Settings panel. */
    private SettingsPanel settingsPanel;
    /** The tabbed pane. */
    private JTabbedPane tabbedPane;
    /** Parent window. */
    private final Window parentWindow;

    /**
     * Creates a new instance of ServerSettingsDialog.
     *
     * @param server The server object that we're editing settings for
     * @param parentWindow Parent window
     */
    private ServerSettingsDialog(final Server server, final Window parentWindow) {
        super(parentWindow, ModalityType.MODELESS);

        this.server = server;
        this.parentWindow = parentWindow;

        setTitle("Server settings");
        setResizable(false);

        initComponents();
        initListeners();
    }

    /**
     * Creates the dialog if one doesn't exist, and displays it.
     *
     * @param server The server object that we're editing settings for
     * @param parentWindow Parent window
     */
    public static void showServerSettingsDialog(final Server server,
            final Window parentWindow) {
        me = getServerSettingsDialog(server, parentWindow);

        me.display();
        me.requestFocusInWindow();
    }

    /**
     * Returns the current instance of the ServerSettingsDialog.
     *
     * @param server The server object that we're editing settings for
     * @param parentWindow Parent window
     *
     * @return The current ServerSettingsDialog instance
     */
    public static ServerSettingsDialog getServerSettingsDialog(
            final Server server, final Window parentWindow) {
        synchronized (ServerSettingsDialog.class) {
            if (me == null) {
                me = new ServerSettingsDialog(server, parentWindow);
            }
        }

        return me;
    }

    /**
     * Checks if a Server settings dialog exists.
     *
     * @return true iif a dialog exists
     */
    public static boolean hasServerSettingsDialog() {
        synchronized (ServerSettingsDialog.class) {
            return me != null;
        }
    }

    /** Initialises the main UI components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());

        tabbedPane = new JTabbedPane();

        modesPanel = new UserModesPane(server);

        ignoreList =
                new IgnoreListPanel(server, parentWindow);

        performPanel =
                new PerformTab(server);

        settingsPanel =
                new SettingsPanel(server.getNetworkIdentity(),
                "These settings are specific to this " +
                "network, any settings specified here will overwrite global " +
                "settings");

        if (settingsPanel != null) {
            addSettings();
        }

        final JScrollPane userModesSP = new JScrollPane(modesPanel);
        userModesSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        userModesSP.setOpaque(UIUtilities.getTabbedPaneOpaque());
        userModesSP.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
        userModesSP.setBorder(null);

        tabbedPane.add("User modes", userModesSP);
        tabbedPane.add("Ignore list", ignoreList);
        tabbedPane.add("Perform", performPanel);
        if (settingsPanel != null) {
            tabbedPane.add("Settings", settingsPanel);
        }

        setLayout(new MigLayout("fill, wrap 1, hmax 80sp"));

        add(tabbedPane, "grow");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");

        tabbedPane.setSelectedIndex(server.getConfigManager().
                getOptionInt("dialogstate", "serversettingsdialog"));
    }

    /** Adds the settings to the panel. */
    private void addSettings() {
        settingsPanel.addOption(PreferencesManager.getPreferencesManager()
                .getServerSettings(server.getConfigManager(), server.getServerIdentity()));
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /** Saves the settings from this dialog. */
    public void saveSettings() {
        if (server.getState() != ServerState.CONNECTED) {
            new StandardQuestionDialog(parentWindow, ModalityType.MODELESS,
                    "Server has been disconnected.", "Any changes you have " +
                    "made will be lost, are you sure you want to close this " +
                    "dialog?") {

                private static final long serialVersionUID = 1;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    ServerSettingsDialog.this.dispose();
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    //Ignore
                }
            }.display(parentWindow);
        } else {
            closeAndSave();
        }
    }

    /** Closes this dialog and saves the settings. */
    private void closeAndSave() {
        modesPanel.save();
        settingsPanel.save();
        performPanel.savePerforms();
        ignoreList.saveList();

        final Identity identity = server.getNetworkIdentity();
        identity.setOption("dialogstate", "serversettingsdialog",
                String.valueOf(tabbedPane.getSelectedIndex()));

        dispose();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            saveSettings();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (me == null) {
            return;
        }
        synchronized (me) {
            super.dispose();
            me = null;
        }
    }
}

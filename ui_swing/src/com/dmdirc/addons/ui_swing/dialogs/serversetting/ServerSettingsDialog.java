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

package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.ServerState;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.addons.ui_swing.components.modes.UserModesPane;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify server settings and the ignore list.
 */
public class ServerSettingsDialog extends StandardDialog implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Parent connection. */
    private final Connection connection;
    /** Perform wrapper for the perform panel. */
    private final PerformWrapper performWrapper;
    /** Preferences manager to retrieve settings from. */
    private final PreferencesManager preferencesManager;
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

    /**
     * Creates a new instance of ServerSettingsDialog.
     *
     * @param preferencesManager Preferences manager to retrieve settings from
     * @param compFactory        Preferences setting component factory
     * @param performWrapper     Wrapper for the perform tab.
     * @param connection         The server object that we're editing settings for
     * @param parentWindow       Parent window
     */
    public ServerSettingsDialog(
            final PreferencesManager preferencesManager,
            final PrefsComponentFactory compFactory,
            final PerformWrapper performWrapper,
            final Connection connection,
            final Window parentWindow,
            final ColourManagerFactory colourManagerFactory) {
        super(parentWindow, ModalityType.MODELESS);
        this.connection = connection;
        this.performWrapper = performWrapper;
        this.preferencesManager = preferencesManager;

        setTitle("Server settings");
        setResizable(false);

        initComponents(parentWindow, connection.getWindowModel().getConfigManager(), compFactory,
                colourManagerFactory);
        initListeners();
    }

    /**
     * Initialises the main UI components.
     *
     * @param parentWindow The window that owns this dialog
     * @param config       Config to read from
     * @param compFactory  Preferences setting component factory
     */
    private void initComponents(
            final Window parentWindow,
            final AggregateConfigProvider config,
            final PrefsComponentFactory compFactory,
            final ColourManagerFactory colourManagerFactory) {
        orderButtons(new JButton(), new JButton());

        tabbedPane = new JTabbedPane();

        modesPanel = new UserModesPane(connection);

        ignoreList = new IgnoreListPanel(connection.getWindowModel().getIconManager(),
                connection, parentWindow);

        performPanel = new PerformTab(connection.getWindowModel().getIconManager(),
                colourManagerFactory, config, performWrapper, connection);

        settingsPanel = new SettingsPanel(connection.getWindowModel().getIconManager(), compFactory,
                "These settings are specific to this network, any settings specified here will "
                + "overwrite global settings");

        addSettings();

        final JScrollPane userModesSP = new JScrollPane(modesPanel);
        userModesSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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

        tabbedPane.setSelectedIndex(connection.getWindowModel().getConfigManager().
                getOptionInt("dialogstate", "serversettingsdialog"));
    }

    /** Adds the settings to the panel. */
    private void addSettings() {
        settingsPanel.addOption(preferencesManager.getServerSettings(
                connection.getWindowModel().getConfigManager(),
                connection.getServerIdentity()));
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /** Saves the settings from this dialog. */
    public void saveSettings() {
        if (connection.getState() == ServerState.CONNECTED) {
            closeAndSave();
        } else {
            new StandardQuestionDialog(getOwner(), ModalityType.MODELESS,
                    "Server has been disconnected.", "Any changes you have " +
                    "made will be lost, are you sure you want to close this " + "dialog?",
                    () -> { dispose(); }).display();
        }
    }

    /** Closes this dialog and saves the settings. */
    private void closeAndSave() {
        modesPanel.save();
        settingsPanel.save();
        performPanel.savePerforms();
        ignoreList.saveList();

        final ConfigProvider identity = connection.getNetworkIdentity();
        identity.setOption("dialogstate", "serversettingsdialog",
                String.valueOf(tabbedPane.getSelectedIndex()));

        dispose();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            saveSettings();
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }

}

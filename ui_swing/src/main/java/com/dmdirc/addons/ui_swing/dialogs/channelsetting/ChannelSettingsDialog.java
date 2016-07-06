/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.addons.ui_swing.components.modes.ChannelModesPane;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.input.TabCompleterUtils;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows the user to modify channel settings (modes, topics, etc).
 */
public class ChannelSettingsDialog extends StandardDialog implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 8;
    /** The channel object that this dialog belongs to. */
    private final GroupChat channel;
    /** Channel identity file. */
    private final ConfigProvider identity;
    /** Channel window. */
    private final WindowModel channelWindow;
    private final IconManager iconManager;
    /** Tabbed pane. */
    private JTabbedPane tabbedPane;
    /** Client settings panel. */
    private SettingsPanel channelSettingsPane;
    /** List modes panel. */
    private ChannelModesPane channelModesPane;
    /** List modes panel. */
    private TopicPane topicModesPane;
    /** List modes panel. */
    private ChannelListModesPane channelListModesPane;
    /** The config to write settings to. */
    private final ConfigProvider userConfig;
    /** Service manager. */
    private final ServiceManager serviceManager;
    /** Preferences Manager. */
    private final PreferencesManager preferencesManager;
    /** Preferences setting component factory. */
    private final PrefsComponentFactory compFactory;
    /** Clipboard to copy and paste from. */
    private final Clipboard clipboard;
    /** The controller to use to retrieve command information. */
    private final CommandController commandController;
    /** Colour manager factory. */
    private final ColourManagerFactory colourManagerFactory;

    /**
     * Creates a new instance of ChannelSettingsDialog.
     *
     * @param identityFactory    Identity factory
     * @param windowFactory      Swing window factory
     * @param userConfig         The config to write global settings to.
     * @param serviceManager     Service manager
     * @param preferencesManager Preferences Manager
     * @param compFactory        Preferences setting component factory
     * @param groupChat          The group chat object that we're editing settings for
     * @param parentWindow       Parent window
     * @param clipboard          Clipboard to copy and paste from
     * @param commandController  The controller to use to retrieve command information.
     */
    public ChannelSettingsDialog(
            final IdentityFactory identityFactory,
            final SwingWindowFactory windowFactory,
            final ConfigProvider userConfig,
            final ServiceManager serviceManager,
            final PreferencesManager preferencesManager,
            final PrefsComponentFactory compFactory,
            final GroupChat groupChat,
            final Window parentWindow,
            final Clipboard clipboard,
            final CommandController commandController,
            final ColourManagerFactory colourManagerFactory,
            final TabCompleterUtils tabCompleterUtils,
            final IconManager iconManager) {
        super(parentWindow, ModalityType.MODELESS);

        this.userConfig = checkNotNull(userConfig);
        this.serviceManager = checkNotNull(serviceManager);
        this.preferencesManager = checkNotNull(preferencesManager);
        this.compFactory = checkNotNull(compFactory);
        this.channel = checkNotNull(groupChat);
        this.clipboard = clipboard;
        this.commandController = checkNotNull(commandController);
        this.colourManagerFactory = colourManagerFactory;
        this.iconManager = iconManager;

        identity = identityFactory.createChannelConfig(groupChat.getConnection().get().getNetwork(),
                groupChat.getName());
        channelWindow = groupChat.getWindowModel();

        initComponents(tabCompleterUtils);
        initListeners();
    }

    /** Initialises the main UI components. */
    private void initComponents(final TabCompleterUtils tabCompleterUtils) {
        tabbedPane = new JTabbedPane();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Channel settings for " + channel.getName());
        setResizable(false);

        orderButtons(new JButton(), new JButton());

        getContentPane().setLayout(new MigLayout(
                "fill, wrap 1, ins panel, hmax 80sp, wmin 460, wmax 460"));
        getContentPane().add(tabbedPane, "growy, pushy");
        getContentPane().add(getLeftButton(), "split 3, right");
        getContentPane().add(getRightButton(), "right");

        initTopicTab(tabCompleterUtils);

        initIrcTab();

        initListModesTab();

        initSettingsTab();

        tabbedPane.setSelectedIndex(channel.getWindowModel().getConfigManager().
                getOptionInt("dialogstate", "channelsettingsdialog"));
    }

    /** Initialises the Topic tab. */
    private void initTopicTab(final TabCompleterUtils tabCompleterUtils) {
        topicModesPane = new TopicPane(channel, iconManager,
                commandController, serviceManager,
                this, channelWindow, clipboard, colourManagerFactory, tabCompleterUtils);
        tabbedPane.addTab("Topic", topicModesPane);
    }

    /** Initialises the IRC Settings tab. */
    private void initIrcTab() {
        channelModesPane = new ChannelModesPane(channel, iconManager);

        final JScrollPane channelModesSP = new JScrollPane(channelModesPane);
        channelModesSP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        channelModesSP.setOpaque(UIUtilities.getTabbedPaneOpaque());
        channelModesSP.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
        channelModesSP.setBorder(null);

        tabbedPane.addTab("Channel Modes", channelModesSP);
    }

    /** Initialises the IRC Settings tab. */
    private void initListModesTab() {
        channelListModesPane = new ChannelListModesPane(channel.getWindowModel().getConfigManager(),
                userConfig, iconManager, channel, this);
        tabbedPane.addTab("List Modes", channelListModesPane);
    }

    /** Initialises the channel Settings (identities) tab. */
    private void initSettingsTab() {
        initSettingsPanel();

        tabbedPane.addTab("Client Settings", channelSettingsPane);
    }

    /** Initialises the channel settings. */
    private void initSettingsPanel() {
        channelSettingsPane = new SettingsPanel(iconManager, compFactory,
                "These settings are specific to this channel on this network,"
                + " any settings specified here will overwrite global settings");
        channelSettingsPane.addOption(preferencesManager.getChannelSettings(
                channel.getWindowModel().getConfigManager(), identity));
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
    }

    /**
     * Called whenever the user clicks on one of the two buttons.
     *
     * @param actionEvent Event generated by this action
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            save();
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            dispose();
        }
    }

    /** Saves the settings. */
    public void save() {
        channelModesPane.save();
        topicModesPane.setChangedTopic();
        channelSettingsPane.save();
        channelListModesPane.save();

        identity.setOption("dialogstate", "channelsettingsdialog",
                String.valueOf(tabbedPane.getSelectedIndex()));

        dispose();
    }

}

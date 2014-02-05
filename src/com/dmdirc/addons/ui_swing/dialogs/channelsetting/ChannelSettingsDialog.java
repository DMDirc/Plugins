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

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.expandingsettings.SettingsPanel;
import com.dmdirc.addons.ui_swing.components.modes.ChannelModesPane;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.interfaces.ui.InputWindow;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify channel settings (modes, topics, etc).
 */
public class ChannelSettingsDialog extends StandardDialog implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 8;
    /** The channel object that this dialog belongs to. */
    private final Channel channel;
    /** Channel identity file. */
    private final ConfigProvider identity;
    /** Channel window. */
    private final InputWindow channelWindow;
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
    /** Swing controller. */
    private final SwingController controller;
    /** Service manager. */
    private final ServiceManager serviceManager;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Preferences Manager. */
    private final PreferencesManager preferencesManager;
    /** Preferences setting component factory. */
    private final PrefsComponentFactory compFactory;

    /**
     * Creates a new instance of ChannelSettingsDialog.
     *
     * @param controller Swing controller
     * @param identityFactory Identity factory
     * @param windowFactory Swing window factory
     * @param iconManager Icon manager
     * @param serviceManager Service manager
     * @param preferencesManager Preferences Manager
     * @param compFactory Preferences setting component factory
     * @param newChannel The channel object that we're editing settings for
     * @param parentWindow Parent window
     */
    public ChannelSettingsDialog(
            final SwingController controller,
            final IdentityFactory identityFactory,
            final SwingWindowFactory windowFactory,
            final IconManager iconManager,
            final ServiceManager serviceManager,
            final PreferencesManager preferencesManager,
            final PrefsComponentFactory compFactory,
            final Channel newChannel,
            final MainFrame parentWindow) {
        super(parentWindow, ModalityType.MODELESS);

        this.controller = controller;
        this.iconManager = iconManager;
        this.serviceManager = serviceManager;
        this.preferencesManager = preferencesManager;
        this.compFactory = compFactory;

        channel = newChannel;
        identity = identityFactory.createChannelConfig(channel.getConnection().getNetwork(),
                channel.getChannelInfo().getName());
        this.channelWindow = (InputWindow) windowFactory.getSwingWindow(newChannel);

        initComponents();
        initListeners();
    }

    /** Initialises the main UI components. */
    private void initComponents() {
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

        initTopicTab();

        initIrcTab();

        initListModesTab();

        initSettingsTab();

        tabbedPane.setSelectedIndex(channel.getConfigManager().
                getOptionInt("dialogstate", "channelsettingsdialog"));
    }

    /** Initialises the Topic tab. */
    private void initTopicTab() {
        topicModesPane = new TopicPane(channel, iconManager, serviceManager, this, channelWindow);
        tabbedPane.addTab("Topic", topicModesPane);
    }

    /** Initialises the IRC Settings tab. */
    private void initIrcTab() {
        channelModesPane = new ChannelModesPane(controller, channel);

        final JScrollPane channelModesSP = new JScrollPane(channelModesPane);
        channelModesSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        channelModesSP.setOpaque(UIUtilities.getTabbedPaneOpaque());
        channelModesSP.getViewport().setOpaque(UIUtilities.getTabbedPaneOpaque());
        channelModesSP.setBorder(null);

        tabbedPane.addTab("Channel Modes", channelModesSP);
    }

    /** Initialises the IRC Settings tab. */
    private void initListModesTab() {
        channelListModesPane = new ChannelListModesPane(controller, channel, this);
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
                channel.getConfigManager(), identity));
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

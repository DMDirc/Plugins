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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.NickList;
import com.dmdirc.addons.ui_swing.components.SplitPane;
import com.dmdirc.addons.ui_swing.components.TopicBar;
import com.dmdirc.addons.ui_swing.components.TopicBarFactory;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.events.ClientClosingEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;

/**
 * The channel frame is the GUI component that represents a channel to the user.
 */
@Factory(inject = true, singleton = true, providers = true)
public final class ChannelFrame extends InputTextFrame implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 10;
    /** Identity. */
    private final ConfigProvider identity;
    /** split pane. */
    private SplitPane splitPane;
    /** popup menu item. */
    private JMenuItem settingsMI;
    /** Nicklist. */
    private NickList nicklist;
    /** Topic bar. */
    private TopicBar topicBar;
    /** UI controller. */
    private final SwingController controller;
    /** Event bus to despatch events on. */
    private final EventBus eventBus;
    /** Config to read settings from. */
    private final AggregateConfigProvider globalConfig;

    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers, and default options
     * for the form.
     *
     * @param deps               The dependencies required by text frames.
     * @param inputFieldProvider The provider to use to create a new input field.
     * @param identityFactory    The factory to use to create a channel identity.
     * @param topicBarFactory    The factory to use to create topic bars.
     * @param owner              The Channel object that owns this frame
     */
    public ChannelFrame(
            final TextFrameDependencies deps,
            final Provider<SwingInputField> inputFieldProvider,
            final IdentityFactory identityFactory,
            final TopicBarFactory topicBarFactory,
            @Unbound final Channel owner) {
        super(deps, inputFieldProvider, owner);

        this.controller = deps.controller;
        this.eventBus = deps.eventBus;
        this.globalConfig = deps.globalConfig;

        initComponents(topicBarFactory);

        globalConfig.addChangeListener("ui", "channelSplitPanePosition", this);
        globalConfig.addChangeListener(controller.getDomain(), "shownicklist", this);
        eventBus.register(this);

        identity = identityFactory.createChannelConfig(owner.getConnection().getNetwork(),
                owner.getChannelInfo().getName());
    }

    /**
     * Retrieves this channel frame's nicklist component.
     *
     * @return This channel's nicklist
     */
    public NickList getNickList() {
        return nicklist;
    }

    /**
     * Returns the topic bar for this channel frame.
     *
     * @return Topic bar or null if none exists
     */
    public TopicBar getTopicBar() {
        return topicBar;
    }

    /**
     * Initialises the components in this frame.
     *
     * @param topicBarFactory The factory to use to produce topic bars.
     */
    private void initComponents(final TopicBarFactory topicBarFactory) {
        topicBar = topicBarFactory.getTopicBar((Channel) getContainer(),
                getContainer().getIconManager());

        nicklist = new NickList(this, getContainer().getConfigManager());
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(this);

        splitPane = new SplitPane(globalConfig, SplitPane.Orientation.HORIZONTAL);

        setLayout(new MigLayout("fill, ins 0, hidemode 3, wrap 1"));

        add(topicBar, "growx");
        add(splitPane, "grow, push");
        add(getSearchBar(), "growx");
        add(inputPanel, "growx");

        splitPane.setLeftComponent(getTextPane());
        if (getContainer().getConfigManager().getOptionBool(
                controller.getDomain(), "shownicklist")) {
            splitPane.setRightComponent(nicklist);
        } else {
            splitPane.setRightComponent(null);
        }
        splitPane.setResizeWeight(1);
        splitPane.setDividerLocation(-1);
    }

    /**
     * {@inheritDoc}.
     *
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == settingsMI) {
            controller.showChannelSettingsDialog((Channel) getContainer());
        }
    }

    /**
     * Returns the splitpane.
     *
     * @return nicklist JSplitPane
     */
    public JSplitPane getSplitPane() {
        return splitPane;
    }

    @Override
    public void configChanged(final String domain, final String key) {
        super.configChanged(domain, key);

        if ("channelSplitPanePosition".equals(key)) {
            final int splitPanePosition = getContainer().getConfigManager()
                    .getOptionInt("ui", "channelSplitPanePosition");
            UIUtilities.invokeLater(new Runnable() {
                /** {@inheritDoc} */
                @Override
                public void run() {
                    nicklist.setPreferredSize(
                            new Dimension(splitPanePosition, 0));
                    splitPane.setDividerLocation(splitPane.getWidth() - splitPane.
                            getDividerSize() - splitPanePosition);
                }
            });
        }
        if ("shownicklist".equals(key)) {
            if (getContainer().getConfigManager()
                    .getOptionBool(controller.getDomain(), "shownicklist")) {
                splitPane.setRightComponent(nicklist);
            } else {
                splitPane.setRightComponent(null);
            }
        }
    }

    @Subscribe
    public void handleClientClosing(final ClientClosingEvent event) {
        saveSplitPanePosition();
    }

    private void saveSplitPanePosition() {
        UIUtilities.invokeAndWait(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                if (getContainer().getConfigManager().getOptionInt("ui",
                        "channelSplitPanePosition") != nicklist.getWidth()) {
                    identity.setOption("ui", "channelSplitPanePosition",
                            nicklist.getWidth());
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.CHAN_NICK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getChannelPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.CHAN_HYPERLINK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNormalPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        if (getContainer().getConnection().getState().equals(ServerState.CONNECTED)) {
            settingsMI.setEnabled(true);
        } else {
            settingsMI.setEnabled(false);
        }
        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }
        popupMenu.add(settingsMI);
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final FrameContainer window) {
        saveSplitPanePosition();
        topicBar.close();

        super.windowClosing(window);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        eventBus.unregister(this);
        globalConfig.removeListener(this);
        super.dispose();
    }

}

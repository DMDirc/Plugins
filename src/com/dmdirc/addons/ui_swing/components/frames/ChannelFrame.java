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
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.NickList;
import com.dmdirc.addons.ui_swing.components.SplitPane;
import com.dmdirc.addons.ui_swing.components.TopicBar;
import com.dmdirc.addons.ui_swing.components.TopicBarFactory;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;

/**
 * The channel frame is the GUI component that represents a channel to the user.
 */
@Factory(inject = true, singleton = true, providers = true)
public final class ChannelFrame extends InputTextFrame implements ActionListener,
        com.dmdirc.interfaces.ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
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

    /**
     * Creates a new instance of ChannelFrame. Sets up callbacks and handlers,
     * and default options for the form.
     *
     * @param deps The dependencies required by text frames.
     * @param identityFactory The factory to use to create a channel identity.
     * @param topicBarFactory The factory to use to create topic bars.
     * @param urlBuilder The URL Builder to use for constructing icons paths.
     * @param owner The Channel object that owns this frame
     */
    public ChannelFrame(
            final TextFrameDependencies deps,
            final IdentityFactory identityFactory,
            final TopicBarFactory topicBarFactory,
            final URLBuilder urlBuilder,
            @Unbound final Channel owner) {
        super(deps, owner);
        this.controller = getController();

        initComponents(topicBarFactory, urlBuilder);

        controller.getGlobalConfig().addChangeListener("ui",
                "channelSplitPanePosition", this);
        controller.getGlobalConfig().addChangeListener(
                controller.getDomain(), "shownicklist", this);
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.CLIENT_CLOSING);

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
     * Initialises the compoents in this frame.
     */
    private void initComponents(
            final TopicBarFactory topicBarFactory,
            final URLBuilder urlBuilder) {
        topicBar = topicBarFactory.getTopicBar((Channel) getContainer(),
                getContainer().getIconManager(urlBuilder));

        nicklist = new NickList(this, getContainer().getConfigManager());
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(this);

        splitPane = new SplitPane(controller.getGlobalConfig(),
                SplitPane.Orientation.HORIZONTAL);

        setLayout(new MigLayout("fill, ins 0, hidemode 3, wrap 1"));

        add(topicBar, "growx");
        add(splitPane, "grow, push");
        add(getSearchBar(), "growx");
        add(inputPanel, "growx");

        splitPane.setLeftComponent(getTextPane());
        if (getContainer().getConfigManager().getOptionBool(getController()
                .getDomain(), "shownicklist")) {
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
            getController().showChannelSettingsDialog((Channel) getContainer());
        }
    }

    /**
     * Returns the splitpane.
     * @return nicklist JSplitPane
     */
    public JSplitPane getSplitPane() {
        return splitPane;
    }

    /** {@inheritDoc} */
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
            if (getContainer().getConfigManager().getOptionBool(getController()
                    .getDomain(), "shownicklist")) {
                splitPane.setRightComponent(nicklist);
            } else {
                splitPane.setRightComponent(null);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
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
        ActionManager.getActionManager().unregisterListener(this);
        controller.getGlobalConfig().removeListener(this);
        super.dispose();
    }
}

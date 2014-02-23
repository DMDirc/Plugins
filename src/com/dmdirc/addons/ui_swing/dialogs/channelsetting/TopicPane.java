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
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.ui.InputWindow;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/** Topic panel. */
public class TopicPane extends JPanel implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 2;
    /** Parent channel. */
    private final Channel channel;
    /** Channel window. */
    private final InputWindow channelWindow;
    /** Parent dialog. */
    private final ChannelSettingsDialog parent;
    /** Topic history panel. */
    private TopicHistoryPane topicHistoryPane;
    /** Topic display pane. */
    private TopicDisplayPane topicDisplayPane;

    /**
     * Creates a new instance of TopicModesPane.
     *
     * @param channel        Parent channel
     * @param iconManager    Icon manager
     * @param serviceManager Service manager
     * @param parent         Parent dialog
     * @param channelWindow  Channel window
     */
    public TopicPane(final Channel channel, final IconManager iconManager,
            final ServiceManager serviceManager, final ChannelSettingsDialog parent,
            final InputWindow channelWindow) {
        super();

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        this.channel = channel;
        this.parent = parent;
        this.channelWindow = channelWindow;

        update(iconManager, serviceManager);
    }

    /** Updates the panel. */
    private void update(final IconManager iconManager, final ServiceManager serviceManager) {
        setVisible(false);

        removeAll();
        initTopicsPanel(iconManager, serviceManager);
        layoutComponents();

        topicHistoryPane.addActionListener(this);

        setVisible(true);
    }

    /** Initialises the topic panel. */
    private void initTopicsPanel(final IconManager iconManager,
            final ServiceManager serviceManager) {
        topicDisplayPane = new TopicDisplayPane(channel, iconManager, serviceManager, parent,
                channelWindow);
        topicHistoryPane = new TopicHistoryPane(channel);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 1, fill, wmax 450"));

        add(topicDisplayPane, "grow, push");
        add(topicHistoryPane, "grow, pushy");
    }

    /** Processes the topic and changes it if necessary. */
    protected void setChangedTopic() {
        final String topic = topicDisplayPane.getTopic();
        if (!channel.getChannelInfo().getTopic().equals(topic)) {
            channel.setTopic(topic);
        }
    }

    /**
     * {@inheritDoc}.
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e != null && e.getSource() == topicHistoryPane) {
            topicDisplayPane.setTopic(topicHistoryPane.getSelectedTopic());
        }
    }

}
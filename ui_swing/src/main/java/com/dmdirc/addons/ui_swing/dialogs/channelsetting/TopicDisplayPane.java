/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.Topic;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.ReplacePasteAction;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
import com.dmdirc.addons.ui_swing.components.inputfields.TextAreaInputField;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.input.TabCompleterUtils;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * Class to display a topic to an end user as part of the groupChat settings dialog.
 */
public class TopicDisplayPane extends JPanel implements DocumentListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Parent topic pane. */
    private final ChannelSettingsDialog parent;
    /** Associated group chat. */
    private final GroupChat groupChat;
    /** Channel window. */
    private final WindowModel channelWindow;
    /** the maximum length allowed for a topic. */
    private final int topicLengthMax;
    /** Clipboard to copy and paste from. */
    private final Clipboard clipboard;
    /** label showing the number of characters left in a topic. */
    private JLabel topicLengthLabel;
    /** Topic text entry text area. */
    private TextAreaInputField topicText;
    /** Topic who. */
    private TextLabel topicWho;

    /**
     * Creates a new topic display panel. This panel shows an editable version of the current topic
     * along with relating meta data and validates the length of the new input.
     *
     * @param groupChat         Associated group chat
     * @param iconManager       Icon manager
     * @param serviceManager    Service manager
     * @param parent            Parent settings dialog
     * @param channelWindow     Channel window
     * @param clipboard         Clipboard to copy and paste
     * @param commandController The controller to use to retrieve command information.
     */
    public TopicDisplayPane(final GroupChat groupChat, final IconManager iconManager,
            final ServiceManager serviceManager, final ChannelSettingsDialog parent,
            final WindowModel channelWindow, final Clipboard clipboard,
            final CommandController commandController,
            final ColourManagerFactory colourManagerFactory,
            final TabCompleterUtils tabCompleterUtils) {
        this.clipboard = clipboard;
        this.groupChat = groupChat;
        this.parent = parent;
        topicLengthMax = groupChat.getConnection().get().getParser().get().getMaxTopicLength();
        this.channelWindow = channelWindow;

        initComponents(iconManager, groupChat.getWindowModel().getConfigManager(), serviceManager,
                commandController, colourManagerFactory, tabCompleterUtils);
        addListeners();
        layoutComponents();

        setTopic(groupChat.getCurrentTopic());
    }

    private void initComponents(
            final IconManager iconManager,
            final AggregateConfigProvider config,
            final ServiceManager serviceManager,
            final CommandController commandController,
            final ColourManagerFactory colourManagerFactory,
            final TabCompleterUtils tabCompleterUtils) {
        topicLengthLabel = new JLabel();
        topicText = new TextAreaInputField(iconManager, colourManagerFactory, config, 100, 4);
        topicWho = new TextLabel();
        topicWho.setOpaque(false);

        topicText.setLineWrap(true);
        topicText.setWrapStyleWord(true);
        topicText.setRows(5);
        topicText.setColumns(30);
        final SwingInputHandler handler = new SwingInputHandler(serviceManager, topicText,
                commandController,
                groupChat.getWindowModel().getInputModel().get().getCommandParser(),
                channelWindow, tabCompleterUtils, groupChat.getEventBus());
        handler.setTypes(true, false, true, false);
        handler.setTabCompleter(groupChat.getWindowModel().getInputModel().get().getTabCompleter());

        topicText.getActionMap().put("paste-from-clipboard",
                new ReplacePasteAction(clipboard, "(\r\n|\n|\r)", " "));
        topicText.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                0), new TopicEnterAction(parent));
        topicText.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                UIUtilities.getCtrlDownMask()), new TopicEnterAction(parent));

        UIUtilities.addUndoManager(topicText);
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        topicText.getDocument().addDocumentListener(this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("wrap 1, fill, ins 0"));

        add(new JScrollPane(topicText), "grow, push");
        add(topicLengthLabel, "pushx, growx, pushx");
        add(topicWho, "growx, pushx");
    }

    /**
     * Sets the topic for this display panel.
     *
     * @param topic New topic
     */
    public void setTopic(final Optional<Topic> topic) {
        if (topic.isPresent()) {
            topicWho.setText("Topic set by " + topic.get().getClient()
                    .map(GroupChatUser::getNickname).orElse("Unknown")
                    + "<br> on " + topic.get().getDate());
            topicText.setText(topic.get().getTopic());
        } else {
            topicWho.setText("No topic set.");
        }
    }

    /**
     * Gets the topic text currently being displayed
     *
     * @return Current topic text
     */
    public String getTopic() {
        return topicText.getText();
    }

    /** Handles the topic change. */
    private void topicChanged() {
        if (topicLengthMax == 0) {
            topicLengthLabel.setForeground(Color.BLACK);
            topicLengthLabel.setText(topicText.getText().length()
                    + " characters");
        } else {
            final int charsLeft = topicLengthMax - topicText.getText().length();
            if (charsLeft >= 0) {
                topicLengthLabel.setForeground(Color.BLACK);
                topicLengthLabel.setText(charsLeft + " of " + topicLengthMax
                        + " available");
            } else {
                topicLengthLabel.setForeground(Color.RED);
                topicLengthLabel.setText(0 + " of " + topicLengthMax
                        + " available " + (-1 * charsLeft)
                        + " too many characters");
            }
        }
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        topicChanged();
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        topicChanged();
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        //Ignore
    }

}

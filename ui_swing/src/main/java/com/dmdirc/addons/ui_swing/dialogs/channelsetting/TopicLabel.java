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
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.textpane.StyledDocumentMaker;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;

import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import net.miginfocom.swing.MigLayout;

/**
 * Topic Label for use in the topic history panel.
 */
public class TopicLabel extends JPanel {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Topic this label represents. */
    private final Topic topic;
    /** The group chat to which this label belongs. */
    private final GroupChat groupChat;
    /** Topic field. */
    private JEditorPane pane;
    /** Empty Attrib set. */
    private SimpleAttributeSet as;

    /**
     * Instantiates a new topic label based on the specified topic.
     *
     * @param groupChat The group chat to which this label belongs
     * @param topic   Specified topic
     *
     * @since 0.6.3
     */
    public TopicLabel(final GroupChat groupChat, final Topic topic) {
        if (topic == null) {
            throw new IllegalArgumentException();
        }

        this.topic = topic;
        this.groupChat = groupChat;

        super.setBackground(UIManager.getColor("Table.background"));
        super.setForeground(UIManager.getColor("Table.foreground"));

        init();
    }

    /**
     * Initialises the topic field.
     */
    private void initTopicField() {
        pane = new JEditorPane();
        pane.setEditorKit(new StyledEditorKit());

        pane.setFocusable(false);
        pane.setEditable(false);
        pane.setOpaque(false);

        as = new SimpleAttributeSet();
        StyleConstants.setFontFamily(as, pane.getFont().getFamily());
        StyleConstants.setFontSize(as, pane.getFont().getSize());
        if (getBackground() == null) {
            StyleConstants.setBackground(as, UIManager.getColor(
                    "Table.background"));
        } else {
            StyleConstants.setBackground(as, getBackground());
        }
        if (getForeground() == null) {
            StyleConstants.setForeground(as, UIManager.getColor(
                    "Table.foreground"));
        } else {
            StyleConstants.setForeground(as, getForeground());
        }
        StyleConstants.setUnderline(as, false);
        StyleConstants.setBold(as, false);
        StyleConstants.setItalic(as, false);
    }

    /**
     * Initialises all the components and layout.
     */
    private void init() {
        initTopicField();
        removeAll();
        setLayout(new MigLayout("fill, ins 0, debug", "[]0[]", "[]0[]"));

        if (!topic.getTopic().isEmpty()) {
            groupChat.getWindowModel().getBackBuffer().getStyliser().addStyledString(
                    new StyledDocumentMaker((StyledDocument) pane.getDocument(), as),
                    topic.getTopic());
            add(pane, "wmax 450, grow, push, wrap, gapleft 5, gapleft 5");
        }

        TextLabel label;
        if (topic.getTopic().isEmpty()) {
            label = new TextLabel("Topic unset by " + topic.getClient()
                    .map(GroupChatUser::getNickname).orElse("Unknown"));
        } else {
            label = new TextLabel("Topic set by " + topic.getClient()
                    .map(GroupChatUser::getNickname).orElse("Unknown"));
        }
        add(label, "wmax 450, grow, push, wrap, gapleft 5, pad 0");

        label = new TextLabel("on " + topic.getDate());
        add(label, "wmax 450, grow, push, wrap, gapleft 5, pad 0");

        add(new JSeparator(), "newline, span, growx, pushx");

        validate();
    }

    /**
     * Returns the topic for this label.
     *
     * @return Topic
     */
    public Topic getTopic() {
        return topic;
    }

    @Override
    public void setBackground(final Color bg) {
        super.setBackground(bg);
        // This method can be called from the super class constructor, so topic may not have been
        // instansiated.
        if (topic != null
                && ((getBackground() != null && !getBackground().equals(bg))
                || (bg != null && !bg.equals(getBackground())))) {
            init();
        }
    }

    @Override
    public void setForeground(final Color fg) {
        super.setForeground(fg);
        // This method can be called from the super class constructor, so topic may not have been
        // instansiated.
        if (topic != null
                && ((getForeground() != null && !getForeground().equals(fg))
                || (fg != null && !fg.equals(getForeground())))) {
            init();
        }
    }

    @Override
    public void repaint() {
        //Deliberate NOOP
    }

    @Override
    public void firePropertyChange(final String propertyName,
            final Object oldValue, final Object newValue) {
        //Deliberate NOOP
    }

    @Override
    public void firePropertyChange(final String propertyName,
            final boolean oldValue, final boolean newValue) {
        //Deliberate NOOP
    }

    @Override
    public void firePropertyChange(final String propertyName,
            final int oldValue, final int newValue) {
        //Deliberate NOOP
    }

}

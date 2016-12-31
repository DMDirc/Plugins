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

package com.dmdirc.addons.ui_swing.framemanager.tree;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.ImageButton;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingWindowSelectedEvent;
import com.dmdirc.addons.ui_swing.textpane.StyledDocumentMaker;
import com.dmdirc.events.FrameIconChangedEvent;
import com.dmdirc.events.FrameNameChangedEvent;
import com.dmdirc.events.UnreadStatusChangedEvent;
import com.dmdirc.ui.messages.Styliser;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import net.engio.mbassy.listener.Handler;
import net.miginfocom.swing.MigLayout;

/**
 * Node label.
 */
public class NodeLabel extends JPanel {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The window this node represents in the tree. */
    private final TextFrame window;
    /** Colour used to show if the mouse is over this node. */
    private boolean rollover;
    /** Colour used to show if this node has an active notification. */
    private Color notificationColour;
    /** Are we the selected window? */
    private boolean selected;
    /** Node icon. */
    private final ImageButton<?> icon = new ImageButton<>("", null);
    /** Text label. */
    private final JTextPane text = new JTextPane(new DefaultStyledDocument());
    /** Icon manager. */
    private final IconManager iconManager;
    /** Default foreground colour when there are no notifications. */
    private Color defaultForegroundColour;
    /** Current styled text. */
    private String currentText = "";

    /**
     * Instantiates a new node label.
     *
     * @param window Window for this node
     */
    public NodeLabel(final TextFrame window, final IconManager iconManager, final Color defaultForegroundColour) {
        this.window = window;
        this.iconManager = iconManager;
        this.defaultForegroundColour = defaultForegroundColour;

        init();
    }

    /**
     * Initialises the label.
     */
    private void init() {
        if (window == null) {
            return;
        }

        icon.setIcon(iconManager.getIcon(window.getContainer().getIcon()));
        text.setText(window.getContainer().getName());
        text.setBorder(null);

        setLayout(new MigLayout("ins 0"));
        add(icon, "left");
        add(text, "left, grow, pushx");

        icon.setToolTipText(null);
        text.setToolTipText(null);
        notificationColour = null;
        selected = false;
    }

    @Handler
    public void selectionChanged(final SwingWindowSelectedEvent event) {
        selected = event.getWindow().isPresent()
                && window.equals(event.getWindow().get());
    }

    public void unreadStatusChanged(final UnreadStatusChangedEvent event) {
        notificationColour = event.getNotificationColour().map(UIUtilities::convertColour)
                .orElse(defaultForegroundColour);
    }

    @Handler
    public void iconChanged(final FrameIconChangedEvent event) {
        if (window.getContainer().equals(event.getContainer())) {
            icon.setIcon(iconManager.getIcon(event.getIcon()));
        }
    }

    @Handler
    public void nameChanged(final FrameNameChangedEvent event) {
        if (window.getContainer().equals(event.getContainer())) {
            text.setText(event.getName());
        }
    }

    /**
     * Sets the rollover state for the node.
     *
     * @param rollover rollover state
     */
    public void setRollover(final boolean rollover) {
        this.rollover = rollover;
    }

    /**
     * Is this node a rollover node?
     *
     * @return true iff this node is a rollover node
     */
    public boolean isRollover() {
        return rollover;
    }

    /**
     * Is this node a selected node?
     *
     * @return true iff this node is a selected node
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns the notification colour for this node.
     *
     * @return notification colour or null if non set
     */
    public Color getNotificationColour() {
        return notificationColour;
    }

    /**
     * Sets the default foreground colour to use when no notification is present.
     *
     * @param defaultForegroundColour The default colour to use.
     */
    public void setDefaultForegroundColour(Color defaultForegroundColour) {
        this.defaultForegroundColour = defaultForegroundColour;
    }

    @Override
    public Font getFont() {
        return UIManager.getFont("TextPane.font");
    }

    @Override
    public void setForeground(final Color colour) {
        if (text != null) {
            text.setForeground(colour);
        }
    }

    @Override
    public void setBackground(final Color colour) {
        if (text != null) {
            text.setBackground(colour);
        }
    }

    @Override
    public void setFont(final Font font) {
        if (text != null) {
            text.setFont(font);
        }
    }

    @Override
    public void setOpaque(final boolean opacity) {
        if (text != null) {
            text.setOpaque(opacity);
        }
    }

    /**
     * Sets the styles for the text in this label.
     *
     * @param styliser Styliser to use
     * @param newText  Style to set
     */
    public void setTextStyle(final Styliser styliser, final String newText) {
        if (currentText.equals(newText + window.getContainer().getName())) {
            return;
        }
        text.setText("");
        currentText = newText + window.getContainer().getName();
        styliser.addStyledString(
                new StyledDocumentMaker((StyledDocument) text.getDocument()), newText,
                window.getContainer().getName());
    }

}

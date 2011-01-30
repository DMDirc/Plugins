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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.StatusMessage;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Previous status bar messages popup.
 */
class MessagePopup extends StatusbarTogglePanel<JLabel> {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Parent window. */
    private final Window parentWindow;
    /** List of historical messages. */
    private final List<StatusMessage> messages;
    /** Parent panel. */
    private final JPanel parent;

    /**
     * Creates a new message history popup.
     *
     * @param parent Parent to size against
     * @param parentWindow Parent window
     */
    public MessagePopup(final JPanel parent, final Window parentWindow) {
        super(new JLabel("^"),
                new SidelessEtchedBorder(SidelessEtchedBorder.Side.LEFT),
                new SidelessEtchedBorder(SidelessEtchedBorder.Side.TOP));
        this.parentWindow = parentWindow;
        this.parent = parent;
        messages = new ArrayList<StatusMessage>();
    }

    /* {@inheritDoc} */
    @Override
    protected StatusbarPopupWindow getWindow() {
        return new MessageHistoryPanel(this);
    }

    /* {@inheritDoc} */
    @Override
    public void mouseEntered(final MouseEvent e) {
        super.mouseEntered(e);
        if (!isDialogOpen()) {
            setBorder(nonSelectedBorder);
            setBackground(UIManager.getColor("ToolTip.background"));
            setForeground(UIManager.getColor("ToolTip.foreground"));
        }
    }

    /* {@inheritDoc} */
    @Override
    public void mouseExited(final MouseEvent e) {
        super.mouseExited(e);
        if (!isDialogOpen()) {
            setBorder(new SidelessEtchedBorder(SidelessEtchedBorder.Side.LEFT));
            setBackground(null);
            setForeground(null);
        }
    }

    /**
     * Adds a message to this history window.
     *
     * @param message to add
     */
    public void addMessage(final StatusMessage message) {
        synchronized(message) {
            messages.add(message);
        }
    }

    /** Message history status bar popup window. */
    private class MessageHistoryPanel extends StatusbarPopupWindow {

        /**
         * A version number for this class. It should be changed whenever the
         * class structure is changed (or anything else that would prevent
         * serialized objects being unserialized with the new class).
         */
        private static final long serialVersionUID = 2;

        /**
         * Creates a new message history window.
         *
         * @param window Parent window
         */
        public MessageHistoryPanel(final JPanel parent) {
            super(parent, parentWindow);
        }

        /* {@inheritDoc} */
        @Override
        protected void initContent(final JPanel panel) {
            panel.removeAll();
            panel.setPreferredSize(new Dimension(parent.getSize()));
            if (messages.isEmpty()) {
                panel.add(new JLabel("No previous messages."), "grow, push");
                return;
            }

            for (StatusMessage message : messages) {
                panel.add(new JLabel(message.getMessage(), message.getIconType()
                        == null ? null : IconManager.getIconManager().getIcon(
                        message.getIconType()), SwingConstants.LEFT),
                        "grow, push, wrap");
            }
        }

        /* {@inheritDoc} */
        @Override
        protected Point getPopupLocation() {
            final Point point = parent.getLocationOnScreen();
            point.translate(parent.getWidth() / 2 - this.getWidth() / 2 - 2,
                    - this.getHeight() + 1);
            final int maxX = Math.max(parentWindow.getLocationOnScreen().x
                    + parentWindow.getWidth() - 10 - getWidth(),
                    parent.getLocationOnScreen().x + parent.getWidth() - 1
                    - getWidth());
            point.x = Math.min(maxX, point.x + 1);
            return point;
        }
    }
}

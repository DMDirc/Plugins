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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.StatusMessage;
import com.dmdirc.ui.interfaces.StatusBarComponent;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Message label handles showing messages in the status bar.
 */
public class MessageLabel extends JLabel implements StatusBarComponent,
        MouseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Default status bar message. */
    private final StatusMessage defaultMessage;
    /** Message queue. */
    private final Queue<StatusMessage> queue;
    /** Current status messsage. */
    private StatusMessage currentMessage;
    /** Timer to clear the message. */
    private transient TimerTask messageTimer;

    /**
     * Instantiates a new message label.
     */
    public MessageLabel() {
        super();
        queue = new LinkedList<StatusMessage>();
        defaultMessage = new StatusMessage(null, "Ready.", null, -1,
                IdentityManager.getGlobalConfig());
        currentMessage = defaultMessage;
        setText("Ready.");
        setBorder(BorderFactory.createEtchedBorder());
        addMouseListener(this);
    }

    /**
     * Sets the message for this message label.
     *
     * @param newMessage New message
     *
     * @deprecated Should use {@link setMessage(StatusMessage)} instead
     */
    @Deprecated
    public void setMessage(final String newMessage) {
        setMessage(new StatusMessage(null, newMessage, null, -1,
                IdentityManager.getGlobalConfig()));
    }

    /**
     * Sets the message for this message label.
     *
     * @param newMessage New message
     * @param newNotifier New notifier
     *
     * @deprecated Should use {@link setMessage(StatusMessage)} instead
     */
    @Deprecated
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier) {
        setMessage(new StatusMessage(null, newMessage, newNotifier, -1,
                IdentityManager.getGlobalConfig()));
    }

    /**
     * Sets the message for this message label.
     *
     * @param iconType Icon type
     * @param newMessage New message
     *
     * @deprecated Should use {@link setMessage(StatusMessage)} instead
     */
    @Deprecated
    public void setMessage(final String iconType, final String newMessage) {
        setMessage(new StatusMessage(iconType, newMessage, null, -1,
                IdentityManager.getGlobalConfig()));
    }

    /**
     * Sets the message for this message label.
     *
     * @param iconType Icon type
     * @param newMessage New message
     * @param newNotifier New notifier
     *
     * @deprecated Should use {@link setMessage(StatusMessage)} instead
     */
    @Deprecated
    public void setMessage(final String iconType, final String newMessage,
            final StatusMessageNotifier newNotifier) {
        setMessage(new StatusMessage(iconType, newMessage, newNotifier, -1,
                IdentityManager.getGlobalConfig()));
    }

    /**
     * Sets the message for this message label.
     *
     * @param newMessage New message
     * @param newNotifier New notifier
     * @param timeout New timeout
     *
     * @deprecated Should use {@link setMessage(StatusMessage)} instead
     */
    @Deprecated
    public void setMessage(final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        setMessage(new StatusMessage(null, newMessage, newNotifier, timeout,
                IdentityManager.getGlobalConfig()));
    }

    /**
     * Sets the message for this message label.
     *
     * @param iconType Icon type
     * @param newMessage New message
     * @param newNotifier New notifier
     * @param timeout New timeout
     *
     * @deprecated Should use {@link setMessage(StatusMessage)} instead
     */
    @Deprecated
    public void setMessage(final String iconType, final String newMessage,
            final StatusMessageNotifier newNotifier, final int timeout) {
        setMessage(new StatusMessage(iconType, newMessage, newNotifier,
                timeout, IdentityManager.getGlobalConfig()));
    }

    /**
     * Sets the message for this message label.
     *
     * @param message Message object to show
     */
    public void setMessage(final StatusMessage message) {
        synchronized(queue) {
            queue.add(message);
            if (queue.size() == 1) {
                currentMessage = message;
                updateCurrentMessage();
            }
        }
    }

    /**
     * Updates the message label to show the current message info.
     */
    private void updateCurrentMessage() {
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (currentMessage.getIconType() == null) {
                    setIcon(null);
                } else {
                    setIcon(IconManager.getIconManager().getIcon(
                            currentMessage.getIconType()));
                }
                setText(UIUtilities.clipStringifNeeded(MessageLabel.this,
                        currentMessage.getMessage(), getWidth()));
                if (messageTimer != null && (System.currentTimeMillis()
                        - messageTimer.scheduledExecutionTime()) <= 0) {
                    messageTimer.cancel();
                }
                if (!defaultMessage.equals(currentMessage)) {
                    messageTimer = new MessageTimerTask(MessageLabel.this);
                    new Timer("SwingStatusBar messageTimer").schedule(
                            messageTimer, new Date(System.currentTimeMillis()
                            + 250 + currentMessage.getTimeout() * 1000L));
                }
            }
        });
    }

    /**
     * Removes the message from the status bar.
     */
    public void clearMessage() {
        synchronized(queue) {
            if (queue.peek() == null) {
                currentMessage = defaultMessage;
            } else {
                currentMessage = queue.poll();
            }
            updateCurrentMessage();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (currentMessage != null
                && currentMessage.getMessageNotifier() != null) {
            currentMessage.getMessageNotifier().clickReceived(
                    e.getButton(), e.getClickCount());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }
}

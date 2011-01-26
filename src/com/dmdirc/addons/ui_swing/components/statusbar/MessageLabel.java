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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

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
    private final List<StatusMessage> messages;
    /** Current status messsage. */
    private int currentMessage;
    /** Timer to clear the message. */
    private transient TimerTask messageTimer;
    /** Set message synchronisation. */
    private final Semaphore semaphore;

    /**
     * Instantiates a new message label.
     */
    public MessageLabel() {
        super();
        currentMessage = -1;
        messages = new ArrayList<StatusMessage>();
        defaultMessage = new StatusMessage(null, "Ready.", null, -1,
                IdentityManager.getGlobalConfig());
        semaphore = new Semaphore(1);
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
        semaphore.acquireUninterruptibly();
        SwingUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                try {
                    messages.add(message);
                    currentMessage = messages.indexOf(message);
                    if (message.getIconType() == null) {
                        setIcon(null);
                    } else {
                        setIcon(IconManager.getIconManager().getIcon(
                                message.getIconType()));
                    }
                    setText(UIUtilities.clipStringifNeeded(MessageLabel.this,
                            message.getMessage(), getWidth()));

                    if (messageTimer != null && (System.currentTimeMillis()
                            - messageTimer.scheduledExecutionTime()) <= 0) {
                        messageTimer.cancel();
                    }

                    if (!defaultMessage.equals(message)) {
                        messageTimer = new MessageTimerTask(MessageLabel.this);
                        new Timer("SwingStatusBar messageTimer").schedule(
                                messageTimer, new Date(
                                System.currentTimeMillis() + 250
                                + message.getTimeout() * 1000L));
                    }
                } finally {
                    semaphore.release();
                }
            }
        });
    }

    /**
     * Removes the message from the status bar.
     */
    public void clearMessage() {
        setMessage(defaultMessage);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (currentMessage != -1 && messages.size() > currentMessage
                && messages.get(currentMessage).getMessageNotifier() != null) {
            messages.get(currentMessage).getMessageNotifier().clickReceived(
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

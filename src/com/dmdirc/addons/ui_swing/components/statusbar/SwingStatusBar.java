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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.interfaces.ui.StatusBar;
import com.dmdirc.interfaces.ui.StatusBarComponent;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.StatusMessage;

import java.awt.Component;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/** Status bar, shows message and info on the GUI. */
@Singleton
public class SwingStatusBar extends JPanel implements StatusBar {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 5;
    /** Mig layout component restraints. */
    private final String componentConstraints;
    /** Height for the status bar. */
    private final int height;
    /** message label. */
    private final MessageLabel messageLabel;
    /** error panel. */
    private final ErrorPanel errorPanel;
    /** update label. */
    private final UpdaterLabel updateLabel;
    /** Invite label. */
    private final InviteLabel inviteLabel;

    /**
     * Creates a new instance of SwingStatusBar.
     *
     * @param controller   Swing controller
     * @param mainFrame    Main frame
     * @param inviteLabel  The invite label to add to the status bar.
     * @param updaterLabel The updater label to add to the status bar.
     * @param messageLAbel The message label toa dd to the status bar.
     */
    @Inject
    public SwingStatusBar(
            final SwingController controller,
            final MainFrame mainFrame,
            final InviteLabel inviteLabel,
            final UpdaterLabel updaterLabel,
            final MessageLabel messageLabel) {
        super();

        height = getFontMetrics(UIManager.getFont("Table.font")).getHeight()
                + (int) PlatformDefaults.getUnitValueX("related").getValue()
                + (int) PlatformDefaults.getUnitValueX("related").getValue();
        componentConstraints = "sgy components, hmax " + height + ", hmin " + height
                + ", wmin 20, shrink 0";

        this.messageLabel = messageLabel;
        errorPanel = new ErrorPanel(controller, mainFrame, this);
        this.updateLabel = updaterLabel;
        this.inviteLabel = inviteLabel;

        setLayout(new MigLayout("fill, ins 0, hidemode 3"));

        add(messageLabel, "grow, push, sgy components, hmax " + height
                + ", hmin " + height);
        add(updateLabel, componentConstraints);
        add(errorPanel, componentConstraints);
        add(inviteLabel, componentConstraints);
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final StatusMessage message) {
        messageLabel.setMessage(message);
    }

    /** {@inheritDoc} */
    @Override
    public void clearMessage() {
        messageLabel.clearMessage();
    }

    /** {@inheritDoc} */
    @Override
    public void addComponent(final StatusBarComponent component) {
        if (!(component instanceof Component)) {
            Logger.appError(ErrorLevel.HIGH, "Error adding status bar component",
                    new IllegalArgumentException("Component must be an "
                    + "instance of java.awt.component"));
            return;
        }
        if (!Arrays.asList(getComponents()).contains(component)) {
            SwingUtilities.invokeLater(new Runnable() {
                /** {@inheritDoc} */
                @Override
                public void run() {
                    remove(updateLabel);
                    remove(errorPanel);
                    remove(inviteLabel);
                    add((Component) component, componentConstraints);
                    add(updateLabel, componentConstraints);
                    add(inviteLabel, componentConstraints);
                    add(errorPanel, componentConstraints);
                    validate();
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeComponent(final StatusBarComponent component) {
        if (!(component instanceof Component)) {
            Logger.appError(ErrorLevel.HIGH, "Error removing status bar "
                    + "component", new IllegalArgumentException("Component "
                    + "must be an instance of java.awt.component"));
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                remove((Component) component);
                validate();
            }
        });
    }

    /**
     * Returns the message label for this status bar. This is intended to be used for advanced
     * plugins that wish to do compliated things with messages.
     *
     * @return Message label component
     */
    public MessageLabel getMessageComponent() {
        return messageLabel;
    }

}

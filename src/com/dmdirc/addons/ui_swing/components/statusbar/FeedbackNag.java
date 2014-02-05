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

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.FeedbackDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.interfaces.ui.StatusBarComponent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Feedback nag icon.
 */
public class FeedbackNag extends JLabel implements StatusBarComponent,
        MouseListener, ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Dismiss menu. */
    private final JPopupMenu menu;
    /** Show menu item. */
    private final JMenuItem show;
    /** Swing Controller. */
    private final SwingController controller;
    /** Provider of feedback dialogs. */
    private final DialogProvider<FeedbackDialog> feedbackDialogProvider;

    /**
     * Creates a new feedback nag.
     *
     * @param controller Swing controller
     * @param feedbackDialogProvider Provider of feedback dialogs.
     */
    @Inject
    public FeedbackNag(
            final SwingController controller,
            final DialogProvider<FeedbackDialog> feedbackDialogProvider) {
        super();
        this.controller = controller;
        this.feedbackDialogProvider = feedbackDialogProvider;

        menu = new JPopupMenu();
        show = new JMenuItem("Open");
        final JMenuItem dismiss = new JMenuItem("Dismiss");

        setIcon(controller.getIconManager().getIcon("feedback"));
        setBorder(BorderFactory.createEtchedBorder());
        setToolTipText("We would appreciate any feedback you may have about "
                + "DMDirc.");

        menu.add(show);
        menu.add(dismiss);

        show.addActionListener(this);
        dismiss.addActionListener(this);
        addMouseListener(this);
        controller.getSwingStatusBar().addComponent(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.getButton() == 1) {
            feedbackDialogProvider.displayOrRequestFocus();
            controller.getSwingStatusBar().removeComponent(this);
        }
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        checkMouseEvent(e);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        checkMouseEvent(e);
    }

    /**
     * Checks a mouse event for a popup trigger.
     *
     * @param e Mouse event
     */
    private void checkMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            menu.show(this, e.getX(), e.getY());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == show) {
            feedbackDialogProvider.displayOrRequestFocus();
        }
        controller.getSwingStatusBar().removeComponent(this);
    }
}

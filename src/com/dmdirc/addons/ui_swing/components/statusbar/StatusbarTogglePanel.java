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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

/**
 * A panel shown in the status bar which displays a {@link StatusbarPopupWindow}
 * when the clicks it.
 *
 * @since 0.6.6
 */
public abstract class StatusbarTogglePanel extends StatusbarPanel
        implements ComponentListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;

    /**
     * Creates a new {@link StatusbarTogglePanel}, using a default text label.
     */
    public StatusbarTogglePanel() {
        this(new JLabel("Unknown"));
    }

    /**
     * Creates a new {@link StatusbarTogglePanel}, using the specified label.
     *
     * @param label The label to be displayed in the status bar
     */
    public StatusbarTogglePanel(final JLabel label) {
        super(label);

        addComponentListener(this);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
        if (isDialogOpen()) {
            setBackground(null);
            setForeground(null);
            setBorder(new EtchedBorder());
            closeDialog();
        } else {
            setBackground(UIManager.getColor("ToolTip.background"));
            setForeground(UIManager.getColor("ToolTip.foreground"));
            setBorder(new SidelessEtchedBorder(SidelessEtchedBorder.Side.TOP));
            openDialog();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        // Don't care
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        // Don't care
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        // Don't care
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        // Don't care
    }

    /** {@inheritDoc} */
    @Override
    public void componentResized(final ComponentEvent e) {
        refreshDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void componentMoved(final ComponentEvent e) {
        refreshDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void componentShown(final ComponentEvent e) {
        // Don't care
    }

    /** {@inheritDoc} */
    @Override
    public void componentHidden(final ComponentEvent e) {
        // Don't care
    }
}

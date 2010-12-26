/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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

package com.dmdirc.addons.ui_swing.framemanager.buttonbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import net.miginfocom.swing.MigLayout;

/**
 * Implements scrollable onto a JPanel so we have more control over scrolling.
 *
 * @since 0.6.4
 */
public class ButtonPanel extends JPanel implements Scrollable,
        MouseWheelListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** The ButtonBar that created this Panel. */
    private final ButtonBar buttonBar;

    /**
     * Constructor for ButtonPanel.
     *
     * @param layout Layout settings for this ButtonPanel
     * @param buttonBar the buttonBar that created this Panel
     */
    public ButtonPanel(final MigLayout layout, final ButtonBar buttonBar) {
        super(layout);
        this.buttonBar = buttonBar;
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect,
            final int orientation, final int direction) {
        return buttonBar.getButtonHeight();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableBlockIncrement(final Rectangle visibleRect,
            final int orientation, final int direction) {
        return buttonBar.getButtonHeight();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        e.consume();
        final int selectedIndex = getSelectedIndex();
        int newIndex = 0;
        if (e.getWheelRotation() < 0) {
            //Up
            newIndex = selectedIndex > 0 ? selectedIndex - 1
                    : getComponentCount() - 1;
        } else if (e.getWheelRotation() > 0) {
            //Down
            newIndex = (selectedIndex + 1) % getComponentCount();
        }
        ((FrameToggleButton) getComponent(newIndex)).getWindow()
                .activateFrame();
    }

    /**
     * Gets the component index of the button associated with the current
     * selected window.
     *
     * @return Integer Index for the button of the selected window
     *
     * @since 0.6.4
     */
    private int getSelectedIndex() {
        int selectedIndex = 0;
        final FrameToggleButton selectedButton = buttonBar.getSelectedButton();
        for (Component c : getComponents()) {
            if (c == selectedButton) {
                break;
            }
            selectedIndex++;
        }
        return selectedIndex;
    }
}

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

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import net.miginfocom.swing.MigLayout;

/**
 * Implements scrollable onto a JPanel so we have more control over scrolling.
 *
 * @author Simon Mott
 * @since 0.6.4
 */
public class ButtonPanel extends JPanel implements Scrollable {

    /** The ButtonBar that created this Panel. */
    private ButtonBar buttonBar;

    /**
     * Constructor for ButtonPanel.
     *
     * @param layout Layout settings for this ButtonPanel
     * @param buttonBar the buttonBar that created this Panel
     */
    public ButtonPanel(final MigLayout layout, ButtonBar buttonBar) {
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
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return buttonBar.getButtonHeight();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
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

}

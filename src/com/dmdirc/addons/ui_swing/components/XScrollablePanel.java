/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.components;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 *
 */
public class XScrollablePanel extends JPanel implements Scrollable {

    public XScrollablePanel() {
        super();
    }

    public XScrollablePanel(final LayoutManager layout) {
        super(layout);
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableUnitIncrement(final Rectangle visibleRect,
    final int orientation, final int direction) {
        return getFont().getSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableBlockIncrement(final Rectangle visibleRect,
    final int orientation, final int direction) {
        return getFont().getSize() * 3;
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

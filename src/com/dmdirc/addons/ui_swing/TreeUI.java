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

package com.dmdirc.addons.ui_swing;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.TreePath;

/**
 * Metal Tree UI Delegate, with adjustable expand handle control.
 * Use putClientProperty("showHandles", Boolean); to adjust handle visibility.
 */
public class TreeUI extends MetalTreeUI implements PropertyChangeListener {

    /** Show handles? */
    private boolean showHandles = true;

    /**
     * Creates a new UI delegate for the specified component.
     *
     * @param comp Component to create a UI delegate for
     *
     * @return New UI delegate
     */
    public static ComponentUI createUI(final JComponent comp) {
        if (comp instanceof JTree) {
            return new TreeUI((JTree) comp);
        }
        throw new IllegalArgumentException("Component must of of type JTree.");
    }


    /**
     * Creates a new UI Delegate for the specified component.
     *
     * @param component Component to create UI Delegate for
     */
    public TreeUI(final JTree component) {
        super();
        component.addPropertyChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void paintExpandControl(final Graphics g, final Rectangle clipBounds,
            final Insets insets, final Rectangle bounds, final TreePath path,
            final int row, final boolean isExpanded, final boolean hasBeenExpanded,
            final boolean isLeaf) {
        if (showHandles) {
            super.paintExpandControl(g, clipBounds, insets, bounds, path, row,
                    isExpanded, hasBeenExpanded, isLeaf);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if ("showHandles".equals(evt.getPropertyName())) {
            showHandles = (Boolean) evt.getNewValue();
        }
    }
}

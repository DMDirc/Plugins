/*
 * Copyright (c) 2006-2013 DMDirc Developers
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
package com.dmdirc.addons.ui_swing.components.renderers;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import lombok.RequiredArgsConstructor;

/**
 * Simplifies implementing a cell renderer, works around oddities in look and
 * feels.
 */
@RequiredArgsConstructor(callSuper = false)
public abstract class DMDircListCellRenderer implements ListCellRenderer {

    /**
     * A version number for this class.
     */
    private static final long serialVersionUID = 1;
    /**
     * Parent cell renderer.
     */
    private final ListCellRenderer parentRenderer;
    /**
     * Label to use if parent doesn't supply one.
     */
    private JLabel label;

    /**
     * Renders the cell on the given label.
     *
     * @param label Label to render
     * @param value Object to render
     * @param index Index of the cell in the list
     * @param isSelected Is the cell selected
     * @param hasFocus Does the cell have focus
     */
    protected abstract void renderValue(final JLabel label, final Object value,
            final int index, final boolean isSelected, final boolean hasFocus);

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        final Component component = parentRenderer.getListCellRendererComponent(
                list, value, index, isSelected,
                cellHasFocus);
        if (component instanceof JLabel) {
            renderValue((JLabel) component, value, index, isSelected, cellHasFocus);
            return component;
        } else {
            if (label == null) {
                label = new JLabel();
            }
            renderValue(label, value, index, isSelected, cellHasFocus);
            return label;
        }
    }
}

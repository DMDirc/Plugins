/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

/**
 * Font list cell renderer.
 */
public class FontListCellRenderer extends DMDircListCellRenderer<Object> {

    /**
     * Creates a new instance of this renderer.
     *
     * @param renderer Parent renderer
     */
    public FontListCellRenderer(final ListCellRenderer<? super Object> renderer) {
        super(renderer);
    }

    @Override
    protected void renderValue(final JLabel label, final Object value,
            final int index, final boolean isSelected,
            final boolean cellHasFocus) {
        if (value == null) {
            label.setText("Default");
        } else if (value instanceof Font) {
            label.setFont((Font) value);
            label.setText(((Font) value).getFamily());
        } else {
            label.setText(value.toString());
        }
    }

}

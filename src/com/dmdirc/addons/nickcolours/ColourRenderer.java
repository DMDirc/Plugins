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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Provides a colour renderer for JTables.
 */
public class ColourRenderer extends DefaultTableCellRenderer {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The colour manager to use to parse colours. */
    private final ColourManager colourManager;

    /**
     * Creates a new instance of ColourRenderer.
     *
     * @param colourManager The colour manager to use to parse colours.
     */
    public ColourRenderer(final ColourManager colourManager) {
        this.colourManager = colourManager;

        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column) {
        Color colour = null;
        if (value != null && !((String) value).isEmpty()) {
            final Colour temp = colourManager.getColourFromString((String) value, null);
            if (temp != null) {
                colour = UIUtilities.convertColour(temp);
            }
        }

        setHorizontalAlignment(CENTER);

        if (colour == null) {
            setBorder(new LineBorder(Color.GRAY));
            setText("Not Set");
            setBackground(table.getBackground());
        } else {
            setBorder(new LineBorder(Color.BLACK));
            setText("");
            setBackground(colour);
        }

        return this;
    }

}

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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * URI Scheme cell renderer.
 */
public class URIHandlerCellRenderer extends DefaultTableCellRenderer {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;

    @Override
    public Component getTableCellRendererComponent(final JTable table,
            final Object value, final boolean isSelected,
            final boolean hasFocus,
            final int row, final int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);
        if (!(value instanceof String)) {
            setValue(value.toString());
            return this;
        }

        String handler = (String) value;
        switch (handler) {
            case "DMDIRC":
                handler = "Handle internally (irc links only).";
                break;
            case "BROWSER":
                handler = "Use browser (or system registered handler).";
                break;
            case "MAIL":
                handler = "Use mail client.";
                break;
            case "":
                handler = "No handler.";
                break;
            default:
                handler = "Custom command: " + handler;
                break;
        }

        setValue(handler);

        return this;
    }

}

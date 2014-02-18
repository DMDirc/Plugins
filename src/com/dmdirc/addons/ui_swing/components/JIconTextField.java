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

package com.dmdirc.addons.ui_swing.components;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * JTextfield capable of displaying an icon and tooltip.
 */
public class JIconTextField extends JTextField {

    /** Insets used by a normal text field. */
    private final Insets dummyInsets;
    /** Icon to show, or null. */
    private Icon icon;
    /** Message to show, or null. */
    private String message;

    public JIconTextField() {
        super();
        this.icon = null;
        this.dummyInsets = UIManager.getBorder("TextField.border").getBorderInsets(new JTextField());
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public void setIcon(final Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (this.icon != null) {
            final int x = getWidth() - dummyInsets.right - icon.getIconWidth();
            setMargin(new Insets(2, 2, 2, getWidth() - x));
            icon.paintIcon(this, g, x, ((getHeight() - icon.getIconHeight()) / 2));
        } else {
            setMargin(new Insets(2, 2, 2, 2));
        }
    }

    @Override
    public String getToolTipText(final MouseEvent event) {
        if (icon != null && (event.getX() >= getWidth() - dummyInsets.right - dummyInsets.right
                - icon.getIconWidth())) {
            return message;
        }
        return super.getToolTipText(event);
    }

}

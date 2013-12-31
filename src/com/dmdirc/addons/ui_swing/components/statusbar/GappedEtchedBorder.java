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

package com.dmdirc.addons.ui_swing.components.statusbar;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.EtchedBorder;

/**
 * An {@link EtchedBorder} that leaves a gap in the bottom where the
 * status bar popup window is.
 */
public class GappedEtchedBorder extends EtchedBorder {

    /** Java serialisation version ID. */
    private static final long serialVersionUID = 1;
    /** Parent popup window. */
    private transient final StatusbarPopupWindow outer;

    /**
     * Creates a new etched border leaving a gap where the specified window is.
     *
     * @param outer Window to leave a gap for
     */
    protected GappedEtchedBorder(final StatusbarPopupWindow outer) {
        this.outer = outer;
    }

    /** {@inheritDoc} */
    @Override
    public void paintBorder(final Component c, final Graphics g, final int x,
            final int y, final int width, final int height) {
        g.translate(x, y);
        g.setColor(etchType == LOWERED ? getShadowColor(c)
                : getHighlightColor(c));
        g.drawLine(0, 0, width - 1, 0);
        g.drawLine(0, height - 1, outer.getParentPanel().getLocationOnScreen()
                .x - outer.getLocationOnScreen().x, height - 1);
        g.drawLine(outer.getParentPanel().getWidth() + outer.getParentPanel()
                .getLocationOnScreen().x - outer.getLocationOnScreen().x - 2,
                height - 1, width - 1, height - 1);
        g.drawLine(0, 0, 0, height - 1);
        g.drawLine(width - 1, 0, width - 1, height - 1);
        g.translate(-x, -y);
    }
}

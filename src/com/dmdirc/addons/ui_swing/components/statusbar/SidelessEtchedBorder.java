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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.EtchedBorder;

/**
 * An {@link EtchedBorder} with one of it's sides missing.
 */
class SidelessEtchedBorder extends EtchedBorder {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Side to not paint. */
    private final Side side;

    /**
     * Specifies which side to not include.
     */
    public enum Side {
        /** Top border. */
        TOP,
        /** Right hand bortder. */
        RIGHT,
        /** Bottom border. */
        BOTTOM,
        /** Left hand border. */
        LEFT,
        /* Draw all borders. */
        NONE,
    }

    /**
     * Creates a new sideless etched border.
     *
     * @param side Which side do you wish to leave out
     */
    public SidelessEtchedBorder(final Side side) {
        super();
        this.side = side;
    }

    /**
     * Creates a new sideless etched border.
     *
     * @param etchType What etch type to use
     * @param side Which side do you wish to leave out
     */
    public SidelessEtchedBorder(final int etchType, final Side side) {
        super(etchType);
        this.side = side;
    }

    /**
     * Creates a new sideless etched border.
     *
     * @param highlight Highlight colour to use
     * @param shadow Shadow colour to use
     * @param side Which side do you wish to leave out
     */
    public SidelessEtchedBorder(final Color highlight, final Color shadow,
            final Side side) {
        super(highlight, shadow);
        this.side = side;
    }

    /**
     * Creates a new sideless etched border.
     *
     * @param etchType What etch type to use
     * @param highlight Highlight colour to use
     * @param shadow Shadow colour to use
     * @param side Which side do you wish to leave out
     */
    public SidelessEtchedBorder(final int etchType, final Color highlight,
            final Color shadow, final Side side) {
        super(etchType, highlight, shadow);
        this.side = side;
    }

    /** {@inheritDoc} */
    @Override
    public void paintBorder(final Component c, final Graphics g, final int x,
            final int y, final int width, final int height) {
        g.translate(x, y);
        g.setColor(etchType == LOWERED ? getShadowColor(c) : getHighlightColor(c));
        if (!side.equals(Side.TOP)) {
              g.drawLine(0, 0, width - 1, 0);
        }
        if (!side.equals(Side.BOTTOM)) {
            g.drawLine(0, height - 2, width, height - 2);
        }
        if (!side.equals(Side.LEFT)) {
            g.drawLine(0, 0, 0, height - 2);
        }
        if (!side.equals(Side.RIGHT)) {
            g.drawLine(width - 2, 0, width - 2, height - 1);
        }
        g.setColor(Color.WHITE);
        if (!side.equals(Side.BOTTOM)) {
            g.drawLine(0, height - 1, width, height - 1);
        }
        if (!side.equals(Side.RIGHT)) {
            g.drawLine(width - 1, 0, width - 1, height - 1);
        }
        g.translate(-x, -y);
    }
}

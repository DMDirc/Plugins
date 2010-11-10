/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.textpane;

/**
 * A simple class to store a point, coordinates of the point are stored as
 * floats.
 */
public class FloatPoint {
    /** X coordinate. */
    private final float x;
    /** Y coordinate. */
    private final float y;

    /**
     * Creates a new point with the coordinates supplied.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    public FloatPoint(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the X coordinate of this point.
     *
     * @return X coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of this point.
     *
     * @return Y coordinate
     */
    public float getY() {
        return y;
    }
}

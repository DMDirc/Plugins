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

package com.dmdirc.addons.ui_swing.textpane;

import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders a single line in a document to a graphics object.
 */
public interface LineRenderer {

    /**
     * Renders a line to the given graphics object.
     *
     * @param graphics The graphics object to render to.
     * @param canvasWidth The width of the canvas available to render on.
     * @param canvasHeight The height of the canvas available to render on.
     * @param drawPosY The Y position to start rendering at.
     * @param line The number of the line to be rendered.
     * @return The result of the render. Callers should not store the result object, as it may
     * be recycled.
     */
    RenderResult render(final Graphics2D graphics, final float canvasWidth,
            final float canvasHeight, final float drawPosY, final int line);

    /**
     * Describes the results of a rendering attempt.
     *
     * <p>For performance purposes, renderers should create a single instance of this class and
     * recycle it between calls to {@link #render}. Callers should copy the values they require
     * out of the result before calling {@link #render} again.
     */
    class RenderResult {

        /** Map of line information to their rendered rectangles. */
        public final Map<LineInfo, Rectangle2D.Float> drawnAreas = new HashMap<>();

        /** Map of line information to the layout used to render them. */
        public final Map<LineInfo, TextLayout> textLayouts = new HashMap<>();

        /** The total height that was used while rendering, in pixels. */
        public float totalHeight;

        /** The ID of the first visible line that was rendered. */
        public int firstVisibleLine;

    }

}

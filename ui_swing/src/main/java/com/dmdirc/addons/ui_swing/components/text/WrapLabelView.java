/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * @author Stanislav Lapitsky
 * @version 1.0
 */

package com.dmdirc.addons.ui_swing.components.text;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.View;

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
public class WrapLabelView extends LabelView {

    /**
     * Creates a new wrap label view.
     *
     * @param elem Element to view
     */
    public WrapLabelView(final Element elem) {
        super(elem);
    }

    @Override
    public int getBreakWeight(final int axis, final float pos, final float len) {
        if (axis == View.X_AXIS) {
            checkPainter();
            final int p0 = getStartOffset();
            final int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos,
                    len);
            if (p1 == p0) {
                // can't even fit a single character
                return View.BadBreakWeight;
            }
            try {
                //if the view contains line break char return forced break
                if (getDocument().getText(p0, p1 - p0).contains("\r")) {
                    return View.ForcedBreakWeight;
                }
            } catch (BadLocationException ex) {
                //should never happen
            }
        }
        return super.getBreakWeight(axis, pos, len);
    }

    @Override
    public View breakView(final int axis, final int p0, final float pos,
            final float len) {
        if (axis == View.X_AXIS) {
            checkPainter();
            final int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos,
                    len);
            try {
                //if the view contains line break char break the view
                final int index = getDocument().getText(p0, p1 - p0).indexOf('\r');
                if (index >= 0) {
                    return createFragment(p0, p0 + index + 1);
                }
            } catch (BadLocationException ex) {
                //should never happen
            }
        }
        return super.breakView(axis, p0, pos, len);
    }

}

/*
 * @author Stanislav Lapitsky
 * @version 1.0
 */

package com.dmdirc.addons.ui_swing.components.text;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.GlyphView;
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
                final int index = getDocument().getText(p0, p1 - p0)
                        .indexOf("\r");
                if (index >= 0) {
                    return (GlyphView) createFragment(p0, p0 + index + 1);
                }
            } catch (BadLocationException ex) {
                //should never happen
            }
        }
        return super.breakView(axis, p0, pos, len);
    }

}

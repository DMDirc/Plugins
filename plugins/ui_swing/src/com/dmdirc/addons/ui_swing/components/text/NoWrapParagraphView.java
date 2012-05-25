/*
 * @author Stanislav Lapitsky
 * @version 1.0
 */

package com.dmdirc.addons.ui_swing.components.text;

import javax.swing.text.Element;
import javax.swing.text.ParagraphView;

/**
 * @author Stanislav Lapitsky
 * @version 1.0
 */
public class NoWrapParagraphView extends ParagraphView {

    /**
     * Creates a new no wrap paragraph view.
     *
     * @param elem Element to view
     */
    public NoWrapParagraphView(final Element elem) {
        super(elem);
    }

    /** {@inheritDoc} */
    @Override
    public void layout(final int width, final int height) {
        super.layout(Short.MAX_VALUE, height);
    }

    /** {@inheritDoc} */
    @Override
    public float getMinimumSpan(final int axis) {
        return super.getPreferredSpan(axis);
    }
}

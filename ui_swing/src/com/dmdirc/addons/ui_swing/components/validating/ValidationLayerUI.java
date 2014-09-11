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

package com.dmdirc.addons.ui_swing.components.validating;

import com.dmdirc.util.validators.ValidationResponse;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.plaf.LayerUI;

/**
 * Validation UI, paints a translucent red overly on components that fail validation.
 *
 * @param <T> Type of component to validate
 */
public class ValidationLayerUI<T extends Component> extends LayerUI<T> implements
        ComponentValidatorListener {

    /**
     * Serialisation version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Current validation state.
     */
    private boolean isFailure;

    @Override
    public void paint(final Graphics g, final JComponent c) {
        super.paint(g, c);
        if (isFailure) {
            final Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .25f));
            g2.setPaint(Color.red);
            g2.fillRect(0, 0, c.getWidth(), c.getHeight());
            g2.dispose();
        }
    }

    @Override
    public void validationChanged(final ValidationResponse validation) {
        isFailure = validation.isFailure();
    }

}

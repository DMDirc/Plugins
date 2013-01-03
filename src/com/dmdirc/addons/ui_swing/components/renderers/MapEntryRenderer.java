/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.components.renderers;

import java.awt.Component;
import java.util.Map.Entry;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Map entry renderer.
 */
public final class MapEntryRenderer extends DefaultListCellRenderer {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Current list cell renderer. */
    private final ListCellRenderer renderer;
    /** Renderer cast to JLabel. */
    private final JLabel label;

    /**
     * Creates a new map entry renderer. Renders a map entry as its value.
     *
     * @param renderer Renderer
     */
    public MapEntryRenderer(final ListCellRenderer renderer) {
        /*
         * All List cell renderers in Swing are JLabels, as checked by asserts
         * in odd UI delegates.  Check and cast here to access nice convenience
         * methods of a jabel, if not create a jabel and return that as the
         * component.  This is mainly a workaround for look and feel's based on
         * synth and add their own rendering.
         */
        if (renderer instanceof JLabel) {
            this.label = (JLabel) renderer;
        } else {
            this.label = new JLabel();
        }
        this.renderer = renderer;
    }

    /** {@inheritDoc} */
    @Override
    public Component getListCellRendererComponent(final JList list,
            final Object value, final int index, final boolean isSelected,
            final boolean cellHasFocus) {

        renderer.getListCellRendererComponent(
                list, value, index, isSelected,
                cellHasFocus);
        if (value == null) {
            label.setText("Any");
        } else if (value instanceof Entry) {
            label.setText((String) ((Entry<?, ?>) value).getValue());
        } else {
            label.setText(value.toString());
        }

        return label;
    }
}

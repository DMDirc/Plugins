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

package com.dmdirc.addons.ui_swing.components.renderers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.ListCellRenderer;

/**
 * Displays the text from the specified getter as the list cell. If the getter does not exist it
 * will fall back to the toString method.
 *
 * @param <E> the type of values this renderer can be used for
 */
public class MethodListCellRenderer<E> extends DMDircListCellRenderer<E> {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Stores the method to read the displayed text from.
     */
    protected final Method read;

    /**
     * Creates a renderer.
     *
     * @param parentRenderer Parent renderer
     * @param method         Method to call on the renderer value
     */
    public MethodListCellRenderer(final ListCellRenderer<? super E> parentRenderer,
            final Method method) {
        super(parentRenderer);
        this.read = method;
    }

    @Override
    protected void renderValue(final JLabel label, final E value, final int index,
            final boolean isSelected, final boolean hasFocus) {
        String textValue = "";
        try {
            textValue = (String) read.invoke(value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            textValue = value.toString();
        } finally {
            label.setText(textValue);
        }
    }

}

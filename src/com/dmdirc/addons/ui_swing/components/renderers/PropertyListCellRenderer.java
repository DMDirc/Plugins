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

import java.awt.Component;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Displays the text from the specified getter as the list cell. If the getter does not exist it
 * will fall back to the toString method.
 */
public class PropertyListCellRenderer extends DefaultListCellRenderer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Stores the method to read the displayed text from.
     */
    private final Method read;

    /**
     * Creates a renderer.
     *
     * @param type Type of object to be rendered
     * @param property name of the getter to be called on the type
     */
    public PropertyListCellRenderer(final Class<?> type, final String property) {
        final PropertyDescriptor propertyDescriptor;
        Method readMethod;
        try {
            propertyDescriptor = new PropertyDescriptor(property, type);
            readMethod = propertyDescriptor.getReadMethod();
        } catch (IntrospectionException ex) {
            try {
                readMethod = type.getMethod("toString");
            } catch (NoSuchMethodException | SecurityException ex1) {
                throw new RuntimeException("Unable to access toString method", ex);
            }
        }
        read = readMethod;
    }

    @Override
    public Component getListCellRendererComponent(final JList<?> list, final Object value,
            final int index, final boolean isSelected, final boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String textValue = "";
        try {
            textValue = (String) read.invoke(value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            textValue = value.toString();
        } finally {
            setText(textValue);
        }
        return this;
    }

}

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

package com.dmdirc.addons.ui_swing.components;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Table model that display fields from a type of object.
 *
 * @param <T> Type of object to display
 */
public class GenericTableModel<T> extends AbstractTableModel {

    private final List<T> values;
    private final List<String> getters;
    private final Map<String, String> headers;
    private final Class<T> clazz;

    /**
     * Creates a new generic table model.
     *
     * @param type    Type to be displayed, used to verify getters
     * @param getters Names of the getters to display in the table
     */
    public GenericTableModel(Class<T> type, final String... getters) {
        Preconditions.checkArgument(getters.length > 0);
        clazz = type;
        for (String getter : getters) {
            Preconditions.checkArgument(getter.length() > 0);
            Preconditions.checkArgument(getGetter(getter).isPresent());
        }
        values = new ArrayList<>();
        headers = new HashMap<>();
        this.getters = new ArrayList<>();
        this.getters.addAll(Arrays.asList(getters));
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public int getColumnCount() {
        return getters.size();
    }

    @Override
    public String getColumnName(final int column) {
        final String columnName = getters.get(column);
        if (headers.containsKey(columnName)) {
            return headers.get(columnName);
        }
        return columnName;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        try {
            final T value = values.get(rowIndex);
            final Method method = getGetter(getters.get(columnIndex)).get();
            return method.invoke(value);
        } catch (IndexOutOfBoundsException | ReflectiveOperationException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Sets the name of the header for a specific column.
     *
     * @param column Column index
     * @param header Header name
     */
    public void setHeaderName(final int column, final String header) {
        Preconditions.checkNotNull(header);
        Preconditions.checkArgument(column < getters.size());
        headers.put(getters.get(column), header);
        fireTableStructureChanged();
    }

    /**
     * Sets all of the header names. Must include all the headers, otherwise use
     * {@link #setHeaderName(int, String)}.
     *
     * @param headerValues Names for all headers in the table
     */
    public void setHeaderNames(final String... headerValues) {
        Preconditions.checkArgument(headerValues.length == getters.size());
        for (String header : headerValues) {
            headers.put(getters.get(headers.size()), header);
        }
        fireTableStructureChanged();
    }

    /**
     * Adds the specified value at the specified index, the column is ignored.
     *
     * @param value       Value to add
     * @param rowIndex    Index of the row to add
     * @param columnIndex This is ignored
     */
    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        try {
            values.add(rowIndex, (T) value);
            fireTableRowsInserted(rowIndex, rowIndex);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Value of incorrect type.", ex);
        }
    }

    /**
     * Adds the specified value at the end of the model.
     *
     * @param value Value to add
     */
    public void addValue(final T value) {
        values.add(value);
        final int index = values.indexOf(value);
        fireTableRowsInserted(index, index);
    }

    /**
     * Returns an optional method for the specified name.
     *
     * @param name Name of the getter
     *
     * @return Optional method
     */
    private Optional<Method> getGetter(final String name) {
        try {
            return Optional.fromNullable(clazz.getMethod(name));
        } catch (NoSuchMethodException ex) {
            return Optional.absent();
        }
    }

}

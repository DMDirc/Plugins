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
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model that display fields from a type of object.
 *
 * @param <T> Type of object to display
 */
public class GenericTableModel<T> extends AbstractTableModel {

    /**
     * List of values in the model.
     */
    private final List<T> values;
    /**
     * List of getters for each value, used as columns.
     */
    private final String[] getters;
    /**
     * List of header names for each column.
     */
    private final String[] headers;
    /**
     * Generic type this table contains, used to verify getters exist.
     */
    private final Class<T> clazz;

    /**
     * Creates a new generic table model.
     *
     * @param type         Type to be displayed, used to verify getters
     * @param getterValues Names of the getters to display in the table
     */
    public GenericTableModel(final Class<T> type, final String... getterValues) {
        Preconditions.checkArgument(getterValues.length > 0);
        clazz = type;
        for (String getter : getterValues) {
            Preconditions.checkNotNull(getter);
            Preconditions.checkArgument(!getter.isEmpty());
            Preconditions.checkArgument(getGetter(getter).isPresent());
        }
        this.values = new ArrayList<>();
        this.headers = new String[getterValues.length];
        this.getters = new String[getterValues.length];
        for (int i = 0; i < getterValues.length; i++) {
            this.getters[i] = getterValues[i];
            this.headers[i] = getterValues[i];
        }
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public int getColumnCount() {
        return getters.length;
    }

    @Override
    public String getColumnName(final int column) {
        Preconditions.checkElementIndex(column, headers.length);
        return headers[column];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, getters.length);
        try {
            final T value = values.get(rowIndex);
            final Method method = getGetter(getters[columnIndex]).get();
            return method.invoke(value);
        } catch (IndexOutOfBoundsException | ReflectiveOperationException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Returns the value at the specified row.
     *
     * @param rowIndex Index to retrieve
     *
     * @return Value at the specified row
     */
    public T getValue(final int rowIndex) {
        Preconditions.checkElementIndex(rowIndex, values.size());
        return values.get(rowIndex);
    }

    /**
     * Sets the name of the header for a specific column.
     *
     * @param column Column index
     * @param header Header name
     */
    public void setHeaderName(final int column, final String header) {
        Preconditions.checkNotNull(header);
        Preconditions.checkElementIndex(column, getters.length);
        headers[column] = header;
        fireTableStructureChanged();
    }

    /**
     * Sets all of the header names. Must include all the headers, otherwise use
     * {@link #setHeaderName(int, String)}.
     *
     * @param headerValues Names for all headers in the table
     */
    public void setHeaderNames(final String... headerValues) {
        Preconditions.checkArgument(headerValues.length == getters.length);
        System.arraycopy(headerValues, 0, this.headers, 0, headerValues.length);
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

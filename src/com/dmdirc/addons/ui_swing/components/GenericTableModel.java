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
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class GenericTableModel<T> extends AbstractTableModel {

    private final List<T> values;
    private final List<String> getters;
    private final Class<T> clazz;

    public GenericTableModel(Class<T> type, final String... getters) {
        Preconditions.checkArgument(getters.length > 0);
        clazz = type;
        for (String getter : getters) {
            Preconditions.checkArgument(getter.length() > 0);
            Preconditions.checkArgument(getGetter(getter).isPresent());
        }
        values = new ArrayList<>();
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
        String getter = getters.get(column);
        getter = getter.substring(0, 1).toUpperCase() + getter.substring(1);
        return getter;
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

    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        try {
            values.add(rowIndex, (T) value);
            fireTableRowsInserted(rowIndex, rowIndex);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Value of incorrect type.", ex);
        }
    }

    public void addValue(final T value) {
        values.add(value);
        final int index = values.indexOf(value);
        fireTableRowsInserted(index, index);
    }

    private Optional<Method> getGetter(final String name) {
        try {
            String getter = name;
            getter = getter.substring(0, 1).toUpperCase() + getter.substring(1);
            return Optional.fromNullable(clazz.getMethod("get" + getter));
        } catch (NoSuchMethodException ex) {
            return Optional.absent();
        }
    }

}

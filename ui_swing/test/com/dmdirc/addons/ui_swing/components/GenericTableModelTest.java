/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GenericTableModelTest {

    @Mock private TableModelListener listener;
    @Captor private ArgumentCaptor<TableModelEvent> tableModelEvent;
    private List<TestObject> values;
    private TestObject newObject;
    private GenericTableModel<TestObject> instance;
    private String[] headers;

    @Before
    public void setUp() throws Exception {
        values = new ArrayList<>();
        values.add(new TestObject("one", new NotAString("value-one")));
        values.add(new TestObject("two", new NotAString("value-two")));
        values.add(new TestObject("three", new NotAString("value-three")));
        values.add(new TestObject("four", new NotAString("value-four")));
        values.add(new TestObject("five", new NotAString("value-five")));

        newObject = new TestObject("RAR", new NotAString("RAR"));

        headers = new String[]{"getName", "getValue"};

        instance = new GenericTableModel<>(TestObject.class, headers);
        values.forEach(instance::addValue);
        instance.addTableModelListener(listener);
    }

    @Test
    public void testGetRowCount() throws Exception {
        assertEquals(values.size(), instance.getRowCount());
    }

    /// TODO: Row out of bounds

    @Test
    public void testGetColumnCount() throws Exception {
        assertEquals(headers.length, instance.getColumnCount());
    }

    // TODO: Column out of bounds

    @Test
    public void testGetColumnName() throws Exception {
        for (int i = 0; i < headers.length; i++) {
            assertEquals(headers[i], instance.getColumnName(i));
        }
    }

    // TODO: Get out of bounds column names

    @Test
    public void testGetColumnClass() throws Exception {
        assertSame(String.class, instance.getColumnClass(0));
        assertSame(NotAString.class, instance.getColumnClass(1));
    }

    // TODO: Get column out of bounds

    @Test
    public void testGetValueAt() throws Exception {
        for (int i = 0; i < values.size(); i++) {
            assertEquals(values.get(i).getName(), instance.getValueAt(i, 0));
            assertEquals(values.get(i).getValue(), instance.getValueAt(i, 1));
        }
    }

    // TODO: Get row and column out of bounds

    @Test
    public void testIsCellEditable() throws Exception {
        assertFalse(instance.isCellEditable(0, 0));
        assertFalse(instance.isCellEditable(0, 1));
        assertFalse(instance.isCellEditable(1, 0));
        assertFalse(instance.isCellEditable(1, 1));
    }

    // TODO: Test non default isEditable

    @Test
    public void testGetValue() throws Exception {
        for (int i = 0; i < values.size(); i++) {
            assertEquals(values.get(i), instance.getValue(i));
        }
    }

    // TODO: Test out of bounds

    @Test
    public void testGetIndex() throws Exception {
        for (int i = 0; i < values.size(); i++) {
            assertEquals(i, instance.getIndex(values.get(i)));
        }
        assertEquals(-1, instance.getIndex(new TestObject("", new NotAString(""))));
        assertEquals(-1, instance.getIndex(null));
    }

    @Test
    public void testSetHeaderNames() throws Exception {
        for (int i = 0; i < headers.length; i++) {
            assertEquals(headers[i], instance.getColumnName(i));
        }
        final String[] setHeaders = {"name", "value"};
        instance.setHeaderNames(setHeaders);
        for (int i = 0; i < setHeaders.length; i++) {
            assertEquals(setHeaders[i], instance.getColumnName(i));
        }
        // TODO: Check listener fired appropriately
    }

    // TODO: Set longer and shorter than

    @Test
    public void testSetValueAt() throws Exception {
        assertEquals(values.get(0).getName(), instance.getValueAt(0, 0));
        instance.setValueAt(newObject, 0, 0);
        assertEquals(values.get(0).getName(), instance.getValueAt(0, 0));
        verify(listener).tableChanged(tableModelEvent.capture());
        assertEquals(0, tableModelEvent.getValue().getType());
        assertEquals(0, tableModelEvent.getValue().getFirstRow());
        assertEquals(0, tableModelEvent.getValue().getLastRow());
        assertEquals(-1, tableModelEvent.getValue().getColumn());
    }

    // TODO: Test with non default edit function
    // TODO: Test with incorrect value type

    @Test
    public void testReplaceValueAt() throws Exception {
        assertEquals(values.get(0), instance.getValue(0));
        instance.replaceValueAt(newObject, 0);
        assertEquals(newObject, instance.getValue(0));
        // TODO: Check listener fired appropriately
    }

    @Test
    public void testRemoveValue() throws Exception {
        assertEquals(values.size(), instance.getRowCount());
        assertTrue(instance.getIndex(values.get(0)) != -1);
        instance.removeValue(values.get(0));
        assertEquals(values.size() -1, instance.getRowCount());
        assertEquals(-1, instance.getIndex(values.get(0)));
        // TODO: Check listener fired appropriately
    }

    // TODO: Test invalid value

    @Test
    public void testRemoveAll() throws Exception {
        assertEquals(values.size(), instance.getRowCount());
        instance.removeAll();
        // TODO: Check listener fired appropriately
    }

    @Test
    public void testAddValue() throws Exception {
        assertEquals(values.size(), instance.getRowCount());
        assertEquals(-1, instance.getIndex(newObject));
        instance.addValue(newObject);
        assertEquals(values.size() + 1, instance.getRowCount());
        assertEquals(values.size(), instance.getIndex(newObject));
        // TODO: Check listener fired appropriately
    }

    @Test
    public void testElements() throws Exception {
        assertEquals(values, new ArrayList<>(instance.elements()));
    }

    private static class TestObject {
        private String name;
        private NotAString value;

        public TestObject(final String name, final NotAString value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public NotAString getValue() {
            return value;
        }

        public void setValue(final NotAString value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("Name", getName())
                    .add("Value", getValue())
                    .toString();
        }

        @Override
        public boolean equals(final Object object) {
            return object instanceof TestObject &&
                    Objects.equals(getName(), ((TestObject) object).getName())
                    && Objects.equals(getValue(), ((TestObject) object).getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getValue());
        }
    }

    private static class NotAString {
        private final String value;

        public NotAString(final String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("Value", get())
                    .toString();
        }

        @Override
        public boolean equals(final Object object) {
            return object instanceof NotAString
                    && Objects.equals(get(), ((NotAString) object).get());
        }

        @Override
        public int hashCode() {
            return Objects.hash(get());
        }
    }
}
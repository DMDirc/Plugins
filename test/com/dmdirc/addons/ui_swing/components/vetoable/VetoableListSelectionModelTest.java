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

package com.dmdirc.addons.ui_swing.components.vetoable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class VetoableListSelectionModelTest {

    @Mock private VetoableChangeListener vetoListener;
    @Mock private ListSelectionListener selectionListener;

    @Test
    public void testInitialState() {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        assertEquals(instance.getLeadSelectionIndex(), -1);
        assertEquals(instance.getSelectionMode(), SINGLE_SELECTION);
        assertFalse(instance.getValueIsAdjusting());
    }

    @Test
    public void testNonVetoed() {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        instance.setLeadSelectionIndex(5);
        assertEquals(instance.getLeadSelectionIndex(), 5);
    }

    @Test
    public void testVetoedCalled() throws PropertyVetoException {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        instance.addVetoableSelectionListener(vetoListener);
        instance.setLeadSelectionIndex(5);
        assertEquals(instance.getLeadSelectionIndex(), 5);
        verify(vetoListener).vetoableChange(any(PropertyChangeEvent.class));
    }

    @Test
    public void testVetoedCalledAndVetoed() throws PropertyVetoException {
        doThrow(new PropertyVetoException(null, null))
                .when(vetoListener).vetoableChange(any(PropertyChangeEvent.class));
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        instance.addVetoableSelectionListener(vetoListener);
        instance.setLeadSelectionIndex(5);
        assertEquals(instance.getLeadSelectionIndex(), -1);
    }

    @Test
    public void testListenerCalledWithoutVetoListener() {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        instance.addListSelectionListener(selectionListener);
        instance.setLeadSelectionIndex(5);
        assertEquals(instance.getLeadSelectionIndex(), 5);
        verify(selectionListener).valueChanged(any(ListSelectionEvent.class));
    }

    @Test
    public void testListenerCalledWithVetoListenerNoVeto() throws PropertyVetoException {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        instance.addListSelectionListener(selectionListener);
        instance.addVetoableSelectionListener(vetoListener);
        instance.setLeadSelectionIndex(5);
        assertEquals(instance.getLeadSelectionIndex(), 5);
        verify(vetoListener).vetoableChange(any(PropertyChangeEvent.class));
        verify(selectionListener).valueChanged(any(ListSelectionEvent.class));
    }

    @Test
    public void testListenerCalledWithVetoListenerWithVeto() throws PropertyVetoException {
        doThrow(new PropertyVetoException(null, null))
                .when(vetoListener).vetoableChange(any(PropertyChangeEvent.class));
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        instance.addListSelectionListener(selectionListener);
        instance.addVetoableSelectionListener(vetoListener);
        instance.setLeadSelectionIndex(5);
        assertEquals(instance.getLeadSelectionIndex(), -1);
        verify(vetoListener).vetoableChange(any(PropertyChangeEvent.class));
        verify(selectionListener, never()).valueChanged(any(ListSelectionEvent.class));
    }

    @Test
    public void testClearSelection() {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        instance.setLeadSelectionIndex(5);
        assertEquals(instance.getLeadSelectionIndex(), 5);
        instance.clearSelection();
        assertEquals(instance.getLeadSelectionIndex(), -1);
    }

    @Test
    public void testIsSelectionEmpty() {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        assertTrue(instance.isSelectionEmpty());
        instance.setLeadSelectionIndex(5);
        assertFalse(instance.isSelectionEmpty());
    }

    @Test
    public void testIsSelectedIndex() {
        final VetoableListSelectionModel instance = new VetoableListSelectionModel();
        assertTrue(instance.isSelectedIndex(-1));
        assertFalse(instance.isSelectedIndex(5));
        instance.setLeadSelectionIndex(5);
        assertTrue(instance.isSelectedIndex(5));
        assertFalse(instance.isSelectedIndex(-1));
    }

}

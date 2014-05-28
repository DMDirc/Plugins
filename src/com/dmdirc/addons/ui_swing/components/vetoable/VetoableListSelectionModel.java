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

import com.dmdirc.util.collections.ListenerList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Default List selection model, with the ability to veto selection events.
 */
public class VetoableListSelectionModel implements ListSelectionModel {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Listener list. */
    private final ListenerList listeners = new ListenerList();
    /** Selected index. */
    private int selection;
    /** Is the value adjusting? */
    private boolean valueAdjusting;

    /**
     * Adds a vetaoble selection listener to this model.
     *
     * @param l Listener to add
     */
    public void addVetoableSelectionListener(
            final VetoableChangeListener l) {
        listeners.add(VetoableChangeListener.class, l);
    }

    /**
     * Removes a vetoable selection listener from this model.
     *
     * @param l Listener to remove
     */
    public void removeVetoableSelectionListener(
            final VetoableChangeListener l) {
        listeners.remove(VetoableChangeListener.class, l);
    }

    /**
     * Fires a vetoable selection change event.
     *
     * @param event Property change event
     *
     * @return true iif the event is to be vetoed
     */
    protected boolean fireVetoableSelectionChange(final PropertyChangeEvent event) {
        boolean result = false;
        for (VetoableChangeListener listener : listeners.get(VetoableChangeListener.class)) {
            try {
                listener.vetoableChange(event);
            } catch (PropertyVetoException ex) {
                result &= true;
            }
        }
        return result;
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (fireVetoableSelectionChange(
                new PropertyChangeEvent(this, "selection", selection, index0))) {
            return;
        }
        selection = index0;
        listeners.getCallable(ListSelectionListener.class).valueChanged(new ListSelectionEvent(this,
                selection, selection, valueAdjusting));
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
        setSelectionInterval(index0, index1);
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
    }

    @Override
    public int getMinSelectionIndex() {
        return selection;
    }

    @Override
    public int getMaxSelectionIndex() {
        return selection;
    }

    @Override
    public boolean isSelectedIndex(int index) {
        return index == selection;
    }

    @Override
    public int getAnchorSelectionIndex() {
        return selection;
    }

    @Override
    public void setAnchorSelectionIndex(int index) {
        setSelectionInterval(index, index);
    }

    @Override
    public int getLeadSelectionIndex() {
        return selection;
    }

    @Override
    public void setLeadSelectionIndex(int index) {
        setSelectionInterval(index, index);
    }

    @Override
    public void clearSelection() {
        setSelectionInterval(-1, -1);
    }

    @Override
    public boolean isSelectionEmpty() {
        return selection == -1;
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
    }

    @Override
    public void removeIndexInterval(int index0, int index1) {
    }

    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        if (valueAdjusting != valueIsAdjusting) {
            valueAdjusting = valueIsAdjusting;
            listeners.getCallable(ListSelectionListener.class).valueChanged(new ListSelectionEvent(
                    this, selection, selection, valueAdjusting));
        }
    }

    @Override
    public boolean getValueIsAdjusting() {
        return valueAdjusting;
    }

    @Override
    public void setSelectionMode(int selectionMode) {
    }

    @Override
    public int getSelectionMode() {
        return SINGLE_SELECTION;
    }

    @Override
    public void addListSelectionListener(ListSelectionListener l) {
        listeners.add(ListSelectionListener.class, l);
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener l) {
        listeners.remove(ListSelectionListener.class, l);
    }

}

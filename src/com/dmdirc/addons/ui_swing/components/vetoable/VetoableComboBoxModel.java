/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import javax.swing.DefaultComboBoxModel;

/**
 * Vetoable combo box model.
 */
public class VetoableComboBoxModel extends DefaultComboBoxModel {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Listener list. */
    private ListenerList listeners = new ListenerList();

    /**
     * Constructs an empty DefaultComboBoxModel object.
     */
    public VetoableComboBoxModel() {
        super();
    }

    /**
     * Constructs a DefaultComboBoxModel object initialized with
     * an array of objects.
     *
     * @param items  an array of Object objects
     */
    public VetoableComboBoxModel(final Object[] items) {
        super(items);
    }

    /**
     * Adds a vetaoble selection listener to this model.
     *
     * @param l Listener to add
     */
    public void addVetoableSelectionListener(
            final VetoableComboBoxSelectionListener l) {
        listeners.add(VetoableComboBoxSelectionListener.class, l);
    }

    /**
     * Removes a vetoable selection listener from this model.
     *
     * @param l Listener to remove
     */
    public void removeVetoableSelectionListener(
            final VetoableComboBoxSelectionListener l) {
        listeners.remove(VetoableComboBoxSelectionListener.class, l);
    }

    /**
     * Fires a vetoable selection change event.
     *
     * @param newValue New value
     *
     * @return true iif the event is to be vetoed
     */
    protected boolean fireVetoableSelectionChange(final Object newValue) {
        boolean result = true;
        final VetoableChangeEvent event = new VetoableChangeEvent(this, newValue);
        for (VetoableComboBoxSelectionListener listener :
            listeners.get(VetoableComboBoxSelectionListener.class)) {
                result &= listener.selectionChanged(event);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void setSelectedItem(final Object anItem) {
        final Object oldItem = getSelectedItem();

        super.setSelectedItem(anItem);
        if (!fireVetoableSelectionChange(anItem)) {
            super.setSelectedItem(oldItem);
        }
    }
}

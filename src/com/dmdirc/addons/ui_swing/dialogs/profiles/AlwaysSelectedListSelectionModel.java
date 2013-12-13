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
package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.addons.ui_swing.components.GenericListModel;
import com.dmdirc.util.collections.ListenerList;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This list selection model will ensure there is always a selection a single selection if there is
 * data in the model.
 *
 * @param <T> Type of object in the model
 */
public class AlwaysSelectedListSelectionModel<T> implements ListSelectionModel, ListDataListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** List model. */
    private final GenericListModel<T> model;
    /** Selected index. */
    private int selectedIndex;
    /** Listener list. */
    private final ListenerList listeners;

    /**
     * Creates a new selection model.
     *
     * @param model Model to monitor for changes
     */
    public AlwaysSelectedListSelectionModel(final GenericListModel<T> model) {
        this.model = model;
        listeners = new ListenerList();
        if (!model.isEmpty()) {
            selectedIndex = 0;
        }
        model.addListDataListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (model.getSize() == 0) {
            selectedIndex = -1;
            return;
        }
        selectedIndex = index0;
    }

    /** {@inheritDoc} */
    @Override
    public void addSelectionInterval(int index0, int index1) {
        setSelectionInterval(index0, index1);
    }

    /** {@inheritDoc} */
    @Override
    public void removeSelectionInterval(int index0, int index1) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public int getMinSelectionIndex() {
        return selectedIndex;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxSelectionIndex() {
        return selectedIndex;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSelectedIndex(int index) {
        return index == selectedIndex;
    }

    /** {@inheritDoc} */
    @Override
    public int getAnchorSelectionIndex() {
        return selectedIndex;
    }

    /** {@inheritDoc} */
    @Override
    public void setAnchorSelectionIndex(int index) {
        selectedIndex = index;
    }

    /** {@inheritDoc} */
    @Override
    public int getLeadSelectionIndex() {
        return selectedIndex;
    }

    /** {@inheritDoc} */
    @Override
    public void setLeadSelectionIndex(int index) {
        selectedIndex = index;
    }

    /** {@inheritDoc} */
    @Override
    public void clearSelection() {
        if (model.getSize() == 0) {
            selectedIndex = -1;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSelectionEmpty() {
        return selectedIndex == -1;
    }

    /** {@inheritDoc} */
    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void removeIndexInterval(int index0, int index1) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public boolean getValueIsAdjusting() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setSelectionMode(int selectionMode) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public int getSelectionMode() {
        return SINGLE_SELECTION;
    }

    /** {@inheritDoc} */
    @Override
    public void addListSelectionListener(final ListSelectionListener listener) {
        listeners.add(ListSelectionListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeListSelectionListener(ListSelectionListener listener) {
        listeners.remove(ListSelectionListener.class, listener);
    }

    public void fireValueChanged(final ListSelectionEvent e) {
        listeners.getCallable(ListSelectionListener.class).valueChanged(e);
    }

    /** {@inheritDoc} */
    @Override
    public void intervalAdded(ListDataEvent e) {
        if (selectedIndex == -1 && !model.isEmpty()) {
            selectedIndex = 0;
            fireValueChanged(new ListSelectionEvent(e, selectedIndex, selectedIndex, false));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void intervalRemoved(ListDataEvent e) {
        if (model.isEmpty()) {
            selectedIndex = -1;
            fireValueChanged(new ListSelectionEvent(e, selectedIndex, selectedIndex, false));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void contentsChanged(ListDataEvent e) {
        //Ignore
    }
}

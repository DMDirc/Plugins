/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.adapters;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.util.collections.ListObserver;
import com.dmdirc.util.collections.ObservableList;

import javax.swing.table.AbstractTableModel;

/**
 * Adapts an observable list for use as a table model.
 *
 * @param <T> The type of object in the list
 */
public abstract class ObservableListTableModelAdapter<T> extends AbstractTableModel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The list we're observing. */
    protected final ObservableList<T> list;

    /**
     * Creates a new {@link ObservableListTableModelAdapter} backed by the
     * given list.
     *
     * @param list The backing list for this model
     */
    public ObservableListTableModelAdapter(final ObservableList<T> list) {
        this.list = list;
        this.list.addListListener(new Listener());
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return this.list.size();
    }

    /**
     * List observer which fires the relevant methods to notify this model's
     * listeners.
     */
    private class Listener implements ListObserver {

        /** {@inheritDoc} */
        @Override
        public void onItemsAdded(final Object source, final int startIndex,
                final int endIndex) {
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    fireTableRowsInserted(startIndex, endIndex);
                }
            });
        }

        /** {@inheritDoc} */
        @Override
        public void onItemsRemoved(final Object source, final int startIndex,
                final int endIndex) {
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    fireTableRowsDeleted(startIndex, endIndex);
                }
            });
        }

        /** {@inheritDoc} */
        @Override
        public void onItemsChanged(final Object source, final int startIndex,
                final int endIndex) {
            UIUtilities.invokeLater(new Runnable() {

                /** {@inheritDoc} */
                @Override
                public void run() {
                    fireTableRowsUpdated(startIndex, endIndex);
                }
            });
        }
    }

}

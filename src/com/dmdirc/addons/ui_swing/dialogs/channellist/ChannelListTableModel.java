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

package com.dmdirc.addons.ui_swing.dialogs.channellist;

import com.dmdirc.addons.ui_swing.adapters.ObservableListTableModelAdapter;
import com.dmdirc.lists.GroupListEntry;
import com.dmdirc.lists.GroupListManager;

/**
 * Table model for channel list results.
 */
public class ChannelListTableModel extends ObservableListTableModelAdapter<GroupListEntry> {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The names to use for the model's columns. */
    private static final String[] COLUMN_NAMES = new String[] {
        "Name", "Users", "Topic"
    };

    /** The types to use for the model's columns. */
    @SuppressWarnings("rawtypes")
    private static final Class[] COLUMN_TYPES = new Class[] {
        String.class, Integer.class, String.class
    };

    /**
     * Creates a new table model backed by the given manager.
     *
     * @param manager The manager to use to retrieve group list entries.
     */
    public ChannelListTableModel(final GroupListManager manager) {
        super(manager.getGroups());
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final GroupListEntry entry = list.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return entry.getName();
            case 1:
                return entry.getUsers();
            case 2:
                return entry.getTopic();
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return COLUMN_TYPES[columnIndex];
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(final int column) {
        return COLUMN_NAMES[column];
    }

    /**
     * Returns the group list entry at the specified row.
     *
     * @param row Row index
     *
     * @return Group list entry
     */
    public GroupListEntry getGroupListEntry(final int row) {
        return list.get(row);
    }
}

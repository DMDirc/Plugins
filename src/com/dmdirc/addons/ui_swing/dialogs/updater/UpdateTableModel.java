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

package com.dmdirc.addons.ui_swing.dialogs.updater;

import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.UpdateStatus;
import com.dmdirc.updater.manager.UpdateStatusListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Update table model.
 */
public class UpdateTableModel extends AbstractTableModel implements UpdateStatusListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** The update manager to use. */
    private final CachingUpdateManager updateManager;
    /** Data list. */
    private final List<UpdateComponent> updates = new ArrayList<UpdateComponent>();
    /** Enabled list. */
    private final List<UpdateComponent> enabled = new ArrayList<UpdateComponent>();
    /** Cached progress for each component. */
    private final Map<UpdateComponent, Double> progress = new HashMap<UpdateComponent, Double>();
    /** Number formatter. */
    private NumberFormat formatter;

    /** Creates a new instance of UpdateTableModel. */
    public UpdateTableModel() {
        this(UpdateChecker.getManager(), Collections.<UpdateComponent>emptyList());
    }

    /**
     * Creates a new instance of UpdateTableModel.
     *
     * @param updateManager The update manager to use
     * @param updates List of components to display
     */
    public UpdateTableModel(final CachingUpdateManager updateManager,
            final List<UpdateComponent> updates) {
        super();

        this.updateManager = updateManager;
        this.updateManager.addUpdateStatusListener(this);

        setUpdates(updates);
        formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
    }

    /**
     * Sets the updates list.
     *
     * @param updates List of updates
     */
    public void setUpdates(final List<UpdateComponent> updates) {
        this.updates.clear();
        this.updates.addAll(updates);
        this.enabled.clear();
        this.enabled.addAll(updates);
        this.progress.clear();

        fireTableDataChanged();
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return updates.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 4;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Update?";
            case 1:
                return "Component";
            case 2:
                return "New version";
            case 3:
                return "Status";
            default:
                throw new IllegalArgumentException("Unknown column: " +
                        columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return UpdateComponent.class;
            case 2:
                return String.class;
            case 3:
                return UpdateStatus.class;
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 0;
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (updates.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= "
                    + updates.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }

        final UpdateComponent component = updates.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return enabled.contains(component);
            case 1:
                return component;
            case 2:
                return updateManager.getCheckResult(component).getUpdatedVersionName();
            case 3:
                final UpdateStatus status = updateManager.getStatus(component);
                final boolean hasProgress = progress.containsKey(component)
                        && progress.get(component) > 0;

                return status.getDescription() + (hasProgress
                        ? " (" + formatter.format(progress.get(component)) + "%)"
                        : "");
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAt(final Object aValue, final int rowIndex,
            final int columnIndex) {
        if (updates.size() <= rowIndex) {
            throw new IndexOutOfBoundsException(rowIndex + " >= " +
                    updates.size());
        }
        if (rowIndex < 0) {
            throw new IllegalArgumentException("Must specify a positive integer");
        }
        switch (columnIndex) {
            case 0:
                enabled.add(updates.get(rowIndex));
                break;
            default:
                throw new IllegalArgumentException("Unknown column: "
                        + columnIndex);
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /**
     * Gets the update at the specified row.
     *
     * @param rowIndex Row to retrieve
     *
     * @return Specified Update
     */
    public UpdateComponent getUpdate(final int rowIndex) {
        return updates.get(rowIndex);
    }

    /**
     * Gets a list of all updates.
     *
     * @return Update list
     */
    public List<UpdateComponent> getUpdates() {
        return Collections.unmodifiableList(updates);
    }

    /**
     * Is the specified component to be updated?
     *
     * @param update Component to check
     *
     * @return true iif the component needs to be updated
     */
    public boolean isEnabled(final UpdateComponent update) {
        return enabled.contains(update);
    }

    /**
     * Is the component at the specified index to be updated?
     *
     * @param rowIndex Component index to check
     *
     * @return true iif the component needs to be updated
     */
    public boolean isEnabled(final int rowIndex) {
        return isEnabled(updates.get(rowIndex));
    }

    /**
     * Adds an update to the list.
     *
     * @param update Update to add
     */
    public void addRow(final UpdateComponent update) {
        updates.add(update);
        fireTableRowsInserted(updates.indexOf(update), updates.indexOf(update));
    }

    /**
     * Removes a specified row from the list.
     *
     * @param row Row to remove
     */
    public void removeRow(final int row) {
        updates.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * Returns the index of the specified update.
     *
     * @param update Update to get index of
     *
     * @return Index of the update or -1 if not found.
     */
    public int indexOf(final UpdateComponent update) {
        return updates.indexOf(update);
    }

    /** {@inheritDoc} */
    @Override
    public void updateStatusChanged(final UpdateComponent component,
            final UpdateStatus status, final double progress) {
        this.progress.put(component, progress);
        fireTableCellUpdated(updates.indexOf(component), 3);
    }
}


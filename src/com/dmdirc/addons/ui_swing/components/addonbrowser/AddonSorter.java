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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import java.util.Comparator;

import javax.swing.ButtonModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Addon sorter.
 */
public class AddonSorter extends TableRowSorter<DefaultTableModel> implements
        Comparator<AddonInfoLabel> {

    private final ButtonModel sortByDate;
    private final ButtonModel sortByName;
    private final ButtonModel sortByRating;
    private final ButtonModel sortByStatus;

    /**
     * Creates a new addon sorter.
     *
     * @param model Table model to sort
     * @param sortByDate Sort by date
     * @param sortByName Sort by name
     * @param sortByRating Sort by rating
     * @param sortByStatus Sort by status
     * @param filter Addon filter
     */
    public AddonSorter(final DefaultTableModel model, final ButtonModel sortByDate,
            final ButtonModel sortByName, final ButtonModel sortByRating,
            final ButtonModel sortByStatus, final AddonFilter filter) {
        super(model);
        this.sortByDate = sortByDate;
        this.sortByName = sortByName;
        this.sortByRating = sortByRating;
        this.sortByStatus = sortByStatus;

        setRowFilter(filter);
        setComparator(0, this);
        toggleSortOrder(0);
    }

    /** {@inheritDoc} */
    @Override
    public int compare(final AddonInfoLabel o1, final AddonInfoLabel o2) {
        final AddonInfo info1 = o1.getAddonInfo();
        final AddonInfo info2 = o2.getAddonInfo();
        if (sortByDate.isSelected()) {
            return info1.getId() - info2.getId();
        } else if (sortByName.isSelected()) {
            return info1.getTitle().compareTo(info2.getTitle());
        } else if (sortByRating.isSelected()) {
            return info1.getRating() - info2.getRating();
        } else if (sortByStatus.isSelected()) {
            return (info1.isVerified() ? 1 : 0) - (info2.isVerified() ? 1 : 0);
        } else {
            return 0;
        }
    }
}

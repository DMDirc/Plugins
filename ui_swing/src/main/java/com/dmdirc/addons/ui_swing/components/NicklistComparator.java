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

import com.dmdirc.interfaces.GroupChatUser;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares nicklist entries to each other, for sorting purposes.
 */
public final class NicklistComparator implements Comparator<GroupChatUser>, Serializable {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** whether to sort the nicklist by modes. */
    private final boolean sortByMode;
    /** whether to sort the nicklist by case. */
    private final boolean sortByCase;

    /**
     * Creates a new instance of NicklistComparator.
     *
     * @param newSortByMode sorts by channel mode of the user
     * @param newSortByCase sorts by nickname case
     */
    public NicklistComparator(final boolean newSortByMode,
            final boolean newSortByCase) {
        this.sortByMode = newSortByMode;
        this.sortByCase = newSortByCase;
    }

    @Override
    public int compare(final GroupChatUser client1, final GroupChatUser client2) {
        ComparisonChain comparisonChain = ComparisonChain.start();
        if (sortByMode) {
            comparisonChain = comparisonChain.compare(client1.getAllModes(),
                    client2.getAllModes(), client1.getModeComparator());
        }
        if (sortByCase) {
            comparisonChain = comparisonChain.compare(client1.getNickname(), client2.getNickname());
        } else {
            comparisonChain = comparisonChain.compare(client1.getNickname().toLowerCase(),
                    client2.getNickname().toLowerCase());
        }
        return comparisonChain.result();
    }

}

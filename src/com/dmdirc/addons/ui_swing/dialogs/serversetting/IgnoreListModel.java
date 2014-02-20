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

package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.parser.common.IgnoreList;

import javax.swing.AbstractListModel;

/**
 * Shows the list of ignores in string form.
 */
public class IgnoreListModel extends AbstractListModel<String> {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Ignore list backing this model. */
    private final IgnoreList ignoreList;
    /** Are we in simple mode. */
    private boolean isSimple;

    /**
     * Creates a new ignore list based on the specified ignore list.
     *
     * @param ignoreList Ignorelist backing this model
     */
    public IgnoreListModel(final IgnoreList ignoreList) {
        super();

        this.ignoreList = ignoreList;
        isSimple = ignoreList.canConvert();
    }

    @Override
    public int getSize() {
        return ignoreList.count();
    }

    @Override
    public String getElementAt(final int index) {
        if (isSimple) {
            return ignoreList.getSimpleList().get(index);
        } else {
            return ignoreList.getRegexList().get(index);
        }
    }

    /**
     * Sets the is simple flag.
     *
     * @param isSimple Are we in simple mode or not?
     */
    public void setIsSimple(final boolean isSimple) {
        this.isSimple = isSimple;

        fireContentsChanged(this, 0, getSize());
    }

    /**
     * Notifies this model that it has been updated and needs to fire listeners.
     */
    public void notifyUpdated() {
        fireContentsChanged(this, 0, getSize());
    }

}

/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.components;

import javax.swing.ComboBoxModel;

/**
 * Generic combo box model, extends generic list model.
 *
 * @param <T> Model holds this type of object
 */
public class GenericComboBoxModel<T> extends GenericListModel<T> implements ComboBoxModel<T> {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** Selected Object. */
    private T selectedObject;

    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(final Object anItem) {
        if ((selectedObject != null && !selectedObject.equals(anItem))
                || selectedObject == null && anItem != null) {
            selectedObject = (T) anItem;
            fireContentsChanged(this, -1, -1);
        }
    }

    @Override
    public T getSelectedItem() {
        return selectedObject;
    }

}

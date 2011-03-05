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

package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;

import com.dmdirc.actions.ActionGroup;
import com.dmdirc.addons.ui_swing.components.validating.NoDuplicatesInListValidator;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * No duplicates list validator, overriden to work with action groups.
 */
class ActionGroupNoDuplicatesInListValidator extends
        NoDuplicatesInListValidator {

    /**
     * Creates a new validator.
     *
     * @param list List
     * @param model Model to validate
     */
    public ActionGroupNoDuplicatesInListValidator(final JList list,
            final DefaultListModel model) {
        super(true, list, model);
    }

    /**
     * Creates a new validator.
     *
     * @param list List
     * @param caseSensitive Case sensitive check?
     * @param model Model to validate
     */
    public ActionGroupNoDuplicatesInListValidator(final boolean caseSensitive,
            final JList list, final DefaultListModel model) {
        super(caseSensitive, list, model);
    }

    /** {@inheritDoc} */
    @Override
    public String listValueToString(final Object object) {
        return ((ActionGroup) object).getName();
    }

    /** {@inheritDoc} */
    @Override
    public int indexOfString(final String string) {
        final String value = caseSensitive ? string : string.toLowerCase();
        int index = -1;
        for (int i = 0; i < model.getSize(); i++) {
            if (((ActionGroup) model.get(i)).getName().equals(value)) {
                index = i;
                break;
            }
        }
        return index;
    }
}

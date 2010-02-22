/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.config.prefs.validator.ValidationResponse;
import com.dmdirc.config.prefs.validator.Validator;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * Validator to check for duplicate values in a list.
 */
public class NoDuplicatesInListValidator implements Validator<String> {

    /** List. */
    private JList list;
    /** List to validate. */
    private DefaultListModel model;
    /** Case sensitive. */
    private boolean caseSensitive;

    /**
     * Creates a new validator.
     *
     * @param list List
     * @param model Model to validate
     */
    public NoDuplicatesInListValidator(final JList list,
            final DefaultListModel model) {
        this(true, list, model);
    }

    /**
     * Creates a new validator.
     *
     * @param list List
     * @param caseSensitive Case sensitive check?
     * @param model Model to validate
     */
    public NoDuplicatesInListValidator(final boolean caseSensitive,
            final JList list, final DefaultListModel model) {
        this.model = model;
        this.list = list;
        this.caseSensitive = caseSensitive;
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse validate(final String object) {
        final String string = caseSensitive ? object : object.toLowerCase();
        if (model.indexOf(string) != -1 && (list.getSelectedValue() == null ||
                !list.getSelectedValue().equals(string))) {
                return new ValidationResponse("Value is a duplicate");
        } else {
            return new ValidationResponse();
        }
    }
}

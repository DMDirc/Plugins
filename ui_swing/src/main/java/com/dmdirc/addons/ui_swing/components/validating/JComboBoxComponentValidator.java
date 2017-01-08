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

package com.dmdirc.addons.ui_swing.components.validating;

import com.dmdirc.util.validators.Validator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class JComboBoxComponentValidator<T> extends ComponentValidator<List<T>, JComboBox<T>> {

    public JComboBoxComponentValidator(final JComboBox<T> component,
            final Validator<List<T>> validator) {
        super(component, validator);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getValidatable() {
        final int size = getComponent().getModel().getSize();
        final List<T> items = new ArrayList<>(size);
        final ListModel<T> model = getComponent().getModel();
        for (int i = 0; i < size; i++) {
            items.add(model.getElementAt(i));
        }
        return items;
    }

    @Override
    public void addHooks() {
        getComponent().getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(final ListDataEvent e) {
                validate();
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                validate();
            }

            @Override
            public void contentsChanged(final ListDataEvent e) {
                validate();
            }
        });
    }

}

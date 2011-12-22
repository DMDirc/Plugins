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

package com.dmdirc.addons.ui_swing.cinch;

import com.dmdirc.addons.ui_swing.components.validating.ValidatableJTextField;

import com.google.common.collect.ImmutableList;

import com.palantir.ptoss.cinch.core.Binding;
import com.palantir.ptoss.cinch.core.BindingContext;
import com.palantir.ptoss.cinch.core.WiringHarness;
import com.palantir.ptoss.cinch.swing.Bound;
import com.palantir.ptoss.cinch.swing.JTextComponentWiringHarness;
import com.palantir.ptoss.util.Mutator;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.Collection;


/**
 * Simple wiring harness for Validatable JText field.  This implementation
 * proxies the actual wiring off to the JTextComponentWiringHarness of Cinch.
 */
public class ValidatableJTextFieldWiringHarness implements WiringHarness<Bound, Field> {

    /** {@inheritDoc} */
    @Override
    public Collection<? extends Binding> wire(final Bound bound,
            final BindingContext context, final Field field)
            throws IllegalAccessException, IntrospectionException {
        return ImmutableList.of(JTextComponentWiringHarness.bindJTextComponent(
                Mutator.create(context, bound.to()), context.getFieldObject(
                field, ValidatableJTextField.class).getTextField()));
    }
}

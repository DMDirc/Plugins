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

import com.dmdirc.util.validators.Validatable;
import com.dmdirc.util.validators.ValidationResponse;

import com.google.common.collect.Lists;

import com.palantir.ptoss.cinch.core.BindableModel;
import com.palantir.ptoss.cinch.core.Binding;
import com.palantir.ptoss.cinch.core.BindingContext;
import com.palantir.ptoss.cinch.core.BindingException;
import com.palantir.ptoss.cinch.core.BindingWiring;
import com.palantir.ptoss.cinch.core.ModelUpdate;
import com.palantir.ptoss.cinch.core.ObjectFieldMethod;
import com.palantir.ptoss.util.Throwables;

import java.beans.IntrospectionException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Performs validation on a component implementing the Validatable interface.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ValidatesIf {

    /**
     * Method to bind to model.
     *
     * @return Model method to bind
     */
    String to();

    /**
     * Utility class to perform the wiring of the component annotated by
     * this class.
     */
    public static class Wiring implements BindingWiring {

        /** {@inheritDoc} */
        @Override
        public Collection<Binding> wire(final BindingContext context) {
            final List<Field> actions = context.getAnnotatedFields(ValidatesIf.class);
            final List<Binding> bindings = Lists.newArrayList();
            for (final Field field : actions) {
                final ValidatesIf action = field.getAnnotation(ValidatesIf.class);
                final String to = action.to();
                try {
                    bindings.addAll(wire(to, field, context));
                } catch (final Exception e) {
                    throw new BindingException("could not wire up @ValidatesIf on " + field.getName(), e);
                }
            }
            return bindings;
        }

        /**
         * Method to wire the component to be validated with the model.
         *
         * @param to Method to call for validation
         * @param field Field to wire
         * @param context Binding context
         *
         * @return Collection of bindings for the component.
         *
         * @throws SecurityException Thrown on reflection error
         * @throws NoSuchMethodException Thrown on reflection error
         * @throws IllegalArgumentException Thrown on reflection error
         * @throws IntrospectionException  Thrown on reflection error
         */
        private static Collection<Binding> wire(final String to, final Field field, final BindingContext context)
                throws SecurityException, NoSuchMethodException, IllegalArgumentException, IntrospectionException {
            if (!Validatable.class.isAssignableFrom(field.getType())) {
                throw new BindingException("not an instance of validatable: " + field);
            }
            final Method callMethod = field.getType().getMethod("setValidation", ValidationResponse.class);
            if (callMethod == null) {
                throw new BindingException("Unable to find setValidation method on field: " + field);
            }
            final Object setValidationObject = context.getFieldObject(field, Object.class);
            final ObjectFieldMethod getter = context.getBindableModelMethod("is" + Character.toUpperCase(to.charAt(0)) + to.substring(1));
            if (getter == null) {
                throw new BindingException("could not find bindable property: " + to);
            }
            if (getter.getMethod().getReturnType() != ValidationResponse.class) {
                throw new BindingException("ValidatesIf binding must return ValidationResponse: " + to);
            }
            getter.getMethod().setAccessible(true);
            final Binding binding = new Binding() {

                /** {@inheritDoc} */
                @Override
                public <T extends Enum<?> & ModelUpdate> void update(final T... changed) {
                    try {
                        ValidationResponse validation = (ValidationResponse) getter.getMethod().invoke(getter.getObject());
                        callMethod.invoke(setValidationObject, validation);
                    } catch (final Exception e) {
                        Throwables.throwUncheckedException(e);
                    }
                }
            };
            ((BindableModel) getter.getObject()).bind(binding);
            return Collections.singleton(binding);
        }
    }
}
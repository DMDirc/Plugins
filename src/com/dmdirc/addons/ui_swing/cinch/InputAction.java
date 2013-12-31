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

package com.dmdirc.addons.ui_swing.cinch;

import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.util.validators.PermissiveValidator;
import com.dmdirc.util.validators.Validator;

import com.google.common.collect.ImmutableList;

import com.palantir.ptoss.cinch.core.Bindable;
import com.palantir.ptoss.cinch.core.BindableModel;
import com.palantir.ptoss.cinch.core.Binding;
import com.palantir.ptoss.cinch.core.BindingContext;
import com.palantir.ptoss.cinch.core.BindingException;
import com.palantir.ptoss.cinch.core.BindingWiring;
import com.palantir.ptoss.cinch.core.Bindings;
import com.palantir.ptoss.cinch.core.ObjectFieldMethod;
import com.palantir.ptoss.cinch.swing.Action;
import com.palantir.ptoss.util.Throwables;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractButton;

import org.apache.log4j.Logger;

/**
 * This action is essentially copied from
 * {@link com.palantir.ptoss.cinch.swing.Action} except it asks the user for
 * a information before performing the action.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface InputAction {

    /**
     * The name of the method to call when the action occurs. Must be accessible
     * in the
     * {@link BindingContext}.
     *
     * @return Method to call when action occurs.
     */
    String call();

    /**
     * Message to be displayed in the confirmation dialog.
     *
     * @return Message
     */
    String message() default "Fill in the following:";

    /**
     * Class name of the validator to use in the input dialog.
     *
     * @return Validator class name
     */
    Class<?> validator() default PermissiveValidator.class;

    /**
     * The name of the method to call to get the display text.
     *
     * @return method to call to get display text
     */
    String content() default "";

    /**
     * Inner utility class used to wire {@link Action} bindings.
     */
    static class Wiring implements BindingWiring {

        private static final Logger LOGGER = Logger.getLogger(InputAction.class);

        /**
         * Wires all {@link Action} bindings in the passed context.
         * Called by {@link Bindings#createBindings(BindingContext)} as part of
         * runtime wiring process.
         *
         * @param context Binding context
         *
         * @return Collection of bindings for this wiring
         */
        @Override
        public Collection<Binding> wire(final BindingContext context) {
            final List<Field> actions = context.getAnnotatedFields(InputAction.class);
            for (final Field field : actions) {
                final InputAction action = field.getAnnotation(InputAction.class);
                try {
                    wire(action.call(), action.message(), action.content(),
                            action.validator(), field, context);
                } catch (ReflectiveOperationException e) {
                    Throwables.throwUncheckedException(e);
                    throw new BindingException("could not wire up "
                            + "@InputAction on " + field.getName(), e);
                }
            }
            return ImmutableList.of();
        }

        /**
         * Wires up to any object with an addActionListener method.
         * Automatically called by {@link #wire(BindingContext)}.
         *
         * @param call name of an {@link ObjectFieldMethod} in the passed {@link BindingContext}.
         * @param message Message to be displayed to the user
         * @param field field to bind the call to.
         * @param context the {@link BindingContext}
         */
        @SuppressWarnings("unchecked")
        private static void wire(final String call, final String message,
                final String existing, final Class<?> validator,
                final Field field, final BindingContext context)
                throws SecurityException, NoSuchMethodException,
                IllegalArgumentException, IllegalAccessException,
                InvocationTargetException, ClassNotFoundException,
                InstantiationException {
            final Method aalMethod = field.getType().getMethod(
                    "addActionListener", ActionListener.class);
            final Object actionObject = context.getFieldObject(
                    field, Object.class);

            final List<Field> fields = context.getAnnotatedFields(
                    Bindable.class);
            final Object object = context.getFieldObject(fields.get(0),
                    Object.class);
            final Method method = fields.get(0).getType().getMethod(call,
                    String.class);

            final ObjectFieldMethod existingMethod;
            if (!existing.isEmpty()) {
                existingMethod = context.getBindableMethod(existing);
                if (existingMethod == null) {
                    throw new BindingException("could not find bindable "
                            + "method: " + existing);
                }
            } else {
                existingMethod = null;
            }

            final ObjectFieldMethod ofm = new ObjectFieldMethod(object,
                    fields.get(0), method);
            if (ofm == null) {
                throw new BindingException("could not find bindable method: "
                        + call);
            }

            final Validator<String> validatorInstance;
            final Constructor<?> ctor = validator.getConstructor(
                    BindableModel.class);
            validatorInstance = (Validator<String>) ctor.newInstance(
                    context.getBindableModels().toArray()[0]);
            aalMethod.invoke(actionObject, new ActionListener() {

                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e) {
                    new StandardInputDialog(null,
                            (Window) ((AbstractButton) actionObject)
                            .getTopLevelAncestor(), ModalityType.DOCUMENT_MODAL,
                            "Input", message, validatorInstance) {

                        /**
                         * Serial Version UID.
                         */
                        private static final long serialVersionUID = 1;

                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public boolean save() {
                            try {
                                final boolean accessible = ofm.getMethod()
                                        .isAccessible();
                                ofm.getMethod().setAccessible(true);
                                ofm.getMethod().invoke(ofm.getObject(), getText());
                                ofm.getMethod().setAccessible(accessible);
                            } catch (final InvocationTargetException itex) {
                                LOGGER.error("exception during action firing",
                                        itex.getCause());
                            } catch (ReflectiveOperationException ex) {
                                LOGGER.error("exception during action firing", ex);
                            }
                            return true;
                        }

                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void display() {
                            String content;
                            if (existingMethod != null) {
                                try {
                                    final boolean accessible = existingMethod
                                            .getMethod().isAccessible();
                                    existingMethod.getMethod().setAccessible(true);
                                    content = (String) existingMethod.getMethod()
                                            .invoke(existingMethod.getObject());
                                    existingMethod.getMethod()
                                            .setAccessible(accessible);
                                } catch (ReflectiveOperationException e) {
                                    content = "";
                                }
                            } else {
                                content = "";
                            }
                            setText(content);
                            super.display();
                        }

                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void cancelled() {
                            //Ignore
                        }
                    }.display();
                }
            });
        }
    }
}

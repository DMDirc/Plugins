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

import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;

import com.google.common.collect.ImmutableList;

import com.palantir.ptoss.cinch.core.Binding;
import com.palantir.ptoss.cinch.core.BindingContext;
import com.palantir.ptoss.cinch.core.BindingException;
import com.palantir.ptoss.cinch.core.BindingWiring;
import com.palantir.ptoss.cinch.core.Bindings;
import com.palantir.ptoss.cinch.core.ObjectFieldMethod;
import com.palantir.ptoss.cinch.swing.Action;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractButton;

import org.apache.log4j.Logger;

/**
 * This action is essentially copied from
 * {@link com.palantir.ptoss.cinch.swing.Action} it adds a
 * confirmation dialog before performing the action.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ConfirmAction {

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
    String message() default "Are you sure you want to do this?";

    /**
     * Inner utility class used to wire {@link Action} bindings.
     */
    static class Wiring implements BindingWiring {

        private static final Logger LOGGER = Logger.getLogger(
                ConfirmAction.class);

        /**
         * Wires all {@link Action} bindings in the passed context.
         * Called by {@link Bindings#createBindings(BindingContext)} as part
         * of runtime wiring process.
         *
         * @param context Binding context
         *
         * @return Collection of bindings for this wiring
         */
        @Override
        public Collection<Binding> wire(final BindingContext context) {
            final List<Field> actions = context.getAnnotatedFields(
                    ConfirmAction.class);
            for (Field field : actions) {
                final ConfirmAction action = field.getAnnotation(
                        ConfirmAction.class);
                try {
                    wire(action.call(), action.message(), field, context);
                } catch (Exception e) {
                    throw new BindingException("could not wire up "
                            + "@ConfirmAction on " + field.getName(), e);
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
        private static void wire(final String call, final String message,
                final Field field, final BindingContext context)
                throws SecurityException, NoSuchMethodException,
                IllegalArgumentException, IllegalAccessException,
                InvocationTargetException {
            final Method aalMethod = field.getType().getMethod(
                    "addActionListener", ActionListener.class);
            final Object actionObject = context.getFieldObject(
                    field, Object.class);
            final ObjectFieldMethod ofm = context.getBindableMethod(call);
            if (ofm == null) {
                throw new BindingException("could not find bindable method: "
                        + call);
            }
            aalMethod.invoke(actionObject, new ActionListener() {

                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e) {
                    new StandardQuestionDialog(
                            (Window) ((AbstractButton) actionObject)
                            .getTopLevelAncestor(), ModalityType.DOCUMENT_MODAL,
                            "Confirmaton", message) {

                        /** Serial Version UID. */
                        private static final long serialVersionUID = 1;

                        /** {@inheritDoc} */
                        @Override
                        public boolean save() {
                            try {
                                boolean accessible = ofm.getMethod().isAccessible();
                                ofm.getMethod().setAccessible(true);
                                ofm.getMethod().invoke(ofm.getObject());
                                ofm.getMethod().setAccessible(accessible);
                            } catch (InvocationTargetException itex) {
                                LOGGER.error("exception during action firing",
                                        itex.getCause());
                            } catch (Exception ex) {
                                LOGGER.error("exception during action firing", ex);
                            }
                            return true;
                        }

                        /** {@inheritDoc} */
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

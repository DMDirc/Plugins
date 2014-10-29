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

package com.dmdirc.addons.ui_swing.components.validating;

import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

import com.google.common.base.Preconditions;

import javax.swing.JComponent;

/**
 * Validates UI components and triggers listeners appropriately.
 *
 * @param <T> Object type to be validated
 * @param <V> Type of the component to be validated
 */
public abstract class ComponentValidator<T, V extends JComponent> {

    /**
     * List of listeners.
     */
    private final ListenerList listeners;
    /**
     * Component to validate.
     */
    private final V component;
    /**
     * Validator to validate against.
     */
    private final Validator<T> validator;
    /**
     * Current validation state.
     */
    private boolean isFailure;
    /**
     * Creates a new component validator.
     *
     * @param component The component to validate
     * @param validator Validator to validate against
     */
    public ComponentValidator(final V component, final Validator<T> validator) {
        Preconditions.checkNotNull(component, "Component cannot be null");
        Preconditions.checkNotNull(validator, "Validator cannot be null");
        this.listeners = new ListenerList();
        this.validator = validator;
        this.component = component;
        component.addPropertyChangeListener("enabled", e -> validate());
    }

    /**
     * Returns the object to validate for this component.
     *
     * @return Object to validate
     */
    public abstract T getValidatable();

    /**
     * Adds all required hooks needed to validate component.
     */
    public abstract void addHooks();

    /**
     * Gets the component that is being validated.
     *
     * @return Component Component to be validated
     */
    protected V getComponent() {
        return component;
    }

    /**
     * Validates this component.
     *
     * @return Result of the validation
     */
    public ValidationResponse validate() {
        final ValidationResponse validation;
        if (component.isEnabled()) {
            validation = validator.validate(getValidatable());
        } else {
            validation = new ValidationResponse();
        }
        if (validation.isFailure() != isFailure) {
            listeners.getCallable(ComponentValidatorListener.class).validationChanged(validation);
        }
        isFailure = validation.isFailure();
        return validation;
    }

    /**
     * Adds a listener to this validator.
     *
     * @param listener Listener to add
     */
    public void addComponentValidatorListener(final ComponentValidatorListener listener) {
        listeners.add(ComponentValidatorListener.class, listener);
    }

    /**
     * Removes a listener to this validator.
     *
     * @param listener Listener to remove
     */
    public void removeComponentValidatorListener(final ComponentValidatorListener listener) {
        listeners.add(ComponentValidatorListener.class, listener);
    }

}

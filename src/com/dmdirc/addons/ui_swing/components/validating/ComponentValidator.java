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

/**
 * Validates UI components and triggers listeners appropriately.
 */
public abstract class ComponentValidator {

    /**
     * List of listeners.
     */
    private final ListenerList listeners;
    /**
     * Validator to validate against.
     */
    private final Validator<String> validator;
    /**
     * Current validation state.
     */
    private boolean isFailure;

    /**
     * Creates a new component validator.
     *
     * @param validator Validator to validate against
     */
    public ComponentValidator(final Validator<String> validator) {
        this.listeners = new ListenerList();
        this.validator = validator;
    }

    /**
     * Returns the string to validate for this component.
     *
     * @return String to validate
     */
    public abstract String getValidatable();

    /**
     * Adds all required hooks needed to validate component.
     */
    public abstract void addHooks();

    /**
     * Validates this component.
     *
     * @return Result of the validation
     */
    public ValidationResponse validate() {
        final ValidationResponse validation = validator.validate(getValidatable());
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

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

package com.dmdirc.addons.ui_swing.components.vetoable;

import javax.swing.event.ChangeEvent;

/**
 * Vetoable change event.
 */
public class VetoableChangeEvent extends ChangeEvent {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Old value. */
    private final Object oldValue;
    /** New value. */
    private final Object newValue;

    /**
     * Creates a new vetoable change event.
     *
     * @param source   Event source
     * @param oldValue Old value
     * @param newValue New value
     */
    public VetoableChangeEvent(final Object source, final Object oldValue, final Object newValue) {
        super(source);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the old value for the event.
     *
     * @return Old value
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Returns the new value for the event.
     *
     * @return New value
     */
    public Object getNewValue() {
        return newValue;
    }

}

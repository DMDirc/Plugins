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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.util.ListenerList;

import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Simple bounded range model allowing value to be anything inside min and max.
 */
public class TextPaneBoundedRangeModel implements BoundedRangeModel {

    /** State change listeners. */
    private final ListenerList listeners = new ListenerList();
    /** Maximum value allowed in the range. */
    private int maximum = 0;
    /** Minimum value allowed in the range. */
    private int minimum = 0;
    /** Value inside the range. */
    private int value = 0;

    /** {@inheritDoc} */
    @Override
    public int getMinimum() {
        return minimum;
    }

    /** {@inheritDoc} */
    @Override
    public void setMinimum(final int newMinimum) {
        setRangeProperties(value, 0, newMinimum, maximum, false);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaximum() {
        return maximum;
    }

    /** {@inheritDoc} */
    @Override
    public void setMaximum(final int newMaximum) {
        setRangeProperties(value, 0, minimum, newMaximum, false);
    }

    /** {@inheritDoc} */
    @Override
    public int getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(final int newValue) {
        setRangeProperties(newValue, 0, minimum, maximum, false);
    }

    /** {@inheritDoc} */
    @Override
    public void setValueIsAdjusting(final boolean b) {
        setRangeProperties(value, 0, minimum, maximum, false);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getValueIsAdjusting() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int getExtent() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void setExtent(final int newExtent) {
        setRangeProperties(value, 0, minimum, maximum, false);
    }

    /** {@inheritDoc} */
    @Override
    public void setRangeProperties(final int value, final int extent,
            final int min, final int max, final boolean adjusting) {
        if (value > max) {
            this.value = max;
        } else if (value < min) {
            this.value = min;
        } else {
            this.value = value;
        }

        if (min < 0) {
            this.minimum = 0;
        } else if (min > max) {
            this.minimum = max;
        } else {
            this.minimum = min;
        }

        if (max < min) {
            this.maximum = min;
        } else {
            this.maximum = max;
        }

        for (ChangeListener listener : listeners.get(ChangeListener.class)) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addChangeListener(final ChangeListener x) {
        listeners.add(ChangeListener.class, x);
    }

    /** {@inheritDoc} */
    @Override
    public void removeChangeListener(final ChangeListener x) {
        listeners.remove(ChangeListener.class, x);
    }

}

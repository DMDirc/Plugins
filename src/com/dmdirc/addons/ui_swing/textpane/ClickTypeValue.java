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

package com.dmdirc.addons.ui_swing.textpane;

/**
 * Returns a click type and associated value.
 */
public class ClickTypeValue {

    private final ClickType clickType;
    private final String value;

    /**
     * Instantiates a new click type with the specified type and value.
     *
     * @param clickType Click type
     * @param value     Value for click (can be empty but not null)
     */
    public ClickTypeValue(final ClickType clickType, final String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        this.clickType = clickType;
        this.value = value;
    }

    /**
     * Returns the type for this click type.
     *
     * @return CLick type
     */
    public ClickType getType() {
        return clickType;
    }

    /**
     * Returns the value for this click.
     *
     * @return Click value (Can be empty)
     */
    public String getValue() {
        return value;
    }

}

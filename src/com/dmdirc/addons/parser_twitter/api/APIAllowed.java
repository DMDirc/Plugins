/*
 *  Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.dmdirc.addons.parser_twitter.api;

/**
 *
 * @author shane
 */
public enum APIAllowed {
    /** It is not known if we are allowed or not. */
    UNKNOWN(false),
    /** We are not allowed. */
    FALSE(false),
    /** We are allowed. */
    TRUE(true);

    /** Boolean value of this APIAllowed */
    final boolean value;

    /**
     * Create an APIAllowed
     *
     * @param booleanValue boolean value for this if needed.
     */
    private APIAllowed(final boolean booleanValue) {
        value = booleanValue;
    }

    /**
     * Get the boolean value of this object.
     *
     * @return the boolean value of this object.
     */
    public boolean getBooleanValue() {
        return value;
    }
}

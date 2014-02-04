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

package com.dmdirc.addons.ui_swing;

import com.google.common.base.Preconditions;

import javax.swing.SwingUtilities;

/**
 * Swing-specific preconditions.
 */
public final class SwingPreconditions {

    private SwingPreconditions() {
        // Shouldn't be instansiated.
    }

    /**
     * Checks that the method is called on the Swing EDT.
     *
     * @throw IllegalStateException if the method is called from another thread.
     */
    public static void checkOnEDT() {
        Preconditions.checkState(SwingUtilities.isEventDispatchThread(),
                "Must be called on the event despatch thread");
    }

    /**
     * Checks that the method is NOT called on the Swing EDT.
     *
     * @throw IllegalStateException if the method is called from the EDT.
     */
    public static void checkNotOnEDT() {
        Preconditions.checkState(!SwingUtilities.isEventDispatchThread(),
                "Must not be called ont he event despatch thread");
    }

}

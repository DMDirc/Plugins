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

package com.dmdirc.addons.ui_swing.framemanager.windowmenu;

import com.dmdirc.FrameContainerComparator;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;

import java.io.Serializable;
import java.util.Comparator;

import javax.swing.JInternalFrame;

/**
 * A comparator that proxies JInternalFrame arrays to a Frame container
 * comparator if appropriate.
 */
public class JInternalFrameComparator implements Comparator<JInternalFrame>,
        Serializable {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Comparator to proxy compares to. */
    private final FrameContainerComparator comparator =
            new FrameContainerComparator();

    /**
     * Compares two frame containers names.
     *
     * @param item1 The first container to compare
     * @param item2 The second container to compare
     * @return -1 if item1 is before item2, 0 if they're equal,
     * +1 if item1 is after item2.
     */
    @Override
    public int compare(final JInternalFrame item1, final JInternalFrame item2) {
        if (!(item1 instanceof TextFrame) || (!(item2 instanceof TextFrame))) {
            return 0;
        }
        return comparator.compare(((TextFrame) item1).getContainer(),
                ((TextFrame) item2).getContainer());
    }
}

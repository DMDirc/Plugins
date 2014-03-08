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

package com.dmdirc.addons.ui_swing.interfaces;

import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;

/**
 * Interface to the management of the active window in the swing ui.
 */
public interface ActiveWindowManager {

    /**
     * Returns the window that is currently active.
     *
     * @return The active window
     */
    public TextFrame getActiveFrame();

    /**
     * Changes the visible frame.
     *
     * @param activeFrame The frame to be activated, or null to show none
     */
    public void setActiveFrame(final TextFrame activeFrame);

    /**
     * Registers a new selection listener with this frame. The listener will be notified whenever
     * the currently selected frame is changed.
     *
     * @param listener The listener to be added
     *
     * @see #setActiveFrame(com.dmdirc.addons.ui_swing.components.frames.TextFrame)
     * @see #getActiveFrame()
     */
    public void addSelectionListener(final SelectionListener listener);

    /**
     * Removes a previously registered selection listener.
     *
     * @param listener The listener to be removed
     *
     * @see #addSelectionListener(com.dmdirc.addons.ui_swing.SelectionListener)
     */
    public void removeSelectionListener(final SelectionListener listener);

}

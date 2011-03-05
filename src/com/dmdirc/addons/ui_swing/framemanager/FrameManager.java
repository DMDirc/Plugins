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

package com.dmdirc.addons.ui_swing.framemanager;

import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowListener;

import javax.swing.JComponent;

/**
 * A frame manager is a widget that allows the user to navigate between the
 * various frames that will be open at any one time.
 */
public interface FrameManager extends SwingWindowListener, SelectionListener {

    /**
     * Sets the parent component of this frame manager. The frame manager
     * should render itself within the parent.
     *
     * @param parent The parent control
     */
    void setParent(JComponent parent);

    /**
     * Sets the UIController for the frame manager.  This method will be called
     * immediately after construction of the frame manager.
     *
     * @param controller UIController
     */
    void setController(SwingController controller);

    /**
     * Indicates whether this frame manager can be positioned vertically
     * (i.e., at the side of the screen).
     *
     * @return True iff the frame manager can be positioned vertically
     */
    boolean canPositionVertically();

    /**
     * Indicates whether this frame manager can be positioned horizontally
     * (i.e., at the top or bottom of the screen).
     *
     * @return True iff the frame manager can be positioned horizontally
     */
    boolean canPositionHorizontally();

}

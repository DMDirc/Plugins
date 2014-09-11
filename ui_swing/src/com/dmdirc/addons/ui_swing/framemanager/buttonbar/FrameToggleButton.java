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

package com.dmdirc.addons.ui_swing.framemanager.buttonbar;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;

import javax.swing.Icon;
import javax.swing.JToggleButton;


/**
 * Custom toggle button that contains Window information for this button.
 */
public class FrameToggleButton extends JToggleButton {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Contains the window associated with this button. */
    private final TextFrame textFrame;
    /** The frame container associated with this button. */
    private final FrameContainer frameContainer;

    /**
     * Create a new instance of FrameToggleButton.
     *
     * @param text           Text to show
     * @param icon           Icon to show
     * @param textFrame      Text frame
     * @param frameContainer Associated frame container
     */
    public FrameToggleButton(final String text, final Icon icon, final TextFrame textFrame,
            final FrameContainer frameContainer) {
        super(text, icon);
        this.textFrame = textFrame;
        this.frameContainer = frameContainer;
    }

    /**
     * Returns the window associated with this button.
     *
     * @return Window associated with this button
     */
    public TextFrame getTextFrame() {
        return textFrame;
    }

    /**
     * Returns the FrameContainer associated with this button.
     *
     * @return FrameContainer associated with this button
     */
    public FrameContainer getFrameContainer() {
        return frameContainer;
    }

}

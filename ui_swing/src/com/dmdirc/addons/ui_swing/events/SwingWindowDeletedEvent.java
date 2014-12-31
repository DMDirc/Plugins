/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.events;

import com.dmdirc.addons.ui_swing.components.frames.TextFrame;

import java.util.Optional;

/**
 * Event triggered when a swing window is deleted.
 */
public class SwingWindowDeletedEvent extends SwingEvent {

    private final Optional<TextFrame> parentWindow;
    private final TextFrame childWindow;

    public SwingWindowDeletedEvent(final Optional<TextFrame> parentWindow,
            final TextFrame childWindow) {
        this.parentWindow = parentWindow;
        this.childWindow = childWindow;
    }

    public Optional<TextFrame> getParentWindow() {
        return parentWindow;
    }

    public TextFrame getChildWindow() {
        return childWindow;
    }
}

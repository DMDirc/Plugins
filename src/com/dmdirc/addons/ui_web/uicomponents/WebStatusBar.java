/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_web.uicomponents;

import com.dmdirc.addons.ui_web.DynamicRequestHandler;
import com.dmdirc.addons.ui_web.Event;
import com.dmdirc.interfaces.ui.StatusBar;
import com.dmdirc.interfaces.ui.StatusBarComponent;
import com.dmdirc.ui.StatusMessage;

import lombok.RequiredArgsConstructor;

/**
 * A status bar handler for the web UI.
 */
@RequiredArgsConstructor
public class WebStatusBar implements StatusBar {

    /** The request handler to pass global events to. */
    private final DynamicRequestHandler handler;

    /** {@inheritDoc} */
    @Override
    public void clearMessage() {
        handler.addEvent(new Event("statusbar", "Ready"));
    }

    /** {@inheritDoc} */
    @Override
    public void addComponent(final StatusBarComponent component) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void removeComponent(final StatusBarComponent component) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(final StatusMessage message) {
        handler.addEvent(new Event("statusbar", message.getMessage()));
    }

}

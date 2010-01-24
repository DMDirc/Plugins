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

package com.dmdirc.addons.ui_web.uicomponents;

import com.dmdirc.ui.interfaces.StatusBar;
import com.dmdirc.ui.interfaces.StatusBarComponent;
import com.dmdirc.ui.interfaces.StatusMessageNotifier;
import com.dmdirc.addons.ui_web.DynamicRequestHandler;
import com.dmdirc.addons.ui_web.Event;

/**
 *
 * @author chris
 */
public class WebStatusBar implements StatusBar {

    /** {@inheritDoc} */
    @Override
    public void setMessage(final String newMessage) {
        DynamicRequestHandler.addEvent(new Event("statusbar", newMessage));
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(String newMessage, StatusMessageNotifier newNotifier) {
        DynamicRequestHandler.addEvent(new Event("statusbar", newMessage));
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(String newMessage, StatusMessageNotifier newNotifier,
                           int timeout) {
        DynamicRequestHandler.addEvent(new Event("statusbar", newMessage));
    }

    /** {@inheritDoc} */
    @Override
    public void clearMessage() {
        DynamicRequestHandler.addEvent(new Event("statusbar", "Ready"));
    }

    /** {@inheritDoc} */
    @Override
    public void addComponent(StatusBarComponent component) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void removeComponent(StatusBarComponent component) {
        // Do nothing
    }

    public boolean isVisible() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(String iconType, String newMessage) {
        DynamicRequestHandler.addEvent(new Event("statusbar", newMessage));
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(String iconType, String newMessage,
            StatusMessageNotifier newNotifier) {
        DynamicRequestHandler.addEvent(new Event("statusbar", newMessage));
    }

    /** {@inheritDoc} */
    @Override
    public void setMessage(String iconType, String newMessage,
            StatusMessageNotifier newNotifier, int timeout) {
        DynamicRequestHandler.addEvent(new Event("statusbar", newMessage));
    }

}

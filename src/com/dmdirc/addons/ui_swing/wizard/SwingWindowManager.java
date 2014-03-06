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

package com.dmdirc.addons.ui_swing.wizard;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Manages all swing windows.
 */
public class SwingWindowManager {

    /** Top level window list. */
    private final List<java.awt.Window> windows;

    @Inject
    public SwingWindowManager(final EventBus eventBus) {
        windows = new ArrayList<>();
        eventBus.register(this);
    }

    @Subscribe
    public void handleWindowEvent(final WindowEvent event) {
        if ((event.getSource() instanceof Window)) {
            if (event.getID() == WindowEvent.WINDOW_OPENED) {
                addTopLevelWindow((Window) event.getSource());
            } else if (event.getID() == WindowEvent.WINDOW_CLOSED) {
                delTopLevelWindow((Window) event.getSource());
            }
        }
    }

    /**
     * Adds a top level window to the window list.
     *
     * @param source New window
     */
    private void addTopLevelWindow(final java.awt.Window source) {
        synchronized (windows) {
            windows.add(source);
        }
    }

    /**
     * Deletes a top level window to the window list.
     *
     * @param source Old window
     */
    private void delTopLevelWindow(final java.awt.Window source) {
        synchronized (windows) {
            windows.remove(source);
        }
    }

    /**
     * Returns a list of top level windows.
     *
     * @return Top level window list
     */
    public List<java.awt.Window> getTopLevelWindows() {
        synchronized (windows) {
            return new ArrayList<>(windows);
        }
    }

}

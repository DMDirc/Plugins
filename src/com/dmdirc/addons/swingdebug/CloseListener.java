/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.swingdebug;

import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ButtonModel;

/**
 * Updates checkbox state on window close.
 */
public class CloseListener implements ComponentListener {

    /** Button model to update. */
    private final ButtonModel model;

    /**
     * Creates a new Close Listener for the specified window and model.
     *
     * @param model Model to keep updated
     * @param window Window to monitor
     */
    public CloseListener(final ButtonModel model, final Window window) {
        super();

        this.model = model;
        window.addComponentListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void componentResized(final ComponentEvent e) {
        //Ignore, don't care
    }

    /** {@inheritDoc} */
    @Override
    public void componentMoved(final ComponentEvent e) {
        //Ignore, don't care
    }

    /** {@inheritDoc} */
    @Override
    public void componentShown(final ComponentEvent e) {
        //Ignore, don't care
    }

    /** {@inheritDoc} */
    @Override
    public void componentHidden(final ComponentEvent e) {
        model.setSelected(false);
    }

    /**
     * Adds a close listener to the specified window, updating the specified
     * model.
     *
     * @param model Model to update
     * @param window Window to listener to
     */
    public static void add(final ButtonModel model, final Window window) {
        new CloseListener(model, window);
    }
}

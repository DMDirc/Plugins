/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes,
 * Simon Mott
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

package com.dmdirc.addons.ui_swing.components.frames;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

/**
 * Frame that contains popped out windows
 */
public class DesktopWindowFrame extends JFrame implements WindowListener {

    /** TextFrame associated with this popout window. */
    private final TextFrame windowFrame;
    /** Placeholder frame for this window whilst it is popped out. */
    private final DesktopPlaceHolderFrame placeHolder;

    /**
     * Creates a new instance of DesktopWindowFrame.
     */
    public DesktopWindowFrame(TextFrame windowFrame, DesktopPlaceHolderFrame
            placeHolder) {
        super();
        this.windowFrame = windowFrame;
        this.placeHolder = placeHolder;
        this.addWindowListener(this);
    }

    public DesktopPlaceHolderFrame getPlaceHolder() {
        return placeHolder;
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(WindowEvent e) {
        this.setVisible(false);
        windowFrame.setPopout(false);
        windowFrame.setPopoutFrame(null);
        windowFrame.getController().getMainFrame().setActiveFrame(windowFrame);

    }

    /** {@inheritDoc} */
    @Override
    public void windowOpened(WindowEvent e) {
        //ignore
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed(WindowEvent e) {
        //ignore
    }

    /** {@inheritDoc} */
    @Override
    public void windowIconified(WindowEvent e) {
        //ignore
    }

    /** {@inheritDoc} */
    @Override
    public void windowDeiconified(WindowEvent e) {
        //ignore
    }

    /** {@inheritDoc} */
    @Override
    public void windowActivated(WindowEvent e) {
        //ignore
    }

    /** {@inheritDoc} */
    @Override
    public void windowDeactivated(WindowEvent e) {
        //ignore
    }
}

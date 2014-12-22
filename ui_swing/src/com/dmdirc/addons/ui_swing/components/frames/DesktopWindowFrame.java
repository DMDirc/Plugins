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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.addons.ui_swing.injection.EdtHandlerInvocation;
import com.dmdirc.events.FrameIconChangedEvent;
import com.dmdirc.events.FrameTitleChangedEvent;

import java.awt.Point;

import javax.swing.JFrame;

import net.miginfocom.swing.MigLayout;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

/**
 * Frame that contains popped out windows
 */
public class DesktopWindowFrame extends JFrame {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** TextFrame associated with this popout window. */
    private final TextFrame windowWindow;
    /** Initial location for popped out window. */
    private final Point initialLocation;

    /**
     * Creates a new instance of DesktopWindowFrame.
     *
     * @param windowWindow Frame that we want to contain in this Desktop frame. popped out.
     */
    public DesktopWindowFrame(final TextFrame windowWindow) {
        this.windowWindow = windowWindow;
        initialLocation = windowWindow.getLocationOnScreen();

        setLayout(new MigLayout("fill, ins rel"));
        add(windowWindow, "grow");
        setPreferredSize(windowWindow.getSize());
        setTitle(windowWindow.getContainer().getTitle());
        setIconImage(windowWindow.getIconManager().getImage(windowWindow
                .getContainer().getIcon()));
    }

    /**
     * Packs and displays this frame.
     */
    public void display() {
        pack();
        setVisible(true);
        setLocation(initialLocation);
    }

    @Handler(invocation = EdtHandlerInvocation.class, delivery = Invoke.Asynchronously)
    public void iconChanged(final FrameIconChangedEvent event) {
        if (event.getContainer().equals(windowWindow.getContainer())) {
            setIconImage(windowWindow.getIconManager().getImage(event.getIcon()));
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class, delivery = Invoke.Asynchronously)
    public void titleChanged(final FrameTitleChangedEvent event) {
        if (event.getContainer().equals(windowWindow.getContainer())) {
            setTitle(event.getTitle());
        }
    }

}

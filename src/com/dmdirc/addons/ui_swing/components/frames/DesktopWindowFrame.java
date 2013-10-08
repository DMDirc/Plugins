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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.interfaces.FrameInfoListener;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import net.miginfocom.swing.MigLayout;

/**
 * Frame that contains popped out windows
 */
public class DesktopWindowFrame extends JFrame implements FrameInfoListener,
        FrameCloseListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** TextFrame associated with this popout window. */
    private final TextFrame windowWindow;
    /** Initial location for popped out window. */
    private final Point initialLocation;

    /**
     * Creates a new instance of DesktopWindowFrame.
     *
     * @param windowWindow Frame that we want to contain in this Desktop frame.
     * popped out.
     */
    public DesktopWindowFrame(final TextFrame windowWindow) {
        super();
        this.windowWindow = windowWindow;
        initialLocation = windowWindow.getLocationOnScreen();

        addWindowListener(new WindowAdapter() {
            /** {@inheritDoc} */
            @Override
            public void windowClosing(final WindowEvent e) {
                windowWindow.setPopout(false);
            }
        });
        windowWindow.getContainer().addFrameInfoListener(this);
        windowWindow.getContainer().addCloseListener(this);

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

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final FrameContainer window) {
        UIUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                windowWindow.setPopout(false);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void iconChanged(final FrameContainer window, final String icon) {
        UIUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                setIconImage(windowWindow.getIconManager().getImage(icon));
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void titleChanged(final FrameContainer window, final String title) {
        UIUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                setTitle(title);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void nameChanged(final FrameContainer window, final String name) {
        //ignore
    }

}

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

package com.dmdirc.addons.ui_swing.framemanager.windowmenu;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.interfaces.FrameInfoListener;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.SwingUtilities;

/**
 * Frame container JMenu.
 */
public class FrameContainerMenu extends JMenu implements FrameInfoListener,
        ActionListener, SelectionListener, FrameContainerMenuInterface {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** The swing controller that owns this item. */
    private final SwingController controller;
    /** Wrapped frame. */
    private final FrameContainer frame;

    /**
     * Instantiates a new FrameContainer menu item wrapping the specified frame.
     *
     * @param frame      Wrapped frame
     * @param controller Controller
     */
    public FrameContainerMenu(final FrameContainer frame,
            final SwingController controller) {
        super(frame.getName());
        setIcon(frame.getIconManager().getIcon(frame.getIcon()));
        new WindowMenuScroller(this, controller.getGlobalConfig(),
                controller.getDomain(), 0);

        this.controller = controller;
        this.frame = frame;

        addActionListener(this);
        frame.addFrameInfoListener(this);
    }

    
    @Override
    public void iconChanged(final FrameContainer window, final String icon) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                if ((frame != null && window != null) && frame.equals(window)) {
                    setIcon(window.getIconManager().getIcon(icon));
                }
            }
        });
    }

    
    @Override
    public void nameChanged(final FrameContainer window, final String name) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                if ((frame != null && window != null) && frame.equals(window)) {
                    setText(name);
                }
            }
        });
    }

    
    @Override
    public void titleChanged(final FrameContainer window, final String title) {
        // Do nothing
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        controller.requestWindowFocus(controller.getWindowFactory()
                .getSwingWindow(frame));
    }

    
    @Override
    public void selectionChanged(final TextFrame window) {
        if (frame.equals(window.getContainer())) {
            setFont(getFont().deriveFont(Font.BOLD));
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }
    }

    /**
     * Informs this menu one of its children is selected.
     */
    protected void childSelected() {
        setFont(getFont().deriveFont(Font.ITALIC));
    }

    
    @Override
    public FrameContainer getFrame() {
        return frame;
    }

}

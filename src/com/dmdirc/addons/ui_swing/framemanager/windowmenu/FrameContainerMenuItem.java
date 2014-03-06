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
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.ui.messages.Styliser;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Provider;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * Action representing a frame.
 */
public class FrameContainerMenuItem extends JMenuItem implements FrameInfoListener,
        ActionListener, SelectionListener, FrameContainerMenuInterface {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The provider to retrieve the mainframe to set active frames. */
    private final Provider<MainFrame> mainFrameProvider;
    /** Wrapped frame. */
    private final FrameContainer frame;
    /** Swing window. */
    private final TextFrame window;
    /** Parent window menu frame manager. */
    private final WindowMenuFrameManager manager;

    /**
     * Instantiates a new FrameContainer menu item wrapping the specified frame.
     *
     * @param mainFrameProvider The provider to retrieve the mainframe to set active frames.
     * @param frame             Wrapped frame
     * @param window            The window this menu item corresponds to.
     * @param manager           Parent window menu frame manager.
     */
    public FrameContainerMenuItem(
            final Provider<MainFrame> mainFrameProvider,
            final FrameContainer frame,
            final TextFrame window,
            final WindowMenuFrameManager manager) {
        super(frame.getName(), frame.getIconManager().getIcon(frame.getIcon()));

        this.mainFrameProvider = mainFrameProvider;
        this.frame = frame;
        this.window = window;
        this.manager = manager;

        addActionListener(this);
        frame.addFrameInfoListener(this);
    }

    @Override
    public void iconChanged(final FrameContainer window, final String icon) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (frame != null && window != null && frame.equals(window)) {
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
                if (frame != null && window != null && frame.equals(window)) {
                    setText(Styliser.stipControlCodes(name));
                }
            }
        });
    }

    @Override
    public void titleChanged(final FrameContainer window, final String title) {
        // Do nothing
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        mainFrameProvider.get().setActiveFrame(window);
    }

    @Override
    public void selectionChanged(final TextFrame window) {
        if (frame.equals(window.getContainer())) {
            setFont(getFont().deriveFont(Font.BOLD));
            final FrameContainer parentWindow = window.getContainer().getParent();
            if (parentWindow != null) {
                manager.parentSelection(parentWindow);
            }
        } else {
            setFont(getFont().deriveFont(Font.PLAIN));
        }
    }

    @Override
    public FrameContainer getFrame() {
        return frame;
    }

}
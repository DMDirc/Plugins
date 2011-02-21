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
package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.DMDComponent;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.commandparser.PopupType;
import java.util.Collection;
import javax.swing.JComponent;

import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * A very basic custom frame.
 */
public class CustomFrame extends TextFrame {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;

    /**
     * Creates a new instance of CustomFrame.
     *
     * @param owner The frame container that owns this frame
     * @param controller Swing controller
     */
    public CustomFrame(final SwingController controller,
            final FrameContainer owner) {
        super(owner, controller);
    }

    /**
     * Initialises components in this frame.
     *
     * @param components List of components to use in this frame
     */
    public void initComponents(final Collection<JComponent> components) {
        setLayout(new MigLayout("fill, hidemode 3, wrap 1"));
        for (JComponent component : components) {
            switch(((DMDComponent) component).getPosition()) {
                case BOTTOM:
                    add(component, "grow, push, south");
                    break;
                case CENTER:
                    add(component, "grow, push, center");
                    break;
                case LEFT:
                    add(component, "grow, push, west");
                    break;
                case RIGHT:
                    add(component, "grow, push, east");
                    break;
                case TOP:
                    add(component, "grow, push, north");
                    break;
                default:
                    add(component, "grow, push");
                    break;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNicknamePopupType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getChannelPopupType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getHyperlinkPopupType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNormalPopupType() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        //Add no custom popup items
    }
}

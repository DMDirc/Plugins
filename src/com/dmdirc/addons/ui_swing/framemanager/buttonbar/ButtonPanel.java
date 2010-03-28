/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
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
package com.dmdirc.addons.ui_swing.framemanager.buttonbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import net.miginfocom.swing.MigLayout;

/**
 * Implements scrollable onto a JPanel so we have more control over scrolling.
 *
 * @author Simon Mott
 * @since 0.6.4
 */
public class ButtonPanel extends JPanel implements Scrollable, MouseWheelListener {

    /** The ButtonBar that created this Panel. */
    private ButtonBar buttonBar;

    /**
     * Constructor for ButtonPanel.
     *
     * @param layout Layout settings for this ButtonPanel
     * @param buttonBar the buttonBar that created this Panel
     */
    public ButtonPanel(final MigLayout layout, ButtonBar buttonBar) {
        super(layout);
        this.buttonBar = buttonBar;
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return buttonBar.getButtonHeight();
    }

    /** {@inheritDoc} */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return buttonBar.getButtonHeight();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        e.consume();
        final int selectedIndex = getSelectedIndex();
        int newIndex = 0;
        if (e.getWheelRotation() < 0) {
            //Up
            //Check if selectedIndex is < 0 and if it is, get the index
            //of the bottom most button, else select the next button
            //up if buttonbar is vertical or left if horizontal
            newIndex = selectedIndex - 1 >= 0 ? selectedIndex - 1 :
                getComponentCount() - 1;
        } else if (e.getWheelRotation() > 0) {
            //Down
            //Check if selectedIndex is greater than the total number of buttons,
            //if it is select the first button in the list, else choose the next
            //button down if buttonbar is vertial or right if horizontal
            newIndex = (selectedIndex + 1) % getComponentCount();
        }
        final FrameToggleButton button = ((FrameToggleButton) getComponent(newIndex));
        button.getWindow().activateFrame();
    }

    /**
     * Gets the component index of the button associated with the current
     * selected window.
     *
     * @return Integer Index for the button of the selected window
     *
     * @since 0.6.4
     */
    private int getSelectedIndex() {
        FrameToggleButton button;
        int i = 0;
        int selectedIndex = 0;
        final FrameToggleButton selectedButton = buttonBar.getSelectedButton();
        for (Component c : getComponents()) {
            button = (FrameToggleButton) c;
            if (button == selectedButton) {
                selectedIndex = i;
                break;
            }
            i++;
        }
        return selectedIndex;
    }

}

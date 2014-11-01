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

package com.dmdirc.addons.ui_swing;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Modifies the popup of a JComboBox to make it the full width of the components it is displaying.
 */
public class ComboBoxWidthModifier implements PopupMenuListener {

    @Override
    public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
        final JComboBox<?> box = (JComboBox<?>) e.getSource();
        final Object comp = box.getUI().getAccessibleChild(box, 0);
        if (!(comp instanceof JPopupMenu)) {
            return;
        }
        final JComponent scrollPane = (JComponent) ((Container) comp).getComponent(0);
        final Dimension size = new Dimension(box.getPreferredSize().width,
                scrollPane.getPreferredSize().height);
        scrollPane.setPreferredSize(size);
        scrollPane.setMaximumSize(size);
    }

    @Override
    public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
        //Ignore
    }

    @Override
    public void popupMenuCanceled(final PopupMenuEvent e) {
        //Ignore
    }

}

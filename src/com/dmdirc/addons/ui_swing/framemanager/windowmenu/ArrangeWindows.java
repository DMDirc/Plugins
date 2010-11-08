/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Menu to add window arrangement options.
 */
public class ArrangeWindows extends JMenu implements ActionListener {
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Menu items for tiling grid, vertical, horizontal. */
    private final JMenuItem grid, vertical, horizontal;
    /** Desktop pane. */
    private SwingController controller;

    /**
     * Creates a new arrange windows menu.  Provides options to arrange windows
     * in a grid, tile vertically and tile horizontally.
     *
     * @param controller Swing controller to request mainframe/desktop pane.
     */
    public ArrangeWindows(final SwingController controller) {
        super();

        this.controller = controller;

        setIcon(IconManager.getIconManager().getIcon("dmdirc"));
        setMnemonic('a');
        setText("Arrange Windows");

        grid = new JMenuItem(IconManager.getIconManager().getIcon("dmdirc"));
        grid.setMnemonic('g');
        grid.setText("Arrange in grid");
        grid.setActionCommand("grid");
        grid.addActionListener(this);
        add(grid);

        horizontal = new JMenuItem(IconManager.getIconManager().getIcon(
                "dmdirc"));
        horizontal.setMnemonic('h');
        horizontal.setText("Arrange horizontally");
        horizontal.setActionCommand("horizontal");
        horizontal.addActionListener(this);
        add(horizontal);

        vertical = new JMenuItem(IconManager.getIconManager().getIcon(
                "dmdirc"));
        vertical.setMnemonic('v');
        vertical.setText("Arrange vertically");
        vertical.setActionCommand("vertical");
        vertical.addActionListener(this);
        add(vertical);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("grid")) {
            controller.getMainFrame().getDesktopPane().tileWindows();
        } else if (e.getActionCommand().equals("horizontally")) {
            controller.getMainFrame().getDesktopPane().tileHorizontally();
        } else if (e.getActionCommand().equals("verticallt")) {
            controller.getMainFrame().getDesktopPane().tileVertically();
        }
    }
}

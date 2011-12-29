/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.MDIBar;
import com.dmdirc.addons.ui_swing.framemanager.windowmenu.WindowMenuFrameManager;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * DMDirc menu bar.
 */
public class MenuBar extends JMenuBar {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Normal menu count. */
    private final int menuItemCount;

    /**
     * Instantiates a new menu bar.
     *
     * @param controller Swing controller
     * @param mainFrame Main frame
     */
    public MenuBar(final SwingController controller,
            final MainFrame mainFrame) {
        super();

        setLayout(new MigLayout("ins 0, fillx"));

        add(new ServerMenu(controller, mainFrame));
        add(new ChannelMenu(controller, mainFrame));
        add(new SettingsMenu(controller));
        add(new WindowMenuFrameManager(controller, mainFrame));
        add(new HelpMenu(controller));
        final int tempCount = getComponentCount();
        add(Box.createHorizontalGlue(), "growx, pushx");
        add(new MDIBar(controller, mainFrame));
        add(Box.createHorizontalStrut(PlatformDefaults.getPanelInsets(1)
                .getUnit()));
        menuItemCount = getComponentCount() - tempCount;

        getActionMap().setParent(null);
        getActionMap().clear();
    }

    /** {@inheritDoc} */
    @Override
    protected void addImpl(final Component comp, final Object constraints,
            final int index) {
        super.addImpl(comp, constraints, getComponentCount() - menuItemCount);
    }

    /**
     * Adds a new menu item to a named parent menu, creating the parent menu
     * if required.
     * @param parentMenu Name of the parent menu
     * @param menuItem Menu item to add
     */
    public void addMenuItem(final String parentMenu, final JMenuItem menuItem) {
        JMenu menu = null;
        for (int i = 0; i < getMenuCount(); i++) {
            menu = getMenu(i);
            if (menu != null && menu.getText().equals(parentMenu)) {
                break;
            }
            menu = null;
        }
        if (menu == null) {
            menu = new JMenu(parentMenu);
            add(menu);
        }

        menu.add(menuItem, 0);
    }
}

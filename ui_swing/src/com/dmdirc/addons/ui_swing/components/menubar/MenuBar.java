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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.addons.ui_swing.components.MDIBar;
import com.dmdirc.addons.ui_swing.framemanager.windowmenu.WindowMenuFrameManager;

import com.google.common.base.Optional;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * DMDirc menu bar.
 */
@Singleton
public class MenuBar extends JMenuBar {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Normal menu count. */
    private final int menuItemCount;
    /** Stores a list of tokens used to remove added menu items. */
    private final Map<String, JMenuItem> menuItems; // NOPMD

    /**
     * Instantiates a new menu bar.
     *
     * @param serverMenu   The server menu to use.
     * @param channelMenu  The channel menu to use.
     * @param settingsMenu The settings menu to use.
     * @param windowMenu   The window menu to use.
     * @param helpMenu     The help menu to use.
     * @param mdiBar       The MDI bar to use.
     */
    @Inject
    public MenuBar(
            final ServerMenu serverMenu,
            final ChannelMenu channelMenu,
            final SettingsMenu settingsMenu,
            final WindowMenuFrameManager windowMenu,
            final HelpMenu helpMenu,
            final MDIBar mdiBar) {
        menuItems = new HashMap<>();
        setLayout(new MigLayout("ins 0, fillx"));

        add(serverMenu);
        add(channelMenu);
        add(settingsMenu);
        add(windowMenu);
        add(helpMenu);
        final int tempCount = getComponentCount();
        add(Box.createHorizontalGlue(), "growx, pushx");
        add(mdiBar);
        add(Box.createHorizontalStrut(PlatformDefaults.getPanelInsets(1)
                .getUnit()));
        menuItemCount = getComponentCount() - tempCount;

        getActionMap().setParent(null);
        getActionMap().clear();
    }

    @Override
    protected void addImpl(final Component comp, final Object constraints,
            final int index) {
        super.addImpl(comp, constraints, getComponentCount() - menuItemCount);
    }

    /**
     * Adds a new menu item to a named parent menu, creating the parent menu if required.
     *
     * @param parentMenu Name of the parent menu
     * @param menuItem   Menu item to add
     */
    public void addMenuItem(final String parentMenu, final JMenuItem menuItem) {
        Optional<JMenu> menu = getParentMenuItem(parentMenu);
        if (!menu.isPresent()) {
            menu = Optional.fromNullable(new JMenu(parentMenu));
            add(menu.get());
        }
        menu.get().add(menuItem, 0);
    }

    public void removeMenuItem(final String parentMenu, final String childItem) {
        Optional<JMenu> menu = getParentMenuItem(parentMenu);
        if (menu.isPresent()) {
            Optional<JMenuItem> menuItem = getChildItem(menu.get(), childItem);
            if (menuItem.isPresent()) {
                menu.get().remove(menuItem.get());
            }
        }
    }

    private Optional<JMenu> getParentMenuItem(final String name) {
        JMenu menu = null;
        for (int i = 0; i < getMenuCount(); i++) {
            menu = getMenu(i);
            if (menu != null && menu.getText().equals(name)) {
                break;
            }
            menu = null;
        }
        return Optional.fromNullable(menu);
    }

    private Optional<JMenuItem> getChildItem(final JMenu menu, final String name) {
        Component child = null;
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            child = menu.getMenuComponent(i);
            if (child instanceof JMenuItem) {
                final JMenuItem childMenu = (JMenuItem) child;
                if (childMenu != null && childMenu.getText().equals(name)) {
                    break;
                }
            }
            child = null;
        }
        if (child instanceof JMenuItem) {
            return Optional.fromNullable((JMenuItem) child);
        } else {
            return Optional.absent();
        }
    }

}

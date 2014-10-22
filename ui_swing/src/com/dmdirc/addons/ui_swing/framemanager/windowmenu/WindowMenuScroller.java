/*
 * Copyright (c) 2006-2012 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;

import javax.swing.JMenu;

import darrylbu.util.MenuScroller;

/**
 * Window menu scroller.
 */
public class WindowMenuScroller implements ConfigChangeListener {

    /** Config manager. */
    private final AggregateConfigProvider config;
    /** Config domain. */
    private final String configDomain;
    /** Menu to scroll. */
    private final JMenu menu;
    /** Fixed menu item count. */
    private final int fixedCount;
    /** Menu scroller. */
    private MenuScroller scroller;

    /**
     * Creates a new menu scroller for the window menu.
     *
     * @param menu         Menu to create scroller for
     * @param config       Config manager
     * @param configDomain Domain to check config settings in
     * @param fixedCount   Number of fixed items in the menu
     */
    private WindowMenuScroller(final JMenu menu, final AggregateConfigProvider config,
            final String configDomain, final int fixedCount) {
        this.menu = menu;
        this.config = config;
        this.configDomain = configDomain;
        this.fixedCount = fixedCount;
    }

    private void init() {
        this.scroller = new MenuScroller(menu,
                config.getOptionInt(configDomain,"windowMenuItems"),
                config.getOptionInt(configDomain, "windowMenuScrollInterval"), fixedCount, 0);
        scroller.setShowSeperators(false);

        config.addChangeListener(configDomain, "windowMenuItems", this);
        config.addChangeListener(configDomain, "windowMenuScrollInterval", this);
    }

    /**
     * Creates a menu scroller and attaches it to the menu.
     */
    public static void createScroller(final JMenu menu, final AggregateConfigProvider config,
            final String configDomain, final int fixedCount) {
        new WindowMenuScroller(menu, config, configDomain, fixedCount).init();
    }

    @Override
    public void configChanged(final String domain, final String key) {
        UIUtilities.invokeLater(() -> {
            scroller.dispose();
            scroller = new MenuScroller(menu,
                    config.getOptionInt(configDomain, "windowMenuItems"),
                    config.getOptionInt(configDomain, "windowMenuScrollInterval"),
                    fixedCount, 0);
            scroller.setShowSeperators(false);
        });
    }

}

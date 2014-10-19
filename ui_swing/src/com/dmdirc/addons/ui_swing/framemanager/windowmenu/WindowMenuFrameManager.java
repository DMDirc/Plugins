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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.plugins.PluginDomain;

import com.google.common.collect.Maps;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.engio.mbassy.listener.Handler;

/**
 * Manages the window menu window list.
 */
@Singleton
public class WindowMenuFrameManager extends JMenu {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Window action factory. */
    private final WindowActionFactory windowActionFactory;
    /** Window selection font changer. */
    private final WindowSelectionFontChangerFactory windowSelectionFontChanger;
    /** Window to item mapping. */
    private final Map<Window, AbstractButton> menuItems;
    /** Swing event bus. */
    private final SwingEventBus swingEventBus;
    /** Close menu item. */
    private final CloseActiveWindowMenuItem closeMenuItem;
    /** Global config. */
    private final AggregateConfigProvider globalConfig;
    /** Plugin domain. */
    private final String domain;
    /** Menu separator. */
    private final WindowMenuSeparator separator;

    @Inject
    public WindowMenuFrameManager(@GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(SwingController.class) final String domain,
            final SwingEventBus swingEventBus,
            final CloseActiveWindowMenuItem closeMenuItem,
            final WindowActionFactory windowActionFactory,
            final WindowSelectionFontChangerFactory windowSelectionFontChanger,
            final WindowMenuSeparator separator) {
        this.windowActionFactory = windowActionFactory;
        this.windowSelectionFontChanger = windowSelectionFontChanger;
        this.swingEventBus = swingEventBus;
        this.closeMenuItem = closeMenuItem;
        this.globalConfig = globalConfig;
        this.separator = separator;
        this.domain = domain;
        menuItems = Maps.newHashMap();
    }

    /**
     * Initialises the frame managers and adds appropriate listeners.
     */
    public void init() {
        swingEventBus.subscribe(this);

        setText("Window");
        setMnemonic('w');

        closeMenuItem.init();
        add(closeMenuItem);
        separator.init();
        add(separator);

        WindowMenuScroller.createScroller(this, globalConfig, domain, getMenuComponentCount());
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void windowAdded(final SwingWindowAddedEvent event) {
        if (event.getParentWindow().isPresent()) {
            addChildWindow(event.getParentWindow().get(), event.getChildWindow());
        } else {
            addTopLevelWindow(event.getChildWindow());
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void windowDeleted(final SwingWindowDeletedEvent event) {
        if (event.getParentWindow().isPresent()) {
            deleteChildWindow(event.getParentWindow().get(), event.getChildWindow());
        } else {
            deleteTopLevelWindow(event.getChildWindow());
        }
    }

    private JMenu getMenu(final TextFrame window) {
        final Action action = windowActionFactory.getWindowAction(window);
        final JMenu menu = new JMenu(action);
        windowSelectionFontChanger.getWindowSelectionFontChanger(menu, window);
        return menu;
    }

    private JMenuItem getMenuItem(final TextFrame window) {
        final Action action = windowActionFactory.getWindowAction(window);
        final JMenuItem menu = new JMenuItem(action);
        windowSelectionFontChanger.getWindowSelectionFontChanger(menu, window);
        return menu;
    }

    private void addChildWindow(final TextFrame parent, final TextFrame child) {
        if (!(menuItems.get(parent) instanceof JMenu)) {
            remove(menuItems.get(parent));
            final AbstractButton item = getMenu(parent);
            menuItems.put(parent, item);
            add(item);
        }
        final AbstractButton item = getMenuItem(child);
        menuItems.put(child, item);
        menuItems.get(parent).add(item);
    }

    private void addTopLevelWindow(final TextFrame window) {
        final AbstractButton item = getMenuItem(window);
        menuItems.put(window, item);
        add(item);
    }

    private void deleteTopLevelWindow(final Window window) {
        remove(menuItems.get(window));
    }

    private void deleteChildWindow(final Window parent, final Window child) {
        menuItems.get(parent).remove(menuItems.get(child));
    }
}

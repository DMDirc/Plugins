/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.systray;

import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.events.ClientMinimisedEvent;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.messages.StyledMessageUtils;
import java.awt.AWTException;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.inject.Inject;
import net.engio.mbassy.listener.Handler;

public class SystrayManager implements ActionListener, MouseListener {

    private final PluginInfo pluginInfo;
    /** Main frame instance. */
    private final MainFrame mainFrame;
    /** This plugin's settings domain. */
    private final String domain;
    /** The config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Icon manager to get images from. */
    private final IconManager iconManager;
    private final StyledMessageUtils styleUtils;
    /** The event bus to listen to events on. */
    private final EventBus eventBus;
    /** The tray icon we're currently using. */
    private TrayIcon icon;

    @Inject
    public SystrayManager(
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(SystrayPlugin.class) final String domain,
            @PluginDomain(SystrayPlugin.class) final PluginInfo pluginInfo,
            final MainFrame mainFrame,
            final IconManager iconManager,
            final StyledMessageUtils styleUtils,
            final EventBus eventBus) {
        this.globalConfig = globalConfig;
        this.domain = domain;
        this.pluginInfo = pluginInfo;
        this.mainFrame = mainFrame;
        this.iconManager = iconManager;
        this.styleUtils = styleUtils;
        this.eventBus = eventBus;
    }

    public void load() {
        final MenuItem show = new MenuItem("Show/hide");
        final MenuItem quit = new MenuItem("Quit");

        final PopupMenu menu = new PopupMenu();
        menu.add(show);
        menu.add(quit);

        show.addActionListener(this);
        quit.addActionListener(this);

        icon = new TrayIcon(iconManager.getImage("logo"), "DMDirc", menu);
        icon.setImageAutoSize(true);
        icon.addMouseListener(this);

        try {
            SystemTray.getSystemTray().add(icon);
            eventBus.subscribe(this);
        } catch (AWTException ex) {
            throw new IllegalStateException("Unable to load plugin", ex);
        }
    }

    public void unload() {
        SystemTray.getSystemTray().remove(icon);
        eventBus.unsubscribe(this);
        icon = null;
    }

    /**
     * Sends a notification via the system tray icon.
     *
     * @param title   The title of the notification
     * @param message The contents of the notification
     * @param type    The type of notification
     */
    public void notify(final String title, final String message, final TrayIcon.MessageType type) {
        icon.displayMessage(title, styleUtils.stripControlCodes(message), type);
    }

    /**
     * Sends a notification via the system tray icon.
     *
     * @param title   The title of the notification
     * @param message The contents of the notification
     */
    public void notify(final String title, final String message) {
        notify(title, message, TrayIcon.MessageType.NONE);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Show/hide":
                mainFrame.setVisible(!mainFrame.isVisible());
                break;
            case "Quit":
                mainFrame.quit();
                break;
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (mainFrame.isVisible()) {
                mainFrame.setVisible(false);
            } else {
                mainFrame.setVisible(true);
                mainFrame.setState(Frame.NORMAL);
                mainFrame.toFront();
            }
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleClientMinimised(final ClientMinimisedEvent event) {
        if (globalConfig.getOptionBool(domain, "autominimise")) {
            mainFrame.setVisible(false);
        }
    }

    @Handler
    public void showConfig(final ClientPrefsOpenedEvent event) {
        final PreferencesDialogModel manager = event.getModel();
        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "System Tray",
                "General configuration settings");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "autominimise", "Auto-hide DMDirc when minimised",
                "If this option is enabled, the systray plugin will hide DMDirc"
                        + " to the system tray whenever DMDirc is minimised",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(category);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

}

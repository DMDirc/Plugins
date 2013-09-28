/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.systray;

import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.IdentityController;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.validators.ValidationResponse;

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

/**
 * The Systray plugin shows DMDirc in the user's system tray, and allows
 * notifications to be disabled.
 */
public final class SystrayPlugin extends BaseCommandPlugin implements
        ActionListener, MouseListener, com.dmdirc.interfaces.ActionListener {

    /** The tray icon we're currently using. */
    private final TrayIcon icon;
    /** Main frame instance. */
    private MainFrame mainFrame;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;
    /** The action controller to use. */
    private final ActionController actionController;
    /** The controller to read/write settings with. */
    private final IdentityController identityController;

    /**
     * Creates a new system tray plugin.
     *
     * @param pluginInfo This plugin's plugin info.
     * @param actionController The action controller to use.
     * @param identityController The identity manager to read settings from.
     * @param commandController Command controller to register commands.
     * @param urlBuilder URL builder to use to resolve icon paths.
     */
    public SystrayPlugin(
            final PluginInfo pluginInfo,
            final ActionController actionController,
            final IdentityController identityController,
            final CommandController commandController,
            final URLBuilder urlBuilder) {
        super(commandController);

        this.pluginInfo = pluginInfo;
        this.actionController = actionController;
        this.identityController = identityController;

        final MenuItem show = new MenuItem("Show/hide");
        final MenuItem quit = new MenuItem("Quit");

        final PopupMenu menu = new PopupMenu();
        menu.add(show);
        menu.add(quit);

        show.addActionListener(this);
        quit.addActionListener(this);

        icon = new TrayIcon(
                new IconManager(identityController.getGlobalConfiguration(), urlBuilder)
                .getImage("logo"), "DMDirc", menu);
        icon.setImageAutoSize(true);
        icon.addMouseListener(this);
        registerCommand(new PopupCommand(this), PopupCommand.INFO);
    }

    /**
     * Sends a notification via the system tray icon.
     * @param title The title of the notification
     * @param message The contents of the notification
     * @param type The type of notification
     */
    public void notify(final String title, final String message,
            final TrayIcon.MessageType type) {
        icon.displayMessage(title, Styliser.stipControlCodes(message), type);
    }

    /**
     * Sends a notification via the system tray icon.
     * @param title The title of the notification
     * @param message The contents of the notification
     */
    public void notify(final String title, final String message) {
        notify(title, message, TrayIcon.MessageType.NONE);
    }

    /**
     * Proxy method for notify, this method is used for the exported command to
     * avoid ambiguity when performing reflection.
     *
     * @param title Title for the notification
     * @param message Text for the notification
     */
    public void showPopup(final String title, final String message) {
        notify(title, message);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("Show/hide")) {
            mainFrame.setVisible(!mainFrame.isVisible());
        } else if (e.getActionCommand().equals("Quit")) {
            mainFrame.quit();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ValidationResponse checkPrerequisites() {
        if (SystemTray.isSupported()) {
            return new ValidationResponse();
        } else {
            return new ValidationResponse("System tray is not supported on "
                    + "this platform.");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        boolean continueLoading = true;
        try {
            SystemTray.getSystemTray().add(icon);
            mainFrame = ((SwingController) pluginInfo.getMetaData().getManager()
                    .getPluginInfoByName("ui_swing").getPlugin())
                    .getMainFrame();
            actionController.registerListener(this, CoreActionType.CLIENT_MINIMISED);
        } catch (AWTException ex) {
            continueLoading = false;
        }
        if (!continueLoading || mainFrame == null) {
            pluginInfo.unloadPlugin();
        }
        super.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        SystemTray.getSystemTray().remove(icon);
        actionController.unregisterListener(this);
        super.onUnload();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "System Tray",
                "General configuration settings");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "autominimise", "Auto-hide DMDirc when minimised",
                "If this option is enabled, the systray plugin will hide DMDirc"
                + " to the system tray whenever DMDirc is minimised",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(category);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
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

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @param e Mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.CLIENT_MINIMISED
                && identityController.getGlobalConfiguration()
                .getOptionBool(getDomain(), "autominimise")) {
            mainFrame.setVisible(false);
        }
    }

}

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

package com.dmdirc.addons.windowflashing;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.FLASHWINFO;

/**
 * Native notification plugin to make DMDirc support windows task bar flashing.
 */
public class WindowFlashing extends BasePlugin implements ConfigChangeListener {

    /** Library instance. */
    private User32 user32;
    /** Flash info object. */
    private FLASHWINFO flashInfo;
    /** Swing main frame. */
    private MainFrame mainFrame;
    /** Cached blink rate setting. */
    private int blinkrate = 0;
    /** Cached count setting. */
    private int flashcount = Integer.MAX_VALUE;
    /** Cached flags setting. */
    private int flags = 0;
    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public WindowFlashing(final PluginInfo pluginInfo) {
        super();
        this.pluginInfo = pluginInfo;
        registerCommand(new FlashWindow(this), FlashWindow.INFO);
    }

    /**
     * Flashes an inactive window under windows.
     */
    public void flashWindow() {
        if (!mainFrame.isFocused()) {
            setupFlashObject();
            user32.FlashWindowEx(flashInfo);
        }
    }

    /**
     * Flashes an inactive window under windows, used as a showNotifications
     * exported command
     *
     * @param title Unused
     * @param message Unused
     */
    public void flashNotification(final String title, final String message) {
        flashWindow();
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        mainFrame = ((SwingController) PluginManager
                .getPluginManager().getPluginInfoByName("ui_swing")
                .getPlugin()).getMainFrame();
        user32 = (User32) Native.loadLibrary("user32", User32.class);
        setupFlashObject();
        IdentityManager.getIdentityManager().getGlobalConfiguration()
                .addChangeListener(getDomain(), this);
        super.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        mainFrame = null;
        user32 = null;
        flashInfo = null;
        NativeLibrary.getInstance("user32").dispose();
        super.onUnload();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Window Flashing",
                "General configuration for window flashing plugin.");

        category.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALINTEGER, getDomain(), "blinkrate",
                "Blink rate", "Specifies the rate at which the taskbar and or "
                + "caption will blink, if unspecified this will be your cursor "
                + "blink rate.",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALINTEGER, getDomain(), "flashcount",
                "Flash count", "Specifies the number of times to blink, if "
                + "unspecified this will blink indefinitely",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "flashtaskbar", "Flash taskbar",
                "Should the taskbar entry flash?",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "flashcaption", "Flash caption",
                "Should the window caption flash?",
                manager.getConfigManager(), manager.getIdentity()));

        manager.addCategory(category);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        blinkrate = getTimeout();
        flashcount = getCount();
        flags = getFlags();
    }

    /**
     * Creates a new flash info object with the cached settings.
     */
    private void setupFlashObject() {
        flashInfo = new FLASHWINFO();
        flashInfo.dwFlags = flags;
        flashInfo.dwTimeout = blinkrate;
        flashInfo.uCount = flashcount;
        flashInfo.hWnd = getHWND();
        flashInfo.cbSize = flashInfo.size();
    }

    /**
     * Returns the native handle object for the main frame.
     * @return
     */
    private HWND getHWND() {
        final HWND hwnd = new HWND();
        final Pointer pointer = Native.getWindowPointer(mainFrame);
        hwnd.setPointer(pointer);
        return hwnd;
    }

    /**
     * Calculates the flags for the flash object based on config settings.
     *
     * @return Flash info flags
     */
    private int getFlags() {
        int returnValue = 0;
        if (IdentityManager.getIdentityManager().getGlobalConfiguration()
                .getOptionBool(getDomain(), "flashtaskbar")) {
            returnValue |= WinUser.FLASHW_TRAY;
        }

        if (IdentityManager.getIdentityManager().getGlobalConfiguration()
                .getOptionBool(getDomain(), "flashcaption")) {
            returnValue |= WinUser.FLASHW_CAPTION;
        }

        if (IdentityManager.getIdentityManager().getGlobalConfiguration()
                .getOptionBool(getDomain(), "flashcount")) {
            returnValue |= WinUser.FLASHW_TIMER;
        } else {
            returnValue |= WinUser.FLASHW_TIMERNOFG;
        }

        return returnValue;
    }

    /**
     * Returns the blink rate value from the config.
     *
     * @return Blink rate
     */
    private int getTimeout() {
        if (IdentityManager.getIdentityManager().getGlobalConfiguration()
                .hasOptionInt(getDomain(), "blinkrate")) {
            return IdentityManager.getIdentityManager().getGlobalConfiguration()
                    .getOptionInt(getDomain(), "blinkrate");
        } else {
            return 0;
        }
    }

    /**
     * Returns the flash count value from the config.
     *
     * @return Number of flashes before stopping
     */
    private int getCount() {
        if (IdentityManager.getIdentityManager().getGlobalConfiguration()
                .hasOptionInt(getDomain(), "flashcount")) {
            return IdentityManager.getIdentityManager().getGlobalConfiguration()
                    .getOptionInt(getDomain(), "flashcount");
        } else {
            return Integer.MAX_VALUE;
        }
    }
}

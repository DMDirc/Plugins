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

package com.dmdirc.addons.windowflashing;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.events.ClientFocusGainedEvent;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigBinding;
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
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.engio.mbassy.listener.Handler;

@Singleton
public class WindowFlashingManager {

    private final PluginInfo pluginInfo;
    /** Swing main frame. */
    private final MainFrame mainFrame;
    /** Event bus. */
    private final EventBus eventBus;
    /** Config binder. */
    private final ConfigBinder binder;
    /** Cached blink rate setting. */
    @ConfigBinding(domain = "plugin-windowflashing", key = "blinkrate",
            fallbacks = {"plugin-windowflashing", "blinkratefallback"})
    private int blinkrate;
    /** Cached count setting. */
    @ConfigBinding(domain = "plugin-windowflashing", key = "flashcount",
            fallbacks = {"plugin-windowflashing", "flashcountfallback"})
    private int flashcount;
    /** Cached flash taskbar setting. */
    @ConfigBinding(domain = "plugin-windowflashing", key = "flashtaskbar")
    private boolean flashtaskbar;
    /** Cached flash caption setting. */
    @ConfigBinding(domain = "plugin-windowflashing", key = "flashcaption")
    private boolean flashcaption;
    /** Library instance. */
    private User32 user32;

    @Inject
    public WindowFlashingManager(
            @GlobalConfig final AggregateConfigProvider config,
            @PluginDomain(WindowFlashing.class) final PluginInfo pluginInfo,
            final MainFrame mainFrame,
            final EventBus eventBus) {
        this.pluginInfo = pluginInfo;
        this.mainFrame = mainFrame;
        this.eventBus = eventBus;
        binder = config.getBinder();
    }

    public void onLoad() {
        user32 = (User32) Native.loadLibrary("user32", User32.class);
        binder.bind(this, WindowFlashing.class);
        eventBus.subscribe(this);
    }

    public void onUnload() {
        eventBus.unsubscribe(this);
        binder.unbind(this);
        user32 = null;
        NativeLibrary.getInstance("user32").dispose();
    }

    /**
     * Flashes an inactive window under windows.
     */
    public void flashWindow() {
        if (!mainFrame.isFocused()) {
            user32.FlashWindowEx(setupFlashObject());
        }
    }

    /**
     * Creates a new flash info object that starts flashing with the configured settings.
     */
    private WinUser.FLASHWINFO setupFlashObject() {
        final WinUser.FLASHWINFO flashInfo = new WinUser.FLASHWINFO();
        flashInfo.dwFlags = getFlags();
        flashInfo.dwTimeout = blinkrate;
        flashInfo.uCount = flashcount;
        flashInfo.hWnd = getHWND();
        flashInfo.cbSize = flashInfo.size();
        return flashInfo;
    }

    /**
     * Creates a new flash object that stops the flashing.
     */
    private WinUser.FLASHWINFO stopFlashObject() {
        final WinUser.FLASHWINFO flashInfo = new WinUser.FLASHWINFO();
        flashInfo.dwFlags = WinUser.FLASHW_STOP;
        flashInfo.dwTimeout = 0;
        flashInfo.uCount = 0;
        flashInfo.hWnd = getHWND();
        flashInfo.cbSize = flashInfo.size();
        return flashInfo;
    }

    /**
     * Returns the native handle object for the main frame.
     *
     * @return
     */
    private WinDef.HWND getHWND() {
        final WinDef.HWND hwnd = new WinDef.HWND();
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
        if (flashtaskbar) {
            returnValue |= WinUser.FLASHW_TRAY;
        }
        if (flashcaption) {
            returnValue |= WinUser.FLASHW_CAPTION;
        }
        if (flashcount >= 0) {
            returnValue |= WinUser.FLASHW_TIMER;
        } else {
            returnValue |= WinUser.FLASHW_TIMERNOFG;
        }
        return returnValue;
    }

    @Handler
    public void handleFocusGained(final ClientFocusGainedEvent event) {
        if (mainFrame != null) {
            user32.FlashWindowEx(stopFlashObject());
        }
    }

    @Handler
    public void showConfig(final ClientPrefsOpenedEvent event) {
        final PreferencesDialogModel manager = event.getModel();
        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Window Flashing",
                "General configuration for window flashing plugin.");

        category.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALINTEGER, pluginInfo.getDomain(), "blinkrate",
                "Blink rate", "Specifies the rate at which the taskbar and or "
                + "caption will blink, if unspecified this will be your cursor "
                + "blink rate.",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALINTEGER, pluginInfo.getDomain(), "flashcount",
                "Flash count", "Specifies the number of times to blink, if "
                + "unspecified this will blink indefinitely",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "flashtaskbar", "Flash taskbar",
                "Should the taskbar entry flash?",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "flashcaption", "Flash caption",
                "Should the window caption flash?",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(category);
    }

}

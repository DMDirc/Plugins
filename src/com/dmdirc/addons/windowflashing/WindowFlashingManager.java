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

package com.dmdirc.addons.windowflashing;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.ClientFocusGainedEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import javax.inject.Inject;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

public class WindowFlashingManager {

    /** Swing main frame. */
    private final MainFrame mainFrame;
    /** Event bus. */
    private final MBassador eventBus;
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
            final MainFrame mainFrame,
            final MBassador eventBus) {
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

}

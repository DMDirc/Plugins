/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginManager;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.FLASHWINFO;

/**
 * Native notification plugin to make DMDirc support windows task bar flashing.
 */
public class WindowFlashing extends Plugin {

    /** Stops the window flashing. */
    private static final int FLASHW_STOP = 0; //NOPMD
    /** Flashes the window caption. */
    private static final int FLASHW_CAPTION = 1; //NOPMD
    /** Flashes the window's taskbar entry. */
    private static final int FLASHW_TRAY = 2; //NOPMD
    /** Flash both the window's caption and taskbar entry. */
    private static final int FLASHW_ALL = 3;
    /** Flash until FLASHW_STOP is set. */
    private static final int FLASHW_TIMER = 4; //NOPMD
    /** Flash until the window gains focus. */
    private static final int FLASHW_TIMERNOFG = 12;
    /** Library instance. */
    private User32 user32;
    /** Flash info object. */
    private FLASHWINFO flashInfo;
    /** Swing main frame. */
    private MainFrame mainFrame;
    /** Flash window command. */
    private FlashWindow flashCommand;

    /**
     * Flashes an inactive window under windows.
     */
    public void flashWindow() {
        user32.FlashWindowEx(flashInfo);
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
        flashCommand = new FlashWindow(this);
        CommandManager.registerCommand(flashCommand);
        mainFrame = ((SwingController) PluginManager
                .getPluginManager().getPluginInfoByName("ui_swing")
                .getPlugin()).getMainFrame();
        user32 = (User32) Native.loadLibrary("user32", User32.class);
        final HWND hwnd = new HWND();
        final Pointer pointer = Native.getWindowPointer(mainFrame);
        hwnd.setPointer(pointer);
        flashInfo = new FLASHWINFO();
        flashInfo.dwFlags = FLASHW_ALL | FLASHW_TIMERNOFG;
        flashInfo.dwTimeout = 0;
        flashInfo.uCount = Integer.MAX_VALUE;
        flashInfo.hWnd = hwnd;
        flashInfo.cbSize = flashInfo.size();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        CommandManager.unregisterCommand(flashCommand);
        flashCommand = null;
        mainFrame = null;
        user32 = null;
        flashInfo = null;
        NativeLibrary.getInstance("user32").dispose();
    }
}

/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.osd;

import com.dmdirc.config.IdentityManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class to manage OSD Windows.
 *
 * @author Simon
 */
public class OsdManager {

    /** The Plugin that owns this OSD Manager. */
    private final OsdPlugin plugin;

    /** List of OSD Windows. */
    private List<OsdWindow> windowList = new ArrayList<OsdWindow>();
    private Queue<String> queueList = new LinkedList<String>();

    /** The spacing between the windows. */
    private static final int WINDOW_GAP = 5;

    /**
     * Create a new OSD Manager.
     *
     * @param plugin The plugin that owns this OSD Manager
     */
    public OsdManager(final OsdPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Add messages to the queue, if queue is less than max windows (if enabled)
     * then display window immediately.
     * 
     * @param title Window title
     * @param message Message to be displayed
     */
    public void addQueue(final String title, final String message) {
        final Integer maxWindows = IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(), "maxWindows");
        final int windowCount = getWindowCount();
        
        if (maxWindows != null && windowCount >= maxWindows) {
            queueList.add(message);
        } else {
            showWindow(title, message);
        }
    }

    /**
     * Create a new OSD window with "message"
     *
     * @param title Title of the OSD window
     * @param message Text to display in the OSD window
     */
    public void showWindow(final String title, final String message) {
        OsdWindow currentWindow = new OsdWindow(message, false,
                IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                "locationX"), getYPosition(), plugin, this);

        windowList.add(currentWindow);
    }

    /**
    * Destroy the given OSD Window and check if the Queue has items, if so
     * Display them.
    *
    * @param window The window that we are destroying.
    */
    public void killWindow(OsdWindow window) {
        windowList.remove(window);
        window.dispose();
        
        String nextItem = queueList.poll();
        if (nextItem != null) {
            showWindow("", nextItem);
        }
    }

    /**
     * Destropy all OSD Windows
     */
    public void killAll() {
        for (OsdWindow window : new ArrayList<OsdWindow>(windowList)) {
            window.setVisible(false);
            killWindow(window);
        }
    }

    /**
    * Get the list of current OSDWindows.
    *
    * @return a List of all currently open OSDWindows
    */
    public List<OsdWindow> getWindowList() {
        return new ArrayList<OsdWindow>(windowList);
    }

    /**
    * Get the count of open windows.
    *
    * @return Current number of OSD Windows open.
    */
    public int getWindowCount() {
        return windowList.size();
    }

    /**
    * Get the Y position for the next window.
    *
    * @return the Y position for the next window.
    */
    public int getYPosition() {
        final String policy = IdentityManager.getGlobalConfig()
                .getOption(plugin.getDomain(), "newbehaviour");
        int y = IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                "locationY");

        if ("down".equals(policy)) {
            // Place our new window below old windows
            for (OsdWindow window : new ArrayList<OsdWindow>(getWindowList())) {
                if (window.isVisible()) {
                    y = Math.max(y, window.getY() + window.getHeight() + WINDOW_GAP);
                }
            }
        } else if ("up".equals(policy)) {
            // Place our new window above old windows
            for (OsdWindow window : new ArrayList<OsdWindow>(getWindowList())) {
                if (window.isVisible()) {
                    y = Math.min(y, window.getY() - window.getHeight() - WINDOW_GAP);
                }
            }
        } else if ("close".equals(policy)) {
            // Close existing windows and use their place
            killAll();
        }

        return y;
    }
}
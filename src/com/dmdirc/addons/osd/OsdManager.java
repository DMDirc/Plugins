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
import java.util.List;

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

    /** The spacing between the windows. */
    private static final int WINDOW_GAP = 5;

    /**
     * Create a new OSD Manager.
     *
     * @param plugin The plugin that owns this OSD Manager
     */
    public OsdManager(final OsdPlugin plugin) {
        //Constructor
        this.plugin = plugin;
    }

    public void createOSDWindow(final String title, final String message) {
        //Check some form of queue.
        OsdWindow currentWindow = new OsdWindow(message, false,
                IdentityManager.getGlobalConfig().getOptionInt(plugin.getDomain(),
                "locationX"), getYPosition(), plugin, this);

        windowList.add(currentWindow);

        System.out.println("Current Displayed: " + getWindowCount());
    }

    /**
    * Destroy the given OSD Window.
    *
    * @param window The window that we are destroying.
    */
    public void destroyOSDWindow(OsdWindow window) {
        windowList.remove(window);
        window.dispose();
    }

    /**
     * Destropy all OSD Windows
     */
    public void destroyAllOSDWindows() {
        for (OsdWindow window : new ArrayList<OsdWindow>(windowList)) {
            window.setVisible(false);
            destroyOSDWindow(window);
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
            destroyAllOSDWindows();
        }

        return y;
    }
}
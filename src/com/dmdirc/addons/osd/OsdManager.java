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

package com.dmdirc.addons.osd;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.messages.ColourManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * Class to manage OSD Windows.
 */
public class OsdManager {

    /** The window the OSD will be associated with. */
    private final MainFrame mainFrame;
    /** The controller to read/write settings with. */
    private final IdentityController identityController;
    /** The Plugin that owns this OSD Manager. */
    private final OsdPlugin plugin;
    /** The colour manager to use to parse colours. */
    private final ColourManager colourManager;
    /** List of OSD Windows. */
    private final List<OsdWindow> windowList = new ArrayList<>();
    /** List of messages to be queued. */
    private final Queue<QueuedMessage> windowQueue = new LinkedList<>();
    /** This plugin's settings domain. */
    private final String domain;

    public OsdManager(final MainFrame mainFrame, final IdentityController identityController,
            final OsdPlugin plugin, final ColourManager colourManager,
            final PluginInfo pluginInfo) {
        this.mainFrame = mainFrame;
        this.identityController = identityController;
        this.plugin = plugin;
        this.colourManager = colourManager;
        this.domain = pluginInfo.getDomain();
    }

    /**
     * Add messages to the queue and call displayWindows.
     *
     * @param timeout Time message will be displayed
     * @param message Message to be displayed.
     */
    public void showWindow(final int timeout, final String message) {
        windowQueue.add(new QueuedMessage(timeout, message));
        displayWindows();
    }

    /**
     * Displays as many windows as appropriate.
     */
    private synchronized void displayWindows() {
        final Integer maxWindows = identityController.getGlobalConfiguration()
                .getOptionInt(domain, "maxWindows", false);

        QueuedMessage nextItem;

        while ((maxWindows == null || getWindowCount() < maxWindows)
                && (nextItem = windowQueue.poll()) != null) {
            displayWindow(nextItem.getTimeout(), nextItem.getMessage());
        }
    }

    /**
     * Create a new OSD window with "message".
     * <p>
     * This method needs to be synchronised to ensure that the window list is not modified in
     * between the invocation of
     * {@link OsdPolicy#getYPosition(com.dmdirc.addons.osd.OsdManager, int)} and the point at which
     * the {@link OsdWindow} is added to the windowList.
     *
     * @see OsdPolicy#getYPosition(com.dmdirc.addons.osd.OsdManager, int)
     * @param message Text to display in the OSD window.
     */
    private synchronized void displayWindow(final int timeout, final String message) {
        final OsdPolicy policy = OsdPolicy.valueOf(identityController.getGlobalConfiguration()
                .getOption(domain, "newbehaviour").toUpperCase());
        final int startY = identityController.getGlobalConfiguration()
                .getOptionInt(domain, "locationY");

        windowList.add(UIUtilities.invokeAndWait(
                new Callable<OsdWindow>() {
            /** {@inheritDoc} */
            @Override
            public OsdWindow call() {
                return new OsdWindow(
                        mainFrame,
                        identityController, plugin, OsdManager.this, colourManager,
                        timeout, message, false,
                        identityController.getGlobalConfiguration().getOptionInt(
                        domain, "locationX"), policy.getYPosition(OsdManager.this, startY), domain);
            }
        }));
    }

    /**
     * Destroy the given OSD Window and check if the Queue has items, if so Display them.
     *
     * @param window The window that we are destroying.
     */
    public synchronized void closeWindow(final OsdWindow window) {
        final OsdPolicy policy = OsdPolicy.valueOf(
                identityController.getGlobalConfiguration()
                .getOption(domain, "newbehaviour").toUpperCase());

        int oldY = window.getDesiredY();
        final int closedIndex = windowList.indexOf(window);

        if (closedIndex == -1) {
            return;
        }

        windowList.remove(window);

        UIUtilities.invokeLater(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                window.dispose();
            }
        });

        final List<OsdWindow> newList = getWindowList();
        for (OsdWindow otherWindow : newList.subList(closedIndex, newList.size())) {
            final int currentY = otherWindow.getDesiredY();
            if (policy.changesPosition()) {
                otherWindow.setDesiredLocation(otherWindow.getDesiredX(), oldY);
                oldY = currentY;
            }
        }
        displayWindows();
    }

    /**
     * Destroy all OSD Windows.
     */
    public void closeAll() {
        for (OsdWindow window : getWindowList()) {
            closeWindow(window);
        }
    }

    /**
     * Get the list of current OSDWindows.
     *
     * @return a List of all currently open OSDWindows.
     */
    public List<OsdWindow> getWindowList() {
        return new ArrayList<>(windowList);
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
     * Return the current plugin.
     *
     * @return Returns current plugin instance.
     */
    public OsdPlugin getPlugin() {
        return plugin;
    }

}

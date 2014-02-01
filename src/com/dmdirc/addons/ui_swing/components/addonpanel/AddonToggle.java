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

package com.dmdirc.addons.ui_swing.components.addonpanel;

import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.themes.Theme;
import com.dmdirc.ui.themes.ThemeManager;
import com.dmdirc.updater.UpdateComponent;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.UpdateStatus;
import com.dmdirc.util.collections.ListenerList;

/**
 * Wraps a Addon object (Theme or Plugin) with a boolean to indicate whether
 * it should be toggled or not.
 */
public final class AddonToggle {

    /** The PluginInfo object we're wrapping. */
    private final PluginInfo pi;
    /** The Theme object we're wrapping. */
    private final Theme theme;
    /** Addon state. */
    private boolean state;
    /** Whether or nor the addon update state should be toggled. */
    private boolean updateState;
    /** Listener list. */
    private final ListenerList listeners = new ListenerList();
    /** Identity to change update settings in. */
    private final ConfigProvider identity;
    /** The manager to update when toggling a theme. */
    private final ThemeManager themeManager;

    /**
     * Creates a new instance of AddonToggle to wrap the specified
     * PluginInfo.
     *
     * @param updateManager The update manager to use to retrieve component info.
     * @param identity Identity to change update settings in.
     * @param pi The PluginInfo to be wrapped.
     */
    public AddonToggle(
            final CachingUpdateManager updateManager,
            final ConfigProvider identity,
            final PluginInfo pi) {
        this.identity = identity;
        this.pi = pi;
        this.theme = null;
        this.themeManager = null;
        state = pi.isLoaded();

        checkUpdateState(updateManager);
    }

    /**
     * Creates a new instance of AddonToggle to wrap the specified Theme.
     *
     * @param updateManager The update manager to use to retrieve component info.
     * @param identity Identity to change update settings in.
     * @param themeManager The manager to update when toggling a theme.
     * @param theme The Theme to be wrapped
     */
    public AddonToggle(
            final CachingUpdateManager updateManager,
            final ConfigProvider identity,
            final ThemeManager themeManager,
            final Theme theme) {
        this.identity = identity;
        this.pi = null;
        this.theme = theme;
        this.themeManager = themeManager;
        state = theme.isEnabled();

        checkUpdateState(updateManager);
    }

    /**
     * Checks whether or not the component that is controlled by this toggle is enabled.
     *
     * @param updateManager The update manager to use to retrieve component info.
     */
    private void checkUpdateState(final CachingUpdateManager updateManager) {
        for (UpdateComponent comp : updateManager.getComponents()) {
            if (comp.getName().equals("addon-" + getID())) {
                updateState = updateManager.getStatus(comp) != UpdateStatus.CHECKING_NOT_PERMITTED;
                break;
            }
        }
    }

    /**
     * Sets the state of this adddon.
     *
     * @param state New state
     */
    public void setState(final boolean state) {
        this.state = state;
        triggerListener();
    }

    /**
     * Sets the update state of this addon.
     *
     * @param updateState New update state
     */
    public void setUpdateState(final boolean updateState) {
        this.updateState = updateState;
        triggerListener();
    }

    /**
     * Gets the state of this PluginInfo, taking into account the state
     * of the toggle setting.
     *
     * @return True if the plugin is or should be loaded, false otherwise.
     */
    public boolean getState() {
        return state;
    }

    /**
     * Gets the update state of this PluginInfo, taking into account the state
     * of the update toggle setting.
     *
     * @return True if the plugin is or should be updated, false otherwise.
     */
    public boolean getUpdateState() {
        if (pi != null) {
            return updateState;
        }
        return false;
    }

    /**
     * Retrieves the PluginInfo object associated with this toggle.
     *
     * @return This toggle's PluginInfo object.
     */
    public PluginInfo getPluginInfo() {
        return pi;
    }

    /**
     * Retrieves the Theme object associated with this toggle.
     *
     * @return This toggle's Theme object.
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Applies the changes to the PluginInfo, if any.
     */
    public void apply() {
        new Thread("Addon-Load-Unload") {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (pi != null) {
                    if (AddonToggle.this.getState()) {
                        pi.loadPlugin();
                    } else {
                        pi.unloadPlugin();
                    }
                    pi.getMetaData().getManager().updateAutoLoad(pi);
                    if (getID() != -1) {
                        if (getUpdateState()) {
                            identity.unsetOption("updater", "enable-addon-"
                                    + getID());
                        } else {
                            identity.setOption("updater", "enable-addon-"
                                    + getID(), false);
                        }
                    }
                }
                if (theme != null) {
                    if (AddonToggle.this.getState()) {
                        theme.applyTheme();
                    } else {
                        theme.removeTheme();
                    }
                    themeManager.synchroniseAutoLoad(theme);
                }
            }
        }.start();
    }

    /**
     * Is this addon unloadable?
     *
     * @return true iff unloadable
     */
    public boolean isUnloadable() {
        if (pi != null) {
            return pi.isUnloadable();
        }
        return true;
    }

    /**
     * Returns the name of this addon.
     *
     * @return Addon name
     */
    public String getName() {
        if (pi != null) {
            return pi.getMetaData().getFriendlyName();
        }
        if (theme != null) {
            return theme.getName();
        }

        return "Unknown addon name";
    }

    /**
     * Returns the ID of this addon. This will return -1 for any theme.
     *
     * @return Addon ID
     */
    public int getID() {
        if (pi != null) {
            return pi.getMetaData().getUpdaterId();
        }
        return -1;
    }

    /**
     * Returns the friendly version of this addon.
     *
     * @return Addon version
     */
    public String getVersion() {
        if (pi != null) {
            return pi.getMetaData().getFriendlyVersion();
        }

        if (theme != null) {
            return theme.getVersion();
        }

        return "Unknown";
    }

    /**
     * Returns the author of this addon.
     *
     * @return Addon author
     */
    public String getAuthor() {
        if (pi != null) {
            return pi.getMetaData().getAuthor();
        }

        if (theme != null) {
            return theme.getAuthor();
        }

        return "Unknown";
    }

    /**
     * Returns the description of this addon.
     *
     * @return Addon description
     */
    public String getDescription() {
        if (pi != null) {
            return pi.getMetaData().getDescription();
        }

        if (theme != null) {
            return theme.getDescription();
        }

        return "There is an error with this addon.";
    }

    /**
     * Adds an addon toggle listener to this panel.
     *
     * @param listener Listener to add
     */
    public void addListener(final AddonToggleListener listener) {
        listeners.add(AddonToggleListener.class, listener);
    }

    /**
     * Removes an addon toggle listener from this panel.
     *
     * @param listener Listener to remove
     */
    public void removeListener(final AddonToggleListener listener) {
        listeners.remove(AddonToggleListener.class, listener);
    }

    /**
     * Triggers this listener to be called across all it's listeners.
     */
    public void triggerListener() {
        listeners.getCallable(AddonToggleListener.class).addonToggled();
    }

}

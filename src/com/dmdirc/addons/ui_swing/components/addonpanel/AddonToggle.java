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
package com.dmdirc.addons.ui_swing.components.addonpanel;

import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.themes.Theme;
import com.dmdirc.ui.themes.ThemeManager;

/**
 * Wraps a Addon object (Theme or Plugin) with a boolean to indicate whether
 * it should be toggled or not.
 */
public class AddonToggle {
    
    /** The PluginInfo object we're wrapping. */
    private final PluginInfo pi;
    /** The Theme object we're wrapping. */
    private final Theme theme;
    /** Whether or not to toggle it. */
    private boolean toggled = false;

    /**
     * Creates a new instance of AddonToggle to wrap the specified
     * PluginInfo or Theme.
     * 
     * @param pi The PluginInfo to be wrapped can be null
     * @param theme The Theme to be wrapped can be null
     */
    public AddonToggle(final PluginInfo pi, final Theme theme) {
        if (pi != null && theme != null) {
            throw new IllegalArgumentException("You cannot wrap a theme and "
                    + "plugin at the same time.");
        }
        this.pi = pi;
        this.theme = theme;
    }
    
    /**
     * Toggles this PluginInfoToggle.
     */
    public void toggle() {
        toggled ^= true;
    }
    
    /**
     * Gets the state of this PluginInfo, taking into account the state
     * of the toggle setting.
     * 
     * @return True if the plugin is or should be loaded, false otherwise.
     */
    public boolean getState() {
        if (pi != null) {
            return toggled ^ pi.isLoaded();
        }
        if (theme != null) {
            return toggled ^ theme.isEnabled();
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
        if (pi != null && toggled) {
            if (pi.isLoaded()) {
                pi.unloadPlugin();
            } else {
                pi.loadPlugin();
            }
            PluginManager.getPluginManager().updateAutoLoad(pi);
        }
        if (theme != null) {
            if (theme.isEnabled()) {
                theme.applyTheme();
            } else {
                theme.removeTheme();
            }
            ThemeManager.updateAutoLoad(theme);
        }
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

}

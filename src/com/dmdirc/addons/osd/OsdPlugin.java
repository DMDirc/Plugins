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

import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.CategoryChangeListener;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.config.prefs.SettingChangeListener;
import com.dmdirc.util.validators.NumericalValidator;
import com.dmdirc.util.validators.OptionalValidator;
import com.dmdirc.plugins.Plugin;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows the user to display on-screen-display messages.
 * @author chris
 */
public final class OsdPlugin extends Plugin implements CategoryChangeListener,
        PreferencesInterface, SettingChangeListener {
    
    /** Config OSD Window. */
    private OsdWindow osdWindow;
    
    /** OSD Command. */
    private OsdCommand command;

    /** The OSD Manager that this plugin is using. */
    private OsdManager osdManager;
    
    /** X-axis position of OSD. */
    private int x;
    
    /** Y-axis potion of OSD. */
    private int y;
    
    /** Setting objects with registered change listeners.*/
    /**
     * Setting objects with registered change listeners.
     * maxWindowSetting not used at present so comment out
     */
    private PreferencesSetting fontSizeSetting, backgroundSetting,
            foregroundSetting, widthSetting, timeoutSetting, maxWindowsSetting;

    /**
     * Creates a new instance of OsdPlugin.
     */
    public OsdPlugin() {
        super();
        osdManager = new OsdManager(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        command = new OsdCommand(this, osdManager);
        CommandManager.registerCommand(command);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        CommandManager.unregisterCommand(command);
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        x = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "locationX");
        y = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "locationY");
        
        final PreferencesCategory category = new PluginPreferencesCategory(
                getPluginInfo(), "OSD",
                "General configuration for OSD plugin.", "category-osd");
        
        fontSizeSetting = new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "fontSize", "Font size", "Changes the font " +
                "size of the OSD").registerChangeListener(this);
        backgroundSetting = new PreferencesSetting(PreferencesType.COLOUR,
                getDomain(), "bgcolour", "Background colour",
                "Background colour for the OSD").registerChangeListener(this);
        foregroundSetting = new PreferencesSetting(PreferencesType.COLOUR,
                getDomain(), "fgcolour", "Foreground colour",
                "Foreground colour for the OSD").registerChangeListener(this);
        widthSetting = new PreferencesSetting(PreferencesType.INTEGER,
                getDomain(), "width", "OSD Width", "Width of the OSD Window").
                registerChangeListener(this);
        timeoutSetting = new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                new OptionalValidator(new NumericalValidator(1, Integer.MAX_VALUE)),
                getDomain(), "timeout", "Timeout", "Length of time in " +
                "seconds before the OSD window closes");
        maxWindowsSetting = new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                new OptionalValidator(new NumericalValidator(1, Integer.MAX_VALUE)),
                getDomain(), "maxWindows", "Maximum open windows", "Maximum number of OSD " +
                "windows that will be displayed at any given time");
                
        category.addSetting(fontSizeSetting);
        category.addSetting(backgroundSetting);
        category.addSetting(foregroundSetting);
        category.addSetting(widthSetting);
        category.addSetting(timeoutSetting);
        category.addSetting(maxWindowsSetting);
        
        final Map<String, String> posOptions = new HashMap<String, String>();

        //Populate policy MULTICHOICE
        for (OsdPolicy policy : OsdPolicy.values()) {
            posOptions.put(policy.name(), policy.getDescription());
        }
        
        category.addSetting(new PreferencesSetting(getDomain(), "newbehaviour",
                "New window policy", "What to do when an OSD Window "
                + "is opened when there are other, existing windows open", posOptions));

        category.addChangeListener(this);
        manager.getCategory("Plugins").addSubCategory(category);
        manager.registerSaveListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void categorySelected(final PreferencesCategory category) {
        osdWindow = new OsdWindow("Please drag this OSD to position", true, x, y, this, osdManager);
        osdWindow.setBackgroundColour(backgroundSetting.getValue());
        osdWindow.setForegroundColour(foregroundSetting.getValue());
        osdWindow.setFontSize(Integer.parseInt(fontSizeSetting.getValue()));
    }

    /** {@inheritDoc} */
    @Override
    public void categoryDeselected(final PreferencesCategory category) {
        x = osdWindow.getLocationOnScreen().x;
        y = osdWindow.getLocationOnScreen().y;
        
        osdWindow.dispose();
        osdWindow = null;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        IdentityManager.getConfigIdentity().setOption(getDomain(), "locationX", x);
        IdentityManager.getConfigIdentity().setOption(getDomain(), "locationY", y);
    }

    /** {@inheritDoc} */
    @Override
    public void settingChanged(final PreferencesSetting setting) {
        if (osdWindow == null) {
            // They've changed categories but are somehow poking settings.
            // Ignore the request.
            return;
        }
        
        if (setting.equals(fontSizeSetting)) {
            osdWindow.setFontSize(Integer.parseInt(setting.getValue()));
        } else if (setting.equals(backgroundSetting)) {
            osdWindow.setBackgroundColour(setting.getValue());
        } else if (setting.equals(foregroundSetting)) {
            osdWindow.setForegroundColour(setting.getValue());
        } else if (setting.equals(widthSetting)) {
            int width = 500;
            try {
                width = Integer.parseInt(setting.getValue());
            } catch (NumberFormatException e) {
                //Ignore
            }
            osdWindow.setSize(width, osdWindow.getHeight());
        }
    }

    /**
     * Shows an OSD with the specified message, title is ignored, exported
     * method used for showNotification.
     *
     * @param title Ignored
     * @param message Message to show
     */
    public void showOSD(final String title, final String message) {
        osdManager.showWindow(message);
    }

}

/*
 * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.freedesktop_notifications;

import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.resourcemanager.ResourceManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.Main;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.installer.StreamReader;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.Identity;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.addons.freedesktop_notifications.commons.StringEscapeUtils;;

/**
 * This plugin adds freedesktop Style Notifications to dmdirc.
 *
 * @author Shane 'Dataforce' McCormack
 */
public final class FreeDesktopNotificationsPlugin extends Plugin {
    /** The DcopCommand we created */
    private FDNotifyCommand command = null;
    
    /** Files dir */
    private static final String filesDir  = Main.getConfigDir() + "plugins/freedesktop_notifications_files/";
    
    /**
     * Creates a new instance of the FreeDesktopNotifications Plugin.
     */
    public FreeDesktopNotificationsPlugin() {
        super();
    }

    /**
     * Used to show a notification using this plugin.
     *
     * @param title Title of dialog if applicable
     * @param message Message to show
     * @return True if the notification was shown.
     */
    public boolean showNotification(final String title, final String message) {
        final int seconds = IdentityManager.getGlobalConfig().getOptionInt(getDomain(), "general.timeout");
        final String icon = IdentityManager.getGlobalConfig().getOption(getDomain(), "general.icon");
        final ArrayList<String> args = new ArrayList<String>();
        
        args.add("/usr/bin/env");
        args.add("python");
        args.add(filesDir+"notify.py");
        args.add("-a");
        args.add("DMDirc");
        args.add("-i");
        args.add(icon);
        args.add("-t");
        args.add(Integer.toString(seconds * 1000));
        args.add("-s");

        if (title != null && !title.isEmpty()) {
            args.add(prepareString(title));
        } else {
            args.add("Notification from DMDirc");
        }
        args.add(prepareString(message));
        
        try {
            final Process myProcess = Runtime.getRuntime().exec(args.toArray(new String[]{}));
            final StringBuffer data = new StringBuffer();
            new StreamReader(myProcess.getErrorStream()).start();
            new StreamReader(myProcess.getInputStream(), data).start();
            try { myProcess.waitFor(); } catch (InterruptedException e) { }
            System.out.println(data.toString());
            return true;
        } catch (SecurityException e) {
        } catch (IOException e) {
        }
        
        return false;
    }
    
    /**
     * Prepare the string for sending to dbus.
     *
     * @param input Input string
     * @return Input string after being processed according to config settings.
     */
    public final String prepareString(final String input) {
        final boolean escapehtml = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.escapehtml");
	final boolean strictescape = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.strictescape");
        final boolean stripcodes = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "advanced.stripcodes");

        String output = input;
        if (stripcodes) { output = Styliser.stipControlCodes(output); }
        if (escapehtml) {
	    if (strictescape) {
                output = StringEscapeUtils.escapeHtml(output);
            } else {
                output = output.replaceAll("&", "&amp;");
                output = output.replaceAll("<", "&lt;");
                output = output.replaceAll(">", "&gt;");
            }
        }
        
        return output;
    }

    /**
     * Called when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        command = new FDNotifyCommand(this);

        // Extract required Files
        final PluginInfo pi = PluginManager.getPluginManager().getPluginInfoByName("freedesktop_notifications");
        
        // This shouldn't actually happen, but check to make sure.
        if (pi != null) {
            // Now get the RM
            try {
                final ResourceManager res = pi.getResourceManager();
                
                // Make sure our files dir exists
                final File newDir = new File(filesDir);
                if (!newDir.exists()) {
                    newDir.mkdirs();
                }
            
                // Now extract the files needed
                extractFiles(res, newDir, ".py");
                extractFiles(res, newDir, ".png");
            } catch (IOException ioe) {
                Logger.userError(ErrorLevel.LOW, "Unable to open ResourceManager for freedesktop_notifications: "+ioe.getMessage(), ioe);
            }
        }
    }
    
    /**
     * Use the given resource manager to extract files ending with the given suffix
     *
     * @param res ResourceManager
     * @param newDir Directory to extract to.
     * @param suffix Suffix to extract
     */
    private static void extractFiles(final ResourceManager res, final File newDir, final String suffix) {
        final Map<String, byte[]> resources = res.getResourcesEndingWithAsBytes(suffix);
        for (Entry<String, byte[]> resource : resources.entrySet()) {
            try {
                final String key = resource.getKey();
                final String resourceName = key.substring(key.lastIndexOf('/'), key.length());

                final File newFile = new File(newDir, resourceName);

                if (!newFile.isDirectory()) {
                    if (newFile.exists()) { newFile.delete(); }
                    ResourceManager.getResourceManager().resourceToFile(resource.getValue(), newFile);
                }
            } catch (IOException ex) {
                Logger.userError(ErrorLevel.LOW, "Failed to extract "+suffix+"s for freedesktop_notifications: "+ex.getMessage(), ex);
            }
        }
    }

    /**
     * Called when this plugin is Unloaded.
     */
    @Override
    public synchronized void onUnload() {
        CommandManager.unregisterCommand(command);
    }
    
    /** {@inheritDoc} */
    @Override
    public void domainUpdated() {
        final Identity defaults = IdentityManager.getAddonIdentity();
        defaults.setOption(getDomain(), "general.icon", filesDir+"icon.png");
    }
    
    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory general = new PreferencesCategory("FreeDesktop Notifications", "General configuration for FreeDesktop Notifications plugin.");
        
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER, getDomain(), "general.timeout", "Timeout", "Length of time in seconds before the notification popup closes."));
        general.addSetting(new PreferencesSetting(PreferencesType.TEXT, getDomain(), "general.icon", "icon", "Path to icon to use on the notification."));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.escapehtml", "Escape HTML", "Some Implementations randomly parse HTML, escape it before showing?"));
	general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.strictescape", "Strict Escape HTML", "Strictly escape HTML or just the basic characters? (&, < and >)"));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "advanced.stripcodes", "Strip Control Codes", "Strip IRC Control codes from messages?"));
        
        manager.getCategory("Plugins").addSubCategory(general);
    }
}


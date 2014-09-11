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

package com.dmdirc.addons.logging;

import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import dagger.ObjectGraph;

/**
 * Adds logging facility to client.
 */
public class LoggingPlugin extends BaseCommandPlugin {

    /** This plugin's plugin info. */
    private final PluginInfo pluginInfo;

    /** The manager in use. */
    private LoggingManager manager;

    /**
     * Creates a new instance of this plugin.
     *
     * @param pluginInfo This plugin's plugin info
     */
    public LoggingPlugin(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new LoggingModule(pluginInfo.getDomain())));
        manager = getObjectGraph().get(LoggingManager.class);

        registerCommand(LoggingCommand.class, LoggingCommand.INFO);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        manager.load();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        manager.unload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Logging", "General configuration for Logging plugin.");
        final PreferencesCategory backbuffer = new PluginPreferencesCategory(
                pluginInfo, "Back Buffer", "Options related to the automatic backbuffer");
        final PreferencesCategory advanced = new PluginPreferencesCategory(
                pluginInfo, "Advanced",
                "Advanced configuration for Logging plugin. You shouldn't need to edit this unless you know what you are doing.");

        general.addSetting(new PreferencesSetting(PreferencesType.DIRECTORY,
                pluginInfo.getDomain(), "general.directory", "Directory",
                "Directory for log files", manager.getConfigManager(),
                manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "general.networkfolders",
                "Separate logs by network",
                "Should the files be stored in a sub-dir with the networks name?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "general.addtime", "Timestamp logs",
                "Should a timestamp be added to the log files?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                pluginInfo.getDomain(), "general.timestamp", "Timestamp format",
                "The String to pass to 'SimpleDateFormat' to format the timestamp",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "general.stripcodes", "Strip Control Codes",
                "Remove known irc control codes from lines before saving?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "general.channelmodeprefix",
                "Show channel mode prefix", "Show the @,+ etc next to nicknames",
                manager.getConfigManager(), manager.getIdentity()));

        backbuffer.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "backbuffer.autobackbuffer", "Automatically display",
                "Automatically display the backbuffer when a channel is joined",
                manager.getConfigManager(), manager.getIdentity()));
        backbuffer.addSetting(new PreferencesSetting(PreferencesType.COLOUR,
                pluginInfo.getDomain(), "backbuffer.colour", "Colour to use for display",
                "Colour used when displaying the backbuffer",
                manager.getConfigManager(), manager.getIdentity()));
        backbuffer.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "backbuffer.lines", "Number of lines to show",
                "Number of lines used when displaying backbuffer",
                manager.getConfigManager(), manager.getIdentity()));
        backbuffer.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "backbuffer.timestamp", "Show Formatter-Timestamp",
                "Should the line be added to the frame with the timestamp from "
                + "the formatter aswell as the file contents",
                manager.getConfigManager(), manager.getIdentity()));

        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "advanced.filenamehash", "Add Filename hash",
                "Add the MD5 hash of the channel/client name to the filename. "
                + "(This is used to allow channels with similar names "
                + "(ie a _ not a  -) to be logged separately)",
                manager.getConfigManager(), manager.getIdentity()));

        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "advanced.usedate", "Use Date directories",
                "Should the log files be in separate directories based on the date?",
                manager.getConfigManager(), manager.getIdentity()));
        advanced.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                pluginInfo.getDomain(), "advanced.usedateformat", "Archive format",
                "The String to pass to 'SimpleDateFormat' to format the "
                + "directory name(s) for archiving",
                manager.getConfigManager(), manager.getIdentity()));

        general.addSubCategory(backbuffer.setInline());
        general.addSubCategory(advanced.setInline());
        manager.getCategory("Plugins").addSubCategory(general.setInlineAfter());
    }

}

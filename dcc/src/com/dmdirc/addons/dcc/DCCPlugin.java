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

package com.dmdirc.addons.dcc;

import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import dagger.ObjectGraph;

/**
 * Adds support for DCC transfers and chats.
 */
public class DCCPlugin extends BaseCommandPlugin {

    private final PluginInfo pluginInfo;
    private DCCManager dccManager;

    public DCCPlugin(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new DCCPluginModule(pluginInfo)));
        dccManager = getObjectGraph().get(DCCManager.class);
        registerCommand(DCCCommand.class, DCCCommand.INFO);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        dccManager.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        dccManager.onUnload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "DCC", "", "category-dcc");
        final PreferencesCategory firewall = new PluginPreferencesCategory(
                pluginInfo, "Firewall", "");
        final PreferencesCategory sending = new PluginPreferencesCategory(
                pluginInfo, "Sending", "");
        final PreferencesCategory receiving = new PluginPreferencesCategory(
                pluginInfo, "Receiving", "");

        manager.getCategory("Plugins").addSubCategory(general.setInlineAfter());
        general.addSubCategory(firewall.setInline());
        general.addSubCategory(sending.setInline());
        general.addSubCategory(receiving.setInline());

        firewall.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                pluginInfo.getDomain(), "firewall.ip", "Forced IP",
                "What IP should be sent as our IP (Blank = work it out)",
                manager.getConfigManager(), manager.getIdentity()));
        firewall.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "firewall.ports.usePortRange", "Use Port Range",
                "Useful if you have a firewall that only forwards specific "
                + "ports", manager.getConfigManager(), manager.getIdentity()));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "firewall.ports.startPort", "Start Port",
                "Port to try to listen on first", manager.getConfigManager(),
                manager.getIdentity()));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "firewall.ports.endPort", "End Port",
                "Port to try to listen on last", manager.getConfigManager(),
                manager.getIdentity()));
        receiving.addSetting(new PreferencesSetting(PreferencesType.DIRECTORY,
                pluginInfo.getDomain(), "receive.savelocation", "Default save location",
                "Where the save as window defaults to?",
                manager.getConfigManager(), manager.getIdentity()));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "send.reverse", "Reverse DCC",
                "With reverse DCC, the sender connects rather than "
                + "listens like normal dcc", manager.getConfigManager(),
                manager.getIdentity()));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "send.forceturbo", "Use Turbo DCC",
                "Turbo DCC doesn't wait for ack packets. this is "
                + "faster but not always supported.",
                manager.getConfigManager(), manager.getIdentity()));
        receiving.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "receive.reverse.sendtoken",
                "Send token in reverse receive",
                "If you have problems with reverse dcc receive resume,"
                + " try toggling this.", manager.getConfigManager(),
                manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "send.blocksize", "Blocksize to use for DCC",
                "Change the block size for send/receive, this can "
                + "sometimes speed up transfers.", manager.getConfigManager(),
                manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "general.percentageInTitle",
                "Show percentage of transfers in the window title",
                "Show the current percentage of transfers in the DCC window "
                + "title", manager.getConfigManager(), manager.getIdentity()));
    }

}

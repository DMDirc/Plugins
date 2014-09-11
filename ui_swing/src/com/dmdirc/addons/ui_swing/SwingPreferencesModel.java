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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.util.validators.NumericalValidator;

import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

/**
 * Manages the swing UI preferences.
 */
public class SwingPreferencesModel {

    private final PluginInfo pluginInfo;
    private final String domain;
    private final AggregateConfigProvider globalConfig;
    private final ConfigProvider globalIdentity;

    public SwingPreferencesModel(final PluginInfo pluginInfo,
            final String domain,
            final AggregateConfigProvider globalConfig,
            final ConfigProvider globalIdentity) {
        this.pluginInfo = pluginInfo;
        this.domain = domain;
        this.globalConfig = globalConfig;
        this.globalIdentity = globalIdentity;
    }

    /**
     * Creates the Swing UI category.
     *
     * @return Swing UI preferences category
     */
    public PreferencesCategory getSwingUICategory() {
        return createGeneralCategory();

    }

    /**
     * Creates the "Advanced" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createGeneralCategory() {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Swing UI", "These config options apply "
                + "only to the swing UI.", "category-gui");

        final Map<String, String> lafs = new HashMap<>();
        final Map<String, String> framemanagers = new HashMap<>();
        final Map<String, String> fmpositions = new HashMap<>();

        // TODO: When we can inject nicely, use a service locator to find all implementations.
        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManagerProvider",
                "Treeview");
        framemanagers.put(
                "com.dmdirc.addons.ui_swing.framemanager.buttonbar.ButtonBarProvider",
                "Button bar");

        fmpositions.put("top", "Top");
        fmpositions.put("bottom", "Bottom");
        fmpositions.put("left", "Left");
        fmpositions.put("right", "Right");

        final UIManager.LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();

        lafs.put("Native", "Native");
        for (final UIManager.LookAndFeelInfo laf : plaf) {
            lafs.put(laf.getName(), laf.getName());
        }

        general.addSetting(new PreferencesSetting("ui", "lookandfeel",
                "Look and feel", "The Java look and feel to use", lafs,
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting("ui", "framemanager",
                "Window manager", "Which window manager should be used?",
                framemanagers,
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting("ui", "framemanagerPosition",
                "Window manager position", "Where should the window "
                + "manager be positioned?", fmpositions,
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting(PreferencesType.FONT,
                "ui", "textPaneFontName", "Textpane font",
                "Font for the textpane",
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                "ui", "textPaneFontSize", "Textpane font size",
                "Font size for the textpane",
                globalConfig, globalIdentity).setRestartNeeded());
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "sortrootwindows", "Sort root windows",
                "Sort child windows in the frame managers?",
                globalConfig, globalIdentity));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "sortchildwindows", "Sort child windows",
                "Sort root windows in the frame managers?",
                globalConfig, globalIdentity));

        general.addSubCategory(createNicklistCategory());
        general.addSubCategory(createTreeViewCategory());
        general.addSubCategory(createAdvancedCategory());

        return general;
    }

    /**
     * Creates the "Advanced" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createAdvancedCategory() {
        final PreferencesCategory advanced = new PluginPreferencesCategory(
                pluginInfo, "Advanced", "");

        advanced.addSetting(new PreferencesSetting(
                PreferencesType.INTEGER, new NumericalValidator(10, -1),
                "ui", "frameBufferSize",
                "Window buffer size", "The maximum number of lines in a "
                + "window buffer", globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "mdiBarVisibility", "MDI Bar Visibility",
                "Controls the visibility of the MDI bar",
                globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.BOOLEAN, "ui",
                        "useOneTouchExpandable", "Use one touch expandable split "
                        + "panes?", "Use one touch expandable arrows for "
                        + "collapsing/expanding the split panes",
                        globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                domain, "windowMenuItems", "Window menu item count",
                "Number of items to show in the window menu",
                globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.INTEGER, domain,
                        "windowMenuScrollInterval", "Window menu scroll interval",
                        "Number of milliseconds to pause when autoscrolling in the "
                        + "window menu",
                        globalConfig, globalIdentity));
        advanced.addSetting(
                new PreferencesSetting(PreferencesType.BOOLEAN, domain,
                        "showtopicbar", "Show topic bar",
                        "Shows a graphical topic bar in channels.",
                        globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain,
                "shownicklist", "Show nicklist?",
                "Do you want the nicklist visible",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "showfulltopic", "Show full topic in topic bar?",
                "Do you want to show the full topic in the topic bar or just"
                + "first line?",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "hideEmptyTopicBar", "Hide empty topic bar?",
                "Do you want to hide the topic bar when there is no topic",
                globalConfig, globalIdentity));
        advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "textpanelinenotification",
                "New line notification", "Do you want to be notified about new "
                + "lines whilst scrolled up?",
                globalConfig, globalIdentity));

        return advanced;
    }

    /**
     * Creates the "Treeview" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createTreeViewCategory() {
        final PreferencesCategory treeview = new PluginPreferencesCategory(
                pluginInfo, "Treeview", "", "treeview");

        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "treeview", "backgroundcolour", "Treeview background colour",
                "Background colour to use for the treeview",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "treeview", "foregroundcolour", "Treeview foreground colour",
                "Foreground colour to use for the treeview",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewRolloverColour", "Treeview rollover colour",
                "Background colour to use when the mouse cursor is over a "
                + "node",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "treeviewActiveBold", "Active node bold",
                "Make the active node bold?",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveBackground", "Active node background",
                "Background colour to use for active treeview node",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "treeviewActiveForeground", "Active node foreground",
                "Foreground colour to use for active treeview node",
                globalConfig, globalIdentity));
        treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "showtreeexpands", "Show expand/collapse handles",
                "Do you want to show tree view collapse/expand handles",
                globalConfig, globalIdentity));

        return treeview;
    }

    /**
     * Creates the "Nicklist" category.
     *
     * @return Newly created preferences category
     */
    private PreferencesCategory createNicklistCategory() {
        final PreferencesCategory nicklist = new PluginPreferencesCategory(
                pluginInfo, "Nicklist", "", "nicklist");

        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistbackgroundcolour", "Nicklist background colour",
                "Background colour to use for the nicklist",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nicklistforegroundcolour", "Nicklist foreground colour",
                "Foreground colour to use for the nicklist",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(
                PreferencesType.OPTIONALCOLOUR,
                "ui", "nickListAltBackgroundColour",
                "Alternate background colour",
                "Background colour to use for every other nicklist entry",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByMode", "Sort nicklist by user mode",
                "Sort nicknames by the modes that they have?",
                globalConfig, globalIdentity));
        nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "nicklist", "sortByCase", "Sort nicklist by case",
                "Sort nicknames in a case-sensitive manner?",
                globalConfig, globalIdentity));

        return nicklist;
    }

}

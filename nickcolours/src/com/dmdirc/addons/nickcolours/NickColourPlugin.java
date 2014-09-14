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

package com.dmdirc.addons.nickcolours;

import com.dmdirc.ClientModule;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BasePlugin;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Window;
import java.util.concurrent.Callable;

import dagger.ObjectGraph;

/**
 * Adds support for nick colours throughout the client.
 */
public class NickColourPlugin extends BasePlugin {

    /** Plugin info. */
    private final PluginInfo pluginInfo;
    /** Main window that will own any dialogs. */
    private final Window mainWindow;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Colour manager. */
    private final ColourManager colourManager;
    /** Nick colour manager. */
    private NickColourManager nickColourManager;

    public NickColourPlugin(final PluginInfo pluginInfo, final SwingController controller,
            final IconManager iconManager,
            @ClientModule.GlobalConfig final ColourManager colourManager) {
        this.pluginInfo = pluginInfo;
        this.mainWindow = controller.getMainFrame();
        this.iconManager = iconManager;
        this.colourManager = colourManager;
    }

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new NickColourModule(pluginInfo.getDomain())));
        nickColourManager = getObjectGraph().get(NickColourManager.class);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        nickColourManager.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        nickColourManager.onUnload();
    }

    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "Nick Colours",
                "General configuration for NickColour plugin.");
        final PreferencesCategory colours = new PluginPreferencesCategory(
                pluginInfo, "Colours",
                "Set colours for specific nicknames.", UIUtilities.invokeAndWait(
                        new Callable<NickColourPanel>() {

                            @Override
                            public NickColourPanel call() {
                                return new NickColourPanel(mainWindow, iconManager, colourManager,
                                        manager.getIdentity(), manager.getConfigManager(),
                                        pluginInfo.getDomain());
                            }
                        }));

        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursintext", "Show colours in text area",
                "Colour nicknames in main text area?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                "ui", "shownickcoloursinnicklist", "Show colours in"
                + " nick list", "Colour nicknames in channel nick lists?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, pluginInfo.getDomain(),
                "settext", "Set colours in textarea",
                "Should the plugin set the textarea colour of nicks?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, pluginInfo.getDomain(),
                "setnicklist", "Set colours in nick list",
                "Should the plugin set the nick list colour of nicks?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "userandomcolour", "Use random colour",
                "Use a pseudo-random colour for each person?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "useowncolour", "Use colour for own nick",
                "Always use the same colour for our own nickname?",
                manager.getConfigManager(), manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.COLOUR, pluginInfo.getDomain(),
                "owncolour", "Colour to use for own nick",
                "Colour used for our own nickname, if above setting is "
                + "enabled.", manager.getConfigManager(), manager.getIdentity()));

        general.addSubCategory(colours);
        manager.getCategory("Plugins").addSubCategory(general);
    }

}

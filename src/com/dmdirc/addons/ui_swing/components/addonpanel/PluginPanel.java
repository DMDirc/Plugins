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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.addonbrowser.DataLoaderWorkerFactory;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.updater.manager.CachingUpdateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Lists known plugins, enabling the end user to enable/disable these as well as download new ones.
 */
public class PluginPanel extends AddonPanel implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** Manager to retrieve plugin information from. */
    private final PluginManager pluginManager;
    /** Manager to use to retrieve addon-related icons. */
    private final IconManager iconManager;
    /** Manager to use to retrieve update information. */
    private final CachingUpdateManager updateManager;
    /** Configuration to write update-related settings to. */
    private final ConfigProvider userConfig;

    /**
     * Creates a new instance of PluginPanel.
     *
     * @param parentWindow  Parent window
     * @param pluginManager Manager to retrieve plugins from.
     * @param workerFactory Factory to use to create data workers.
     * @param iconManager   Manager to use to retrieve addon-related icons.
     * @param updateManager Manager to use to retrieve update information.
     * @param userConfig    Configuration to write update-related settings to.
     */
    @Inject
    public PluginPanel(
            final MainFrame parentWindow,
            final PluginManager pluginManager,
            final DataLoaderWorkerFactory workerFactory,
            @GlobalConfig final IconManager iconManager,
            final CachingUpdateManager updateManager,
            @UserConfig final ConfigProvider userConfig) {
        super(parentWindow, workerFactory);
        this.pluginManager = pluginManager;
        this.iconManager = iconManager;
        this.updateManager = updateManager;
        this.userConfig = userConfig;
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.PLUGIN_REFRESH);
        pluginManager.refreshPlugins();
    }

    @Override
    protected JTable populateList(final JTable table) {
        final List<PluginInfo> list = new ArrayList<>();
        final List<PluginInfo> sortedList = new ArrayList<>();
        list.addAll(pluginManager.getPluginInfos());
        Collections.sort(list);
        for (final PluginInfo plugin : list) {
            if (plugin.getMetaData().getParent() == null) {
                final List<PluginInfo> childList = new ArrayList<>();
                sortedList.add(plugin);
                for (final PluginInfo child : plugin.getChildren()) {
                    if (!childList.contains(child)) {
                        childList.add(child);
                    }
                }
                Collections.sort(childList);
                sortedList.addAll(childList);
            }
        }

        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ((DefaultTableModel) table.getModel()).setNumRows(0);
                for (final PluginInfo plugin : sortedList) {
                    ((DefaultTableModel) table.getModel()).addRow(
                            new AddonCell[]{
                                new AddonCell(
                                        new AddonToggle(
                                                updateManager,
                                                userConfig,
                                                plugin),
                                        iconManager),});
                }
                table.repaint();
            }
        });
        return table;
    }

    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        populateList(addonList);
    }

    @Override
    protected String getTypeName() {
        return "plugins";
    }

}

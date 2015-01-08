/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.addonbrowser.DataLoaderWorkerFactory;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.events.PluginRefreshEvent;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.updater.manager.CachingUpdateManager;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.engio.mbassy.listener.Handler;

/**
 * Lists known plugins, enabling the end user to enable/disable these as well as download new ones.
 */
public class PluginPanel extends AddonPanel {

    /** A version number for this class. */
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
     * @param eventBus      Event bus to subscribe to events on
     * @param parentWindow  Parent window
     * @param pluginManager Manager to retrieve plugins from.
     * @param workerFactory Factory to use to create data workers.
     * @param iconManager   Manager to use to retrieve addon-related icons.
     * @param updateManager Manager to use to retrieve update information.
     * @param userConfig    Configuration to write update-related settings to.
     */
    @Inject
    public PluginPanel(
            final DMDircMBassador eventBus,
            @MainWindow final Window parentWindow,
            final PluginManager pluginManager,
            final DataLoaderWorkerFactory workerFactory,
            final IconManager iconManager,
            final CachingUpdateManager updateManager,
            @UserConfig final ConfigProvider userConfig) {
        super(parentWindow, workerFactory, eventBus);
        this.pluginManager = pluginManager;
        this.iconManager = iconManager;
        this.updateManager = updateManager;
        this.userConfig = userConfig;
        eventBus.subscribe(this);
        pluginManager.refreshPlugins();
        load();
    }

    @Override
    protected JTable populateList(final JTable table) {
        final List<PluginInfo> list = new ArrayList<>();
        final Collection<PluginInfo> sortedList = new ArrayList<>();
        list.addAll(pluginManager.getPluginInfos());
        Collections.sort(list);
        list.stream().filter(plugin -> plugin.getMetaData().getParent() == null).forEach(plugin -> {
            final List<PluginInfo> childList = new ArrayList<>();
            sortedList.add(plugin);
            plugin.getChildren().stream()
                    .filter(child -> !childList.contains(child))
                    .forEach(childList::add);
            Collections.sort(childList);
            sortedList.addAll(childList);
        });

        UIUtilities.invokeLater(() -> {
            ((DefaultTableModel) table.getModel()).setNumRows(0);
            for (final PluginInfo plugin : sortedList) {
                ((DefaultTableModel) table.getModel()).addRow(
                        new AddonCell[]{
                            new AddonCell(
                                    new AddonToggle(
                                            updateManager,
                                            userConfig,
                                            pluginManager,
                                            plugin),
                                    iconManager),});
            }
            table.repaint();
        });
        return table;
    }

    @Handler
    public void handlePluginRefresh(final PluginRefreshEvent event) {
        populateList(addonList);
    }

    @Override
    protected String getTypeName() {
        return "plugins";
    }

}

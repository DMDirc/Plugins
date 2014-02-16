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

package com.dmdirc.addons.windowstatus;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.windowstatus.WindowStatusModule.WindowStatusDomain;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.plugins.PluginInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Displays information related to the current window in the status bar.
 */
public class WindowStatusManager implements ConfigChangeListener, SelectionListener {

    /** Main frame. */
    private final MainFrame mainFrame;
    /** Status bar we're adding to. */
    private final SwingStatusBar statusBar;
    /** The panel we use in the status bar. */
    private final WindowStatusPanel panel;
    /** Identity controller to read settings from. */
    private final IdentityController identityController;
    /** Plugin settings domain. */
    private final String domain;
    /** Should we show the real name in queries? */
    private boolean showname;
    /** Should we show users without modes? */
    private boolean shownone;
    /** Prefix for users without modes. */
    private String nonePrefix;

    @Inject
    public WindowStatusManager(final MainFrame mainFrame, final SwingStatusBar statusBar,
            final IdentityController identityController,
            @WindowStatusDomain final String domain) {
        this.domain = domain;
        this.mainFrame = mainFrame;
        this.statusBar = statusBar;
        this.identityController = identityController;

        panel = UIUtilities.invokeAndWait(new Callable<WindowStatusPanel>() {

            /** {@inheritDoc} */
            @Override
            public WindowStatusPanel call() {
                return new WindowStatusPanel();
            }
        });
    }

    /**
     * Loads the plugin.
     */
    public void onLoad() {
        statusBar.addComponent(panel);
        mainFrame.addSelectionListener(this);
        identityController.getGlobalConfiguration().addChangeListener(domain, this);
        updateCache();
    }

    /**
     * Unloads the plugin.
     */
    public void onUnload() {
        mainFrame.removeSelectionListener(this);
        statusBar.removeComponent(panel);
    }

    /** {@inheritDoc} */
    @Override
    public void selectionChanged(final TextFrame window) {
        updateStatus(window == null ? null : window.getContainer());
    }

    /** Updates the cached config settings. */
    private void updateCache() {
        showname = identityController.getGlobalConfiguration()
                .getOptionBool(domain, "client.showname");
        shownone = identityController.getGlobalConfiguration()
                .getOptionBool(domain, "channel.shownone");
        nonePrefix = identityController.getGlobalConfiguration()
                .getOption(domain, "channel.noneprefix");
        updateStatus();
    }

    /** Update the window status using the current active window. */
    public void updateStatus() {
        final TextFrame active = mainFrame.getActiveFrame();

        if (active != null) {
            updateStatus(active.getContainer());
        }
    }

    /**
     * Update the window status using a given FrameContainer as the
     * active frame.
     *
     * @param current Window to use when adding status.
     */
    public void updateStatus(final FrameContainer current) {
        if (current == null) {
            return;
        }
        final StringBuffer textString = new StringBuffer();

        if (current instanceof Connection) {
            textString.append(((Connection) current).getAddress());
        } else if (current instanceof Channel) {
            final ChannelInfo chan = ((Channel) current).getChannelInfo();
            final Map<Integer, String> names = new HashMap<>();
            final Map<Integer, Integer> types = new HashMap<>();

            textString.append(chan.getName());
            textString.append(" - Nicks: ");
            textString.append(chan.getChannelClientCount());
            textString.append(" (");

            for (ChannelClientInfo client : chan.getChannelClients()) {
                String mode = client.getImportantModePrefix();
                final Integer im = client.getClient().getParser()
                        .getChannelUserModes().indexOf(mode);

                if (!names.containsKey(im)) {
                    if (mode.isEmpty()) {
                        if (shownone) {
                            mode = nonePrefix;
                        } else {
                            continue;
                        }
                    }
                    names.put(im, mode);
                }

                Integer count = types.get(im);

                if (count == null) {
                    count = Integer.valueOf(1);
                } else {
                    count++;
                }
                types.put(im, count);
            }

            boolean isFirst = true;

            for (Map.Entry<Integer, Integer> entry : types.entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    textString.append(' ');
                }
                textString.append(names.get(entry.getKey()));
                textString.append(entry.getValue());
            }

            textString.append(')');
        } else if (current instanceof Query) {
            final Query frame = (Query) current;

            textString.append(frame.getHost());
            if (showname && frame.getConnection().getParser() != null) {
                final ClientInfo client = frame.getConnection().getParser()
                        .getClient(frame.getHost());
                final String realname = client.getRealname();
                if (realname != null && !realname.isEmpty()) {
                    textString.append(" - ");
                    textString.append(client.getRealname());
                }
            }
        } else {
            textString.append("???");
        }
        panel.setText(textString.toString());
    }

    /**
     * Shows the configuration page.
     *
     * @param manager Prefs dialog manager to add settings to
     * @param pluginInfo Plugin info
     */
    public void showConfig(final PreferencesDialogModel manager,
            final PluginInfo pluginInfo) {
        final PreferencesCategory category = new PluginPreferencesCategory(
                pluginInfo, "Window status", "");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "channel.shownone", "Show 'none' count",
                "Should the count for users with no state be shown?",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                domain, "channel.noneprefix", "'None' count prefix",
                "The Prefix to use when showing the 'none' count",
                manager.getConfigManager(), manager.getIdentity()));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                domain, "client.showname", "Show real name",
                "Should the realname for clients be shown if known?",
                manager.getConfigManager(), manager.getIdentity()));

        manager.getCategory("Plugins").addSubCategory(category);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        updateCache();
    }
}

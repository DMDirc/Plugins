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

package com.dmdirc.addons.windowstatus;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesManager;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.plugins.Plugin;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.util.ReturnableThread;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Displays information related to the current window in the status bar.
 *
 * @author Shane 'Dataforce' McCormack
 */
public final class WindowStatusPlugin extends Plugin implements ActionListener, ConfigChangeListener {

    /** The panel we use in the status bar. */
    private final WindowStatusPanel panel;
    private boolean showname, shownone;
    private String nonePrefix;

    /** Creates a new instance of WindowStatusPlugin. */
    public WindowStatusPlugin() {
        super();

        panel = UIUtilities.invokeAndWait(
                new ReturnableThread<WindowStatusPanel>() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                setObject(new WindowStatusPanel());
            }
        });
    }

    /**
     * Called when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        ((SwingController) PluginManager.getPluginManager()
                .getPluginInfoByName("ui_swing").getPlugin()).getStatusBar().addComponent(panel);
        IdentityManager.getGlobalConfig().addChangeListener(getDomain(), this);
        updateCache();

        ActionManager.addListener(this, CoreActionType.CLIENT_FRAME_CHANGED);
    }

    /**
     * Called when this plugin is unloaded.
     */
    @Override
    public void onUnload() {
        ((SwingController) PluginManager.getPluginManager()
                .getPluginInfoByName("ui_swing").getPlugin()).getStatusBar().removeComponent(panel);
        ActionManager.removeListener(this);
    }

    /**
     * Process an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        if (type.equals(CoreActionType.CLIENT_FRAME_CHANGED)) {
            updateStatus((FrameContainer<?>) arguments[0]);
        }
    }

    private void updateCache() {
        showname = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "client.showname");
        shownone = IdentityManager.getGlobalConfig().getOptionBool(getDomain(), "channel.shownone");
        nonePrefix = IdentityManager.getGlobalConfig().getOption(getDomain(), "channel.noneprefix");
        updateStatus();
    }

    /**
     * Update the window status using the current active window.
     */
    public void updateStatus() {
        final FrameContainer<?> active = WindowManager.getActiveWindow();

        if (active != null) {
            updateStatus(active);
        }
    }

    /**
     * Update the window status using a given FrameContainer as the active frame.
     *
     * @param current Window to use when adding status.
     */
    public void updateStatus(final FrameContainer<?> current) {
        if (current == null) {
            return;
        }
        final StringBuffer textString = new StringBuffer();

        if (current instanceof Server) {
            final Server frame = (Server) current;

            textString.append(frame.getName());
        } else if (current instanceof Channel) {
            final Channel frame = (Channel) current;
            final ChannelInfo chan = frame.getChannelInfo();
            final Map<Integer, String> names = new HashMap<Integer, String>();
            final Map<Integer, Integer> types = new HashMap<Integer, Integer>();

            textString.append(chan.getName());
            textString.append(" - Nicks: " + chan.getChannelClientCount() + " (");

            for (ChannelClientInfo client : chan.getChannelClients()) {
                String mode = client.getImportantModePrefix();
                final Integer im = client.getClient().getParser().getChannelUserModes().indexOf(mode);

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

            for (Entry<Integer, Integer> entry : types.entrySet()) {
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
            if (showname && frame.getServer().getParser() != null) {
                final ClientInfo client = frame.getServer().getParser().getClient(frame.getHost());
                final String realname = client.getRealname();
                if (!realname.isEmpty()) {
                    textString.append(" - ");
                    textString.append(client.getRealname());
                }
            }
        } else {
            textString.append("???");
        }
        panel.setText(textString.toString());
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesManager manager) {
        final PreferencesCategory category = new PluginPreferencesCategory(
                getPluginInfo(), "Window status", "");

        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "channel.shownone", "Show 'none' count",
                "Should the count for users with no state be shown?"));
        category.addSetting(new PreferencesSetting(PreferencesType.TEXT,
                getDomain(), "channel.noneprefix", "'None' count prefix",
                "The Prefix to use when showing the 'none' count"));
        category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                getDomain(), "client.showname", "Show real name",
                "Should the realname for clients be shown if known?"));

        manager.getCategory("Plugins").addSubCategory(category);
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        updateCache();
    }

}

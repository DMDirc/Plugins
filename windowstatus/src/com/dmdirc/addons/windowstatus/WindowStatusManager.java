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
import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingWindowSelectedEvent;
import com.dmdirc.addons.ui_swing.injection.SwingEventBus;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.events.StatusBarComponentAddedEvent;
import com.dmdirc.events.StatusBarComponentRemovedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.plugins.PluginDomain;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

/**
 * Displays information related to the current window in the status bar.
 */
public class WindowStatusManager implements ConfigChangeListener {

    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** Identity controller to read settings from. */
    private final IdentityController identityController;
    /** Plugin settings domain. */
    private final String domain;
    /** The event bus to post events to. */
    private final DMDircMBassador eventBus;
    /** The swing event bus to register for events on. */
    private final DMDircMBassador swingEventBus;
    /** The panel we use in the status bar. */
    private WindowStatusPanel panel;
    /** Should we show the real name in queries? */
    private boolean showname;
    /** Should we show users without modes? */
    private boolean shownone;
    /** Prefix for users without modes. */
    private String nonePrefix;

    @Inject
    public WindowStatusManager(final ActiveFrameManager activeFrameManager,
            final IdentityController identityController,
            @PluginDomain(WindowStatusPlugin.class) final String domain,
            final DMDircMBassador eventBus,
            @SwingEventBus final DMDircMBassador swingEventBus) {
        this.domain = domain;
        this.activeFrameManager = activeFrameManager;
        this.identityController = identityController;
        this.eventBus = eventBus;
        this.swingEventBus = swingEventBus;
    }

    /**
     * Loads the plugin.
     */
    public void onLoad() {
        panel = UIUtilities.invokeAndWait(new Callable<WindowStatusPanel>() {

            @Override
            public WindowStatusPanel call() {
                return new WindowStatusPanel();
            }
        });
        eventBus.publishAsync(new StatusBarComponentAddedEvent(panel));
        swingEventBus.subscribe(this);
        identityController.getGlobalConfiguration().addChangeListener(domain, this);
        updateCache();
    }

    /**
     * Unloads the plugin.
     */
    public void onUnload() {
        swingEventBus.unsubscribe(this);
        eventBus.publishAsync(new StatusBarComponentRemovedEvent(panel));
        panel = null;
    }

    @Handler(invocation = EdtHandlerInvocation.class, delivery = Invoke.Asynchronously)
    public void selectionChanged(final SwingWindowSelectedEvent event) {
        if (event.getWindow().isPresent()) {
            updateStatus(event.getWindow().get().getContainer());
        }
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
        final TextFrame active = activeFrameManager.getActiveFrame();

        if (active != null) {
            updateStatus(active.getContainer());
        }
    }

    /**
     * Update the window status using a given FrameContainer as the active frame.
     *
     * @param current Window to use when adding status.
     */
    public void updateStatus(final FrameContainer current) {
        if (current == null) {
            return;
        }
        final String textString;

        if (current instanceof Connection) {
            textString = updateStatusConnection((Connection) current);
        } else if (current instanceof Channel) {
            textString = updateStatusChannel((Channel) current);
        } else if (current instanceof Query) {
            textString = updateStatusQuery((Query) current);
        } else {
            textString = "???";
        }
        if (panel != null) {
            panel.setText(textString);
        }
    }

    private String updateStatusConnection(final Connection connection) {
        return connection.getAddress();
    }

    private String updateStatusChannel(final Channel frame) {
        final StringBuilder textString = new StringBuilder();
        final ChannelInfo chan = frame.getChannelInfo();

        textString.append(chan.getName());
        textString.append(" - Nicks: ");
        textString.append(chan.getChannelClientCount());
        textString.append(" (");

        final String channelUserModes = ' ' + chan.getParser().getChannelUserModes();
        final int[] usersWithMode = new int[channelUserModes.length()];
        for (ChannelClientInfo client : chan.getChannelClients()) {
            final String mode = client.getImportantModePrefix();
            final int index = channelUserModes.indexOf(mode);
            usersWithMode[index]++;
        }

        boolean isFirst = true;
        for (int i = channelUserModes.length() - 1; i >= 0; i--) {
            final int count = usersWithMode[i];
            if (count > 0 && (shownone || i > 0)) {
                if (!isFirst) {
                    textString.append(' ');
                }
                final String name = i > 0 ?
                        Character.toString(channelUserModes.charAt(i)) : nonePrefix;
                textString.append(name).append(count);
                isFirst = false;
            }
        }

        textString.append(')');
        return textString.toString();
    }

    private String updateStatusQuery(final Query frame) {
        final StringBuilder textString = new StringBuilder();
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
        return textString.toString();
    }

    @Override
    public void configChanged(final String domain, final String key) {
        updateCache();
    }

}

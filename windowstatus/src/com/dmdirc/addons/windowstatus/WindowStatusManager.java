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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.addons.ui_swing.events.SwingWindowSelectedEvent;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.config.ConfigBinder;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.StatusBarComponentAddedEvent;
import com.dmdirc.events.StatusBarComponentRemovedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.PrivateChat;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.plugins.PluginDomain;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

/**
 * Displays information related to the current window in the status bar.
 */
public class WindowStatusManager {

    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** Config to read settings from. */
    private final ConfigBinder configBinder;
    /** The event bus to post events to. */
    private final DMDircMBassador eventBus;
    /** The swing event bus to register for events on. */
    private final SwingEventBus swingEventBus;
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
            @GlobalConfig final AggregateConfigProvider config,
            @PluginDomain(WindowStatusPlugin.class) final String domain,
            final DMDircMBassador eventBus,
            final SwingEventBus swingEventBus) {
        this.activeFrameManager = activeFrameManager;
        this.configBinder = config.getBinder().withDefaultDomain(domain);
        this.eventBus = eventBus;
        this.swingEventBus = swingEventBus;
    }

    /**
     * Loads the plugin.
     */
    public void onLoad() {
        panel = UIUtilities.invokeAndWait(WindowStatusPanel::new);
        eventBus.publishAsync(new StatusBarComponentAddedEvent(panel));
        swingEventBus.subscribe(this);
        configBinder.bind(this, WindowStatusManager.class);
        UIUtilities.invokeLater(this::updateStatus);
    }

    /**
     * Unloads the plugin.
     */
    public void onUnload() {
        swingEventBus.unsubscribe(this);
        eventBus.publishAsync(new StatusBarComponentRemovedEvent(panel));
        configBinder.unbind(this);
        panel = null;
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void selectionChanged(final SwingWindowSelectedEvent event) {
        event.getWindow().map(Window::getContainer).ifPresent(this::updateStatus);
    }

    /** Update the window status using the current active window. */
    public void updateStatus() {
        activeFrameManager.getActiveFrame().ifPresent(c -> updateStatus(c.getContainer()));
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
        if (panel == null) {
            return;
        }
        final String textString;

        if (current instanceof Connection) {
            textString = updateStatusConnection((Connection) current);
        } else if (current instanceof GroupChat) {
            textString = updateStatusChannel((GroupChat) current);
        } else if (current instanceof PrivateChat) {
            textString = updateStatusQuery((PrivateChat) current);
        } else {
            textString = "???";
        }
        panel.setText(textString);
    }

    private String updateStatusConnection(final Connection connection) {
        return connection.getAddress();
    }

    private String updateStatusChannel(final GroupChat frame) {
        final StringBuilder textString = new StringBuilder();

        textString.append(frame.getName());
        textString.append(" - Nicks: ");
        textString.append(frame.getUsers().size());
        textString.append(" (");

        final String channelUserModes = ' ' + frame.getConnection()
                .map(Connection::getUserModes).orElse("");
        final int[] usersWithMode = new int[channelUserModes.length()];
        frame.getUsers().forEach(user -> {
            final String mode = user.getImportantMode();
            final int index = channelUserModes.indexOf(mode);
            usersWithMode[index]++;
        });

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

    private String updateStatusQuery(final PrivateChat frame) {
        final StringBuilder textString = new StringBuilder();
        textString.append(frame.getHost());
        frame.getConnection().ifPresent(c -> {
            if (showname) {
                frame.getUser().getRealname().ifPresent(s -> textString.append(" - ").append(s));
            }
        });
        return textString.toString();
    }

    @ConfigBinding(key = "client.showname", invocation = EDTInvocation.class)
    public void handleShowName(final String value) {
        showname = Boolean.valueOf(value);
        updateStatus();
    }

    @ConfigBinding(key = "client.shownone", invocation = EDTInvocation.class)
    public void handleShowNone(final String value) {
        shownone = Boolean.valueOf(value);
        updateStatus();
    }

    @ConfigBinding(key = "client.noneprefix", invocation = EDTInvocation.class)
    public void handleShowPrefix(final String value) {
        nonePrefix = value;
        updateStatus();
    }
}

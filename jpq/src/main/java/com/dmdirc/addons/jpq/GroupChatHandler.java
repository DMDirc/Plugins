/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.jpq;

import com.dmdirc.config.binding.ConfigBinder;
import com.dmdirc.config.binding.ConfigBinding;
import com.dmdirc.events.ChannelJoinEvent;
import com.dmdirc.events.ChannelNickChangeEvent;
import com.dmdirc.events.ChannelPartEvent;
import com.dmdirc.events.ChannelQuitEvent;
import com.dmdirc.events.DisplayProperty;
import com.dmdirc.events.DisplayableEvent;
import com.dmdirc.interfaces.GroupChat;

import com.google.common.annotations.VisibleForTesting;

import net.engio.mbassy.listener.Handler;

/**
 * Handles {@link ChannelJoinEvent}, {@link ChannelPartEvent}, {@link ChannelQuitEvent} events and
 * hides them if the required.
 */
public class GroupChatHandler {

    private final GroupChat groupChat;
    private final ConfigBinder binder;
    private boolean hideEvents;
    private boolean hideNickChanges;

    public GroupChatHandler(final String domain, final GroupChat groupChat) {
        this.groupChat = groupChat;
        binder = groupChat.getWindowModel().getConfigManager().getBinder()
                .withDefaultDomain(domain);
    }

    /**
     * Loads this handler, adds required listeners and bindings.
     */
    public void load() {
        groupChat.getEventBus().subscribe(this);
        binder.bind(this, GroupChatHandler.class);
    }

    /**
     * Unloads this handler, removes required listeners and bindings.
     */
    public void unload() {
        groupChat.getEventBus().unsubscribe(this);
        binder.unbind(this);
    }

    @VisibleForTesting
    @ConfigBinding(key = "hidejpq")
    void handleSettingChange(final boolean value) {
        hideEvents = value;
    }

    @VisibleForTesting
    @ConfigBinding(key = "hidenickchanges")
    void handleSettingChangeNickname(final boolean value) {
        hideNickChanges = value;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @VisibleForTesting
    @Handler
    void handleJoin(final ChannelJoinEvent event) {
        if (event.getChannel().equals(groupChat)) {
            hideEvent(event);
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @VisibleForTesting
    @Handler
    void handlePart(final ChannelPartEvent event) {
        if (event.getChannel().equals(groupChat)) {
            hideEvent(event);
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @VisibleForTesting
    @Handler
    void handleQuit(final ChannelQuitEvent event) {
        if (event.getChannel().equals(groupChat)) {
            hideEvent(event);
        }
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @VisibleForTesting
    @Handler
    void handleNickChange(final ChannelNickChangeEvent event) {
        if (event.getChannel().equals(groupChat) && hideNickChanges) {
            event.setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
        }
    }

    private void hideEvent(final DisplayableEvent event) {
        if (hideEvents) {
            event.setDisplayProperty(DisplayProperty.DO_NOT_DISPLAY, true);
        }
    }
}

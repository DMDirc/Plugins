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

package com.dmdirc.addons.nickkeep;

import com.dmdirc.config.profiles.Profile;
import com.dmdirc.events.ChannelNickChangeEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.EventBus;

import java.util.Optional;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

/**
 * Provides Nick Keep support in DMDirc.
 */
public class NickKeepManager {

    private final EventBus eventBus;

    @Inject
    public NickKeepManager(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void load() {
        eventBus.subscribe(this);
    }

    public void unload() {
        eventBus.unsubscribe(this);
    }

    @Handler
    public void handleNickChange(final ChannelNickChangeEvent event) {
        final Optional<Connection> connection = event.getChannel().getConnection();
        if (!connection.isPresent()) {
            //No point carrying on without a connection
            return;
        }
        final Optional<String> currentNickname = connection.map(Connection::getNickname)
                .orElse(Optional.empty());
        if (!currentNickname.isPresent()) {
            //No point carrying on if we don't know our own nickname
            return;
        }
        final Optional<String> desiredNickname = connection.map(Connection::getProfile)
                .map(Profile::getNicknames).map(c -> c.stream().findFirst())
                .orElse(Optional.empty());
        if (!desiredNickname.isPresent()) {
            //No point carrying on without a desired nickname
            return;
        }
        if (!event.getOldNick().equals(desiredNickname.get())) {
            //This isnt the nickname we're looking for, move along
            return;
        }
        if (!currentNickname.equals(desiredNickname)) {
            connection.ifPresent(c -> c.setNickname(desiredNickname.get()));
        }
    }
}

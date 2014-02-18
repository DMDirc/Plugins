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

package com.dmdirc.addons.contactlist;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.interfaces.NicklistListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.util.Collection;

/**
 * Listens for contact list related events.
 */
public class ContactListListener implements NicklistListener,
        ActionListener, FrameCloseListener {

    /** The channel this listener is for. */
    private final Channel channel;

    /**
     * Creates a new ContactListListener for the specified channel.
     *
     * @param channel The channel to show a contact list for
     */
    public ContactListListener(final Channel channel) {
        this.channel = channel;
    }

    /**
     * Adds all necessary listeners for this contact list listener to function.
     */
    public void addListeners() {
        channel.addNicklistListener(this);
        channel.addCloseListener(this);
        ActionManager.getActionManager().registerListener(this, CoreActionType.CHANNEL_USERAWAY,
                CoreActionType.CHANNEL_USERBACK);
    }

    /**
     * Removes the listeners added by {@link #addListeners()}.
     */
    public void removeListeners() {
        channel.removeNicklistListener(this);
        channel.removeCloseListener(this);
        ActionManager.getActionManager().unregisterListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void clientListUpdated(final Collection<ChannelClientInfo> clients) {
        for (ChannelClientInfo client : clients) {
            clientAdded(client);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clientListUpdated() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void clientAdded(final ChannelClientInfo client) {
        final Query query = channel.getConnection().
                getQuery(client.getClient().getNickname(), false);

        query.setIcon("query-" + client.getClient().getAwayState().name().toLowerCase());
    }

    /** {@inheritDoc} */
    @Override
    public void clientRemoved(final ChannelClientInfo client) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (arguments[0] == channel) {
            clientAdded((ChannelClientInfo) arguments[1]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final FrameContainer window) {
        removeListeners();
    }

}

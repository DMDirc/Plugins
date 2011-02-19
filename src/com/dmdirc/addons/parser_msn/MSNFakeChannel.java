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

package com.dmdirc.addons.parser_msn;

import com.dmdirc.parser.common.BaseChannelInfo;
import com.dmdirc.parser.common.ChannelListModeItem;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;

import java.util.Collection;
import java.util.Collections;

/**
 * A 'fake' local channel used to display contact lists.
 */
public class MSNFakeChannel extends BaseChannelInfo {

    /**
     * Creates a new fake channel belonging to the specified parser and
     * with the given name.
     *
     * @param parser The parser that owns this channel
     * @param name The name of the channel
     */
    public MSNFakeChannel(final Parser parser, final String name) {
        super(parser, name);
    }

    /** {@inheritDoc} */
    @Override
    public void setTopic(final String topic) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public String getTopic() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public long getTopicTime() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getTopicSetter() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getModes() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public String getMode(final char mode) {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public Collection<ChannelListModeItem> getListMode(final char mode) {
        return Collections.<ChannelListModeItem>emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public void part(final String reason) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void sendWho() {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void alterMode(final boolean add,
            final Character mode, final String parameter) {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void flushModes() {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void requestListModes() {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    public ChannelClientInfo getChannelClient(final ClientInfo client) {
        return getClient(client.getNickname());
    }

    /** {@inheritDoc} */
    @Override
    public ChannelClientInfo getChannelClient(final String client,
            final boolean create) {
        final String[] parts = getParser().parseHostmask(client);
        if (create && getClient(parts[0]) == null) {
            return new MSNChannelClientInfo(this, getParser().getClient(client));
        }
        return getClient(parts[0]);
    }

    /**
     * Replaces the clients in this channel with a the new list of clients.
     *
     * @param clients client list
     */
    public void replaceContacts(final Collection<ClientInfo> clients) {
        for (ChannelClientInfo client : getChannelClients()) {
            removeClient(client.getClient().getNickname());
        }
        for (ClientInfo client : clients) {
            addClient(client.getNickname(), new MSNChannelClientInfo(this,
                    client));
        }

        getParser().getCallbackManager().getCallbackType(
                ChannelNamesListener.class).call(this);
    }

    /**
     * Adds the specified clients to this channel
     *
     * @param clients client list
     */
    public void addContacts(final Collection<ClientInfo> clients) {
        for (ClientInfo client : clients) {
            addClient(client.getNickname(), new MSNChannelClientInfo(this,
                    client));
        }

        getParser().getCallbackManager().getCallbackType(
                ChannelNamesListener.class).call(this);
    }

    /**
     * Removes the specified clients from this channel
     *
     * @param clients client list
     */
    public void removeContacts(final Collection<ClientInfo> clients) {
        for (ClientInfo client : clients) {
            removeClient(client.getNickname());
        }

        getParser().getCallbackManager().getCallbackType(
                ChannelNamesListener.class).call(this);
    }

}

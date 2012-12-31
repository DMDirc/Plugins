/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.parser.common.AwayState;
import com.dmdirc.parser.common.ParserError;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.callbacks.AwayStateListener;
import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
import com.dmdirc.parser.interfaces.callbacks.ConnectErrorListener;
import com.dmdirc.parser.interfaces.callbacks.ErrorInfoListener;
import com.dmdirc.parser.interfaces.callbacks.OtherAwayStateListener;
import com.dmdirc.parser.interfaces.callbacks.PasswordRequiredListener;
import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
import com.dmdirc.parser.interfaces.callbacks.ServerNoticeListener;
import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.jml.MsnContact;
import net.sf.jml.MsnList;
import net.sf.jml.MsnMessenger;
import net.sf.jml.MsnSwitchboard;
import net.sf.jml.MsnUserStatus;
import net.sf.jml.event.MsnAdapter;
import net.sf.jml.exception.IncorrectPasswordException;
import net.sf.jml.exception.LoginException;
import net.sf.jml.message.MsnInstantMessage;
import net.sf.jml.message.MsnSystemMessage;
import net.sf.jml.message.MsnUnknownMessage;

/**
 * Listener object acting on events from the MSN connection.
 */
public class MSNListener extends MsnAdapter {

    /** MSN Parser. */
    private final MSNParser parser;
    /** Switchboard ID. */
    private final Object object = new Object();

    /**
     * Creates a new MSN listener.
     *
     * @param parser Parent parser
     */
    public MSNListener(final MSNParser parser) {
        this.parser = parser;
    }

    /** {@inheritDoc} */
    @Override
    public void contactAddCompleted(final MsnMessenger mm, final MsnContact mc,
            final MsnList ml) {
        parser.addClient(mc);
        parser.addClients(parser.getFakeChannel(), Arrays.asList(
                new ClientInfo[]{ parser.getClient(mc), }));
    }

    /** {@inheritDoc} */
    @Override
    public void contactListSyncCompleted(final MsnMessenger mm) {
        final Collection<MsnContact> contacts = Arrays.asList(
                mm.getContactList().getContacts());
        final List<ClientInfo> clients = new ArrayList<ClientInfo>();
        for (MsnContact contact : contacts) {
            parser.addClient(contact);
            clients.add(parser.getClient(contact));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void contactStatusChanged(final MsnMessenger mm,
            final MsnContact mc) {
        if (mc.getStatus() != MsnUserStatus.OFFLINE) {
            parser.addClients(parser.getFakeChannel(), Arrays.asList(
                    new ClientInfo[]{ parser.getClient(mc), }));
        } else if (mc.getStatus() == MsnUserStatus.OFFLINE) {
            parser.removeClients(parser.getFakeChannel(), Arrays.asList(
                    new ClientInfo[]{ parser.getClient(mc), }));
        }
        final boolean isBack = mc.getStatus().equals(MsnUserStatus.ONLINE);
        parser.getCallbackManager().getCallbackType(
                OtherAwayStateListener.class).call(parser.getClient(mc),
                isBack ? AwayState.AWAY : AwayState.HERE,
                isBack ? AwayState.HERE : AwayState.AWAY);
    }

    /** {@inheritDoc} */
    @Override
    public void exceptionCaught(final MsnMessenger mm, final Throwable thrwbl) {
        final ParserError error = new ParserError(ParserError.ERROR_ERROR,
                thrwbl.getMessage(), "");
        error.setException(new Exception(thrwbl)); //NOPMD
        if (thrwbl instanceof LoginException) {
            parser.getCallbackManager().getCallbackType(
                    ConnectErrorListener.class).call(error);
        } else if (thrwbl instanceof IncorrectPasswordException) {
            parser.getCallbackManager().getCallbackType(
                    PasswordRequiredListener.class).call(error);
        } else if (thrwbl instanceof ConnectException) {
            parser.getCallbackManager().getCallbackType(
                    SocketCloseListener.class).call(error);
            parser.removeMSNParser();
        } else {
            parser.getCallbackManager().getCallbackType(
                    ErrorInfoListener.class).call(error);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void instantMessageReceived(final MsnSwitchboard ms,
            final MsnInstantMessage mim, final MsnContact mc) {
        parser.getCallbackManager().getCallbackType(
                PrivateMessageListener.class).call(mim.getContent(),
                mc.getEmail().getEmailAddress());
    }

    /** {@inheritDoc} */
    @Override
    public void loginCompleted(final MsnMessenger mm) {
        mm.newSwitchboard(object);
        parser.getCallbackManager().getCallbackType(ServerReadyListener.class)
                .call();
        final ChannelInfo channel = parser.getFakeChannel();
        if (channel != null) {
            parser.getCallbackManager().getCallbackType(
                    ChannelSelfJoinListener.class).call(channel);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void logout(final MsnMessenger mm) {
        parser.getCallbackManager().getCallbackType(SocketCloseListener.class)
                .call();
    }

    /** {@inheritDoc} */
    @Override
    public void offlineMessageReceived(final String body,
            final String contentType, final String encoding,
            final MsnContact mc) {
        parser.getCallbackManager().getCallbackType(
                PrivateMessageListener.class).call(body,
                mc.getEmail().getEmailAddress());
    }

    /** {@inheritDoc} */
    @Override
    public void ownerStatusChanged(final MsnMessenger mm) {
        if (mm.getOwner().getStatus() == MsnUserStatus.ONLINE) {
            parser.getCallbackManager().getCallbackType(AwayStateListener.class)
                    .call(AwayState.AWAY, AwayState.HERE, null);
        } else {
            parser.getCallbackManager().getCallbackType(AwayStateListener.class)
                    .call(AwayState.HERE, AwayState.AWAY, mm.getOwner()
                    .getStatus().getDisplayStatus());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void systemMessageReceived(final MsnMessenger mm,
            final MsnSystemMessage msm) {
        if (msm.getContent() != null) {
            parser.getCallbackManager().getCallbackType(
                    ServerNoticeListener.class).call(msm.getContent(), "MSN");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unknownMessageReceived(final MsnSwitchboard ms,
            final MsnUnknownMessage mum, final MsnContact mc) {
        parser.getCallbackManager().getCallbackType(
                ServerNoticeListener.class).call("Unknown message: "
                + mum.getContent(), "MSN");
    }
}

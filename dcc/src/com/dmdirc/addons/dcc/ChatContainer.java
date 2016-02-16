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

package com.dmdirc.addons.dcc;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.dcc.events.DccChatMessageEvent;
import com.dmdirc.addons.dcc.events.DccChatSelfMessageEvent;
import com.dmdirc.addons.dcc.events.DccChatSocketClosedEvent;
import com.dmdirc.addons.dcc.events.DccChatSocketOpenedEvent;
import com.dmdirc.addons.dcc.io.DCCChat;
import com.dmdirc.events.CommandErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.core.components.WindowComponent;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.util.Arrays;

import javax.annotation.Nullable;

/**
 * This class links DCC Chat objects to a window.
 */
public class ChatContainer extends DCCFrameContainer implements DCCChatHandler {

    /** The DCCChat object we are a window for. */
    private final DCCChat dccChat;
    /** My Nickname. */
    private final String nickname;
    /** Other Nickname. */
    private final String otherNickname;
    /** Event bus to post events on. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of DCCChatWindow with a given DCCChat object.
     *
     * @param parent              The parent of this frame container, if any.
     * @param dcc                 The DCCChat object this window wraps around
     * @param configManager       Config manager
     * @param commandController   The controller to use in the command parser.
     * @param title               The title of this window
     * @param nick                My Current Nickname
     * @param targetNick          Nickname of target
     * @param tabCompleterFactory The factory to use to create tab completers.
     * @param eventBus            The bus to dispatch events on.
     */
    public ChatContainer(
            @Nullable final WindowModel parent,
            final DCCChat dcc,
            final AggregateConfigProvider configManager,
            final BackBufferFactory backBufferFactory,
            final CommandController commandController,
            final String title,
            final String nick,
            final String targetNick,
            final TabCompleterFactory tabCompleterFactory,
            final DMDircMBassador eventBus) {
        super(parent, title, "dcc-chat-inactive", configManager, backBufferFactory,
                new DCCCommandParser(configManager, commandController, eventBus),
                tabCompleterFactory,
                eventBus,
                Arrays.asList(
                        WindowComponent.TEXTAREA.getIdentifier(),
                        WindowComponent.INPUTFIELD.getIdentifier()));
        dccChat = dcc;
        dcc.setHandler(this);
        nickname = nick;
        otherNickname = targetNick;
        this.eventBus = eventBus;
        initBackBuffer();
    }

    /**
     * Get the DCCChat Object associated with this window.
     *
     * @return The DCCChat Object associated with this window
     */
    public DCCChat getDCC() {
        return dccChat;
    }

    @Override
    public void sendLine(final String line) {
        if (dccChat.isWriteable()) {
            eventBus.publishAsync(new DccChatSelfMessageEvent(this, nickname, line));
            dccChat.sendLine(line);
        } else {
            eventBus.publishAsync(new CommandErrorEvent(this, "Socket is closed."));
        }
    }

    @Override
    public void handleChatMessage(final DCCChat dcc, final String message) {
        eventBus.publishAsync(new DccChatMessageEvent(this, otherNickname, message));
    }

    @Override
    public void socketClosed(final DCCChat dcc) {
        eventBus.publishAsync(new DccChatSocketClosedEvent(this));
        if (!isWindowClosing()) {
            setIcon("dcc-chat-inactive");
        }
    }

    @Override
    public void socketOpened(final DCCChat dcc) {
        eventBus.publishAsync(new DccChatSocketOpenedEvent(this));
        setIcon("dcc-chat-active");
    }

    @Override
    public void close() {
        super.close();
        dccChat.close();
    }

}

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

package com.dmdirc.addons.dcc.events;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.dcc.ChatContainer;

/**
 * Fired when a DCC chat message is received.
 */
public class DccChatMessageEvent extends DccDisplayableEvent {

    private final ChatContainer chatWindow;
    private final String nickname;
    private final String message;

    public DccChatMessageEvent(final ChatContainer chatWindow, final String nickname,
            final String message) {
        this.chatWindow = chatWindow;
        this.nickname = nickname;
        this.message = message;
    }

    public ChatContainer getChatWindow() {
        return chatWindow;
    }

    public String getNickname() {
        return nickname;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public FrameContainer getSource() {
        return chatWindow;
    }

}

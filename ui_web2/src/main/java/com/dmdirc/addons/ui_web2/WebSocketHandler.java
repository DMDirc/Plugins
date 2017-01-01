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

package com.dmdirc.addons.ui_web2;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Handles websocket connections.
 */
@WebSocket
public class WebSocketHandler {

    /** Controller to use. */
    @SuppressWarnings("StaticNonFinalField")
    private static WebSocketController controller;

    /**
     * Sets the controller that manages ALL instances of {@link WebSocketHandler}.
     *
     * @param controller Controller to use.
     */
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void setController(final WebSocketController controller) {
        WebSocketHandler.controller = controller;
    }

    @OnWebSocketConnect
    public void connected(final Session session) {
        controller.sessionConnected(session);
    }

    @OnWebSocketClose
    public void closed(final Session session, final int statusCode, final String reason) {
        controller.sessionClosed(session, statusCode, reason);
    }

    @OnWebSocketMessage
    public void message(final Session session, final String message) {
        controller.messageReceived(session, message);
    }

    public static void sendMessage(final Session session, final String message) throws IOException {
        session.getRemote().sendString(message);
    }

}

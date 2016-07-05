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

package com.dmdirc.addons.ui_web2;

import com.dmdirc.DMDircMBassador;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jetty.websocket.api.Session;

/**
 * Manages events raised by the {@link WebSocketHandler}.
 *
 * <p>This serves as a bridge between the {@link WebSocketHandler}, which cannot have dependencies
 * passed in sanely due to the framework, and the rest of the plugin/client.
 */
@Singleton
public class WebSocketController {

    private final Collection<Session> sessions = new CopyOnWriteArrayList<>();
    private final Object sessionLock = new Object();
    private final AtomicBoolean subscribed = new AtomicBoolean(false);
    private final DMDircMBassador eventBus;
    private final InitialStateProducer initialStateProducer;

    @Inject
    public WebSocketController(final DMDircMBassador eventBus, final InitialStateProducer initialStateProducer) {
        this.eventBus = eventBus;
        this.initialStateProducer = initialStateProducer;
    }

    /**
     * Handles a session connected event raised by a {@link WebSocketHandler}.
     *
     * @param session The session that is now connected.
     */
    void sessionConnected(final Session session) {
        synchronized (sessionLock) {
            if (!subscribed.getAndSet(true)) {
                eventBus.subscribe(this);
            }

            sessions.add(session);
        }

        sendMessage(session, initialStateProducer.getInitialState());
    }

    /**
     * Handles a session closed event raised by a {@link WebSocketHandler}.
     *
     * @param session The session that is now closed.
     * @param statusCode The status code returned.
     * @param reason The reason for quitting.
     */
    void sessionClosed(final Session session, final int statusCode, final String reason) {
        synchronized (sessionLock) {
            sessions.remove(session);
            if (sessions.isEmpty() && subscribed.getAndSet(false)) {
                eventBus.unsubscribe(this);
            }
        }
    }

    /**
     * Handles a message received event raised by a {@link WebSocketHandler}.
     *
     * @param session The session that the message is sent on.
     * @param message The message that was received.
     */
    void messageReceived(final Session session, final String message) {
        // Echo the message back for testing
        sendMessage(session, message);
    }

    /**
     * Sends a message to a specific session.
     *
     * @param session The session to send a message to.
     * @param message The message to be sent.
     */
    private void sendMessage(final Session session, final String message) {
        try {
            WebSocketHandler.sendMessage(session, message);
        } catch (IOException ex) {
            // TODO: Raise an error...
        }
    }

    /**
     * Sends a message to all connected sessions.
     *
     * @param message The message to be sent.
     */
    private void sendMessage(final String message) {
        sessions.forEach(s -> sendMessage(s, message));
    }

}

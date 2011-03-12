/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.addons.ui_web;

import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
import com.dmdirc.ui.interfaces.Window;

import java.util.LinkedList;
import java.util.List;

import org.mortbay.util.ajax.Continuation;

/**
 * Represents a single connected client.
 */
public class Client {

    /** The timestamp that the client was last seen. */
    private long lastSeenTime = System.currentTimeMillis();

    /** The Jetty continuation currently in use by the client, if any. */
    private Continuation continuation;

    /** The IP address of the client. */
    private final String ip;

    /** An ordered list of events to be sent to the client */
    private final List<Event> events = new LinkedList<Event>();

    /**
     * Creates a new client for the specified controller.
     *
     * @param controller The controller that this client is connected to
     * @param ip The IP address of this client
     */
    public Client(final WebInterfaceUI controller, final String ip) {
        events.add(new Event("statusbar",
                "Welcome to the DMDirc web interface"));

        this.ip = ip;

        final List<Window> added = new LinkedList<Window>();
        final List<Window> queued
                = new LinkedList<Window>(WebWindow.getWindows());

        while (!queued.isEmpty()) {
            final Window window = queued.remove(0);
            final Window parent = controller.getWindowManager()
                    .getWindow(window.getContainer().getParent());

            if (parent == null) {
                events.add(new Event("newwindow", window));
                added.add(window);
            } else if (added.contains(parent)) {
                events.add(new Event("newchildwindow",
                        new Object[]{parent, window}));
                added.add(window);
            } else {
                queued.add(window);
            }
        }
    }

    /**
     * Retrieves the IP address of this client.
     *
     * @return The client's IP address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Gets the time in milliseconds that has elapsed since this client was
     * last seen.
     *
     * @return The time since the client was last seen, in milliseconds
     */
    public long getTime() {
        return System.currentTimeMillis() - lastSeenTime;
    }

    /**
     * Retrieves the mutex that should be synchronised on when performing
     * a continuation.
     *
     * @return This client's mutex
     */
    public Object getMutex() {
        return events;
    }

    /**
     * Sets the continuation that this client is using.
     *
     * @param continuation This client's new continuation
     */
    public void setContinuation(final Continuation continuation) {
        this.continuation = continuation;
    }

    /**
     * Updates the 'last seen time' of this client to be the current time.
     */
    public void touch() {
        lastSeenTime = System.currentTimeMillis();
    }

    /**
     * Adds a new event to this client's event queue.
     *
     * @param event The event to be added
     */
    public void addEvent(final Event event) {
        synchronized (events) {
            events.add(event);

            if (continuation != null) {
                continuation.resume();
            }
        }
    }

    /**
     * Retrieves all queued events for this client and clears the queue.
     *
     * @return This client's queued events
     */
    public List<Event> retrieveEvents() {
        synchronized (events) {
            final List<Event> res = new LinkedList<Event>(events);
            events.clear();
            return res;
        }
    }

    /**
     * Gets the number of events that are waiting for this client.
     *
     * @return The current number of queued events
     */
    public int getEventCount() {
        return events.size();
    }

}

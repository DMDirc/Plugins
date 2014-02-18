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

package com.dmdirc.addons.parser_xmpp;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.Presence.Type;

/**
 * Represents a single endpoint of one contact.
 */
public class XmppEndpoint implements Comparable<XmppEndpoint> {

    /** The mode of the last presence update. */
    private Presence.Mode mode;
    /** The type of the last presence update. */
    private Presence.Type type;
    /** The status from the last presence update. */
    private String status;
    /** The priority of this endpoint. */
    private int priority;

    /**
     * Gets the mode of this endpoint.
     *
     * @return The last seen presence mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the mode of this endpoint.
     *
     * @param mode The most recently received presence mode
     */
    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets the status of this endpoint.
     *
     * @return The last seen presence status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of this endpoint.
     *
     * @param status The most recently received presence status
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    /**
     * Gets the type of this endpoint.
     *
     * @return The last seen presence type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this endpoint.
     *
     * @param type The most recently received presence type
     */
    public void setType(final Type type) {
        this.type = type;
    }

    /**
     * Gets the priority of this endpoint.
     *
     * @return The priority of this endpoint
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of this endpoint.
     *
     * @param priority The new priority for this endpoint
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }

    /**
     * Gets a user-friendly string describing this endpoint's presence.
     *
     * @return The presence of this endpoint in a user-friendly format
     */
    public String getPresence() {
        final String statusText = (status == null || status.isEmpty()) ? "" : " (" + status + ")";

        if (type == Type.unavailable) {
            return "Unavailable" + statusText;
        } else if (mode == null) {
            return "Available" + statusText;
        } else {
            return getFriendlyMode(mode) + statusText;
        }
    }

    /**
     * Returns a friendly representation of the specified presence mode.
     *
     * @param mode The mode being represented
     *
     * @return A user-friendly string corresponding to the mode
     */
    private static String getFriendlyMode(final Mode mode) {
        switch (mode) {
            case available:
                return "Available";
            case away:
                return "Away";
            case chat:
                return "Chatty";
            case dnd:
                return "Do not disturb";
            case xa:
                return "Extended Away";
            default:
                return mode.toString();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(final XmppEndpoint o) {
        return o.getPriority() - priority;
    }

}

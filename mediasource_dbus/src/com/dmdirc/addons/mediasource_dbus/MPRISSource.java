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

package com.dmdirc.addons.mediasource_dbus;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceState;

import java.util.List;
import java.util.Map;

/**
 * A media source for anything that's compatible with MRPIS.
 */
public class MPRISSource implements MediaSource {

    /** The media source manager. */
    private final DBusMediaSourceManager source;
    /** The service name. */
    private final String service;
    /** The name of the source. */
    private final String name;
    /** A temporary cache of track data. */
    private Map<String, String> data;

    /**
     * Creates a new MPRIS source for the specified service name.
     *
     * @param source  The manager which owns this source
     * @param service The service name of the MRPIS service
     */
    public MPRISSource(final DBusMediaSourceManager source, final String service) {
        this.source = source;
        this.service = service;

        final String info = getFirstValue("org.mpris.MediaPlayer2.Identity");

        if (info.isEmpty()) {
            throw new IllegalArgumentException("No service with that name found");
        }

        this.name = info.replace(' ', '_');
    }

    /**
     * Get the first line of the output for a dbus call to the given function against this service
     * in the /org/mpris/MediaPlayer2 obejct.
     *
     * @param function Function to get data for.
     *
     * @return First line of output.
     */
    protected String getFirstValue(final String function) {
        final List<String> info = source.doDBusCall("org.mpris." + service,
                "/org/mpris/MediaPlayer2", function);

        return info.isEmpty() ? "" : info.get(0);
    }

    @Override
    public MediaSourceState getState() {
        final String status = getStatus();

        if (status == null) {
            data = null;
            return MediaSourceState.CLOSED;
        }

        data = getTrackInfo();

        if (status.equalsIgnoreCase("Playing")) {
            return MediaSourceState.PLAYING;
        } else if (status.equalsIgnoreCase("Paused")) {
            return MediaSourceState.PAUSED;
        } else if (status.equalsIgnoreCase("Stopped")) {
            return MediaSourceState.STOPPED;
        } else {
            return MediaSourceState.NOTKNOWN;
        }
    }

    @Override
    public String getAppName() {
        return name;
    }

    /**
     * Utility method to return the value of the specified key if it exists, or "Unknown" if it
     * doesn't.
     *
     * @param key The key to be retrieved
     *
     * @return The value of the specified key or "Unknown".
     */
    protected String getData(final String key) {
        return data == null || !data.containsKey(key) || data.get(key) == null
                ? "Unknown" : data.get(key);
    }

    @Override
    public String getArtist() {
        return getData("xesam:artist");
    }

    @Override
    public String getTitle() {
        return getData("xesam:title");
    }

    @Override
    public String getAlbum() {
        return getData("xesam:album");
    }

    @Override
    public String getLength() {
        try {
            final Long len = Long.parseLong(getData("mpris:length"));
            return duration(len / 1000);
        } catch (final NumberFormatException nfe) {
            return "Unknown";
        }
    }

    @Override
    public String getTime() {
        try {
            final String position = getFirstValue("org.mpris.MediaPlayer2.Player.Position");
            final Long len = Long.parseLong(position);
            if (len == 0) {
                return "Unknown";
            } else {
                return duration(len / 1000);
            }
        } catch (final NumberFormatException nfe) {
            return "Unknown";
        }
    }

    @Override
    public String getFormat() {
        return "Unknown";
    }

    @Override
    public String getBitrate() {
        return "Unknown";
    }

    /**
     * Retrieves a map of track information from the service.
     *
     * @return A map of metadata returned by the MPRIS service
     */
    protected Map<String, String> getTrackInfo() {
        final List<String> list = source.doDBusCall("org.mpris." + service,
                "/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player.Metadata");
        return source.parseDictionary(list);
    }

    /**
     * Retrieves the 'status' result from the MPRIS service.
     *
     * @return The returned status or null if the service isn't running
     */
    protected String getStatus() {
        if (getFirstValue("org.mpris.MediaPlayer2.Identity").isEmpty()) {
            return null;
        }

        return getFirstValue("org.mpris.MediaPlayer2.Player.PlaybackStatus");
    }

    /**
     * Get the duration in seconds as a string.
     *
     * @param secondsInput Input to get duration for
     *
     * @return Duration as a string
     */
    private String duration(final long secondsInput) {
        final StringBuilder result = new StringBuilder();
        final long hours = secondsInput / 3600;
        final long minutes = secondsInput / 60 % 60;
        final long seconds = secondsInput % 60;

        if (hours > 0) {
            if (hours < 10) {
                result.append('0');
            }

            result.append(hours).append(':');
        }

        if (minutes < 10) {
            result.append('0');
        }

        result.append(minutes).append(':');

        if (seconds < 10) {
            result.append('0');
        }

        result.append(seconds);

        return result.toString();
    }

}

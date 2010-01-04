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

package com.dmdirc.addons.mediasource_dbus;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceState;

import java.util.List;
import java.util.Map;

/**
 * A media source for Banshee.
 *
 * @author chris
 */
public class BansheeSource implements MediaSource {

    /** The dbus service name. */
    private static final String SERVICE = "org.bansheeproject.Banshee";
    /** The dbus interface name. */
    private static final String IFACE = "/org/bansheeproject/Banshee/PlayerEngine";
    /** The method to get the current state. */
    private static final String STATE = "org.bansheeproject.Banshee.PlayerEngine.GetCurrentState";
    /** The method to get the current track. */
    private static final String TRACK = "org.bansheeproject.Banshee.PlayerEngine.GetCurrentTrack";
    /** The method to get the current position. */
    private static final String POSITION = "org.bansheeproject.Banshee.PlayerEngine.GetPosition";

    /** The media source that owns this source. */
    private final DBusMediaSource source;
    /** A cache of track information. */
    private Map<String, String> trackInfo;

    public BansheeSource(final DBusMediaSource source) {
        this.source = source;
    }

    /** {@inheritDoc} */
    @Override
    public MediaSourceState getState() {
        final List<String> res = source.doDBusCall(SERVICE, IFACE, STATE);
        
        if (res.isEmpty()) {
            trackInfo = null;
            return MediaSourceState.CLOSED;
        } else if ("playing".equals(res.get(0))) {
            trackInfo = getTrackInfo();
            return MediaSourceState.PLAYING;
        } else if ("paused".equals(res.get(0))) {
            trackInfo = getTrackInfo();
            return MediaSourceState.PAUSED;
        } else if ("idle".equals(res.get(0))) {
            trackInfo = getTrackInfo();
            return MediaSourceState.STOPPED;
        } else {
            trackInfo = null;
            return MediaSourceState.NOTKNOWN;
        }
    }

    /**
     * Retrieves a map of track information from the dbus service.
     *
     * @return A map of track information
     */
    protected Map<String, String> getTrackInfo() {
        return DBusMediaSource.parseDictionary(source.doDBusCall(SERVICE, IFACE, TRACK));
    }

    /** {@inheritDoc} */
    @Override
    public String getAppName() {
        return "Banshee";
    }

    /** {@inheritDoc} */
    @Override
    public String getArtist() {
        return trackInfo == null ? "Unknown" : trackInfo.get("artist");
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return trackInfo == null ? "Unknown" : trackInfo.get("name");
    }

    /** {@inheritDoc} */
    @Override
    public String getAlbum() {
        return trackInfo == null ? "Unknown" : trackInfo.get("album");
    }

    /** {@inheritDoc} */
    @Override
    public String getLength() {
        return trackInfo == null ? "Unknown" : duration((long)
                Double.parseDouble(trackInfo.get("length")));
    }

    /** {@inheritDoc} */
    @Override
    public String getTime() {
        return duration((long) Double.parseDouble(source.doDBusCall(SERVICE,
                IFACE, POSITION).get(0)) / 1000);
    }

    /** {@inheritDoc} */
    @Override
    public String getFormat() {
        return trackInfo == null ? "Unknown" : trackInfo.get("mime-type");
    }

    /** {@inheritDoc} */
    @Override
    public String getBitrate() {
        return trackInfo == null ? "Unknown" : trackInfo.get("bit-rate");
    }

    /**
     * Get the duration in seconds as a string.
     *
     * @param seconds Input to get duration for
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

            result.append(hours).append(":");
        }

        if (minutes < 10) {
            result.append('0');
        }

        result.append(minutes).append(":");

        if (seconds < 10) {
            result.append('0');
        }

        result.append(seconds);

        return result.toString();
    }

}

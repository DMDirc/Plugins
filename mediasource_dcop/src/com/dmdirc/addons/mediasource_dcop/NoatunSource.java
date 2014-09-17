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

package com.dmdirc.addons.mediasource_dcop;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceState;
import com.dmdirc.util.DateUtils;

import java.util.List;

/**
 * Uses DCOP to retrieve now playing info from Noatun.
 */
public class NoatunSource implements MediaSource {

    /** Our parent plugin. */
    private final DcopMediaSourcePlugin myPlugin;

    /**
     * Create a new instance of this source.
     *
     * @param myPlugin The plugin that owns this source.
     */
    public NoatunSource(final DcopMediaSourcePlugin myPlugin) {
        this.myPlugin = myPlugin;
    }

    @Override
    public MediaSourceState getState() {
        final List<String> res = myPlugin.getDcopResult("dcop noatun Noatun state");
        if (res.isEmpty()) {
            return MediaSourceState.CLOSED;
        } else {
            final String result = res.get(0).trim();
            try {
                final int status = Integer.parseInt(result);
                switch (status) {
                    case 0:
                        return MediaSourceState.STOPPED;
                    case 1:
                        return MediaSourceState.PAUSED;
                    case 2:
                        return MediaSourceState.PLAYING;
                    default:
                        return MediaSourceState.NOTKNOWN;
                }
            } catch (NumberFormatException nfe) {
                return MediaSourceState.CLOSED;
            }
        }
    }

    @Override
    public String getAppName() {
        return "Noatun";
    }

    @Override
    public String getArtist() {
        final String result = myPlugin.getDcopResult("dcop noatun Noatun title").get(0);
        if (!result.contains(" - ")) {
            return "";
        }
        return result.substring(0, result.indexOf(" - "));
    }

    @Override
    public String getTitle() {
        final String result = myPlugin.getDcopResult("dcop noatun Noatun title").get(0);
        if (!result.contains(" - ")) {
            return "";
        }
        return result.substring(result.indexOf(" - ") + 3, result.length());
    }

    @Override
    public String getAlbum() {
        return null;
    }

    @Override
    public String getLength() {
        return myPlugin.getDcopResult("dcop noatun Noatun lengthString").get(0);
    }

    @Override
    public String getTime() {
        return DateUtils.formatDurationAsTime(
                Integer.parseInt(myPlugin.getDcopResult("dcop noatun Noatun position").get(0)) /
                        1000);
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public String getBitrate() {
        return null;
    }

}

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

package com.dmdirc.addons.mediasource_mplayer;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceState;
import com.dmdirc.plugins.implementations.BasePlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a media source for mplayer which uses lsof to hackily see what mplayer is currently
 * accessing.
 */
public class MplayerMediaSourcePlugin extends BasePlugin implements MediaSource {

    @Override
    public MediaSourceState getState() {
        if (getInfo().isEmpty()) {
            return MediaSourceState.CLOSED;
        } else {
            return MediaSourceState.PLAYING;
        }
    }

    @Override
    public String getAppName() {
        return "MPlayer";
    }

    @Override
    public String getArtist() {
        return "";
    }

    @Override
    public String getTitle() {
        return getInfo().get(0);
    }

    @Override
    public String getAlbum() {
        return "";
    }

    @Override
    public String getLength() {
        return "";
    }

    @Override
    public String getTime() {
        return "";
    }

    @Override
    public String getFormat() {
        return "";
    }

    @Override
    public String getBitrate() {
        return "";
    }

    /**
     * Retrieves information about the currently playing track.
     *
     * @return Information about the currently playing track
     */
    public static List<String> getInfo() {
        final List<String> result = new ArrayList<>();

        try {
            final String[] command = {"/bin/bash", "-c",
                "/usr/bin/lsof -c gmplayer |"
                + " grep -Ev '/dev|/lib|/var|/usr|/SYS|DIR|/tmp|pipe|socket|"
                + "\\.xession|fontconfig' | tail -n 1 | sed -r 's/ +/ /g' |"
                + " cut -d ' ' -f 9- | sed -r 's/^.*\\/(.*?)$/\\1/'"};
            final Process process = Runtime.getRuntime().exec(command);

            try (InputStreamReader reader = new InputStreamReader(process.getInputStream());
                    BufferedReader input = new BufferedReader(reader)) {

                String line;
                while ((line = input.readLine()) != null) {
                    result.add(line);
                }
            }

            process.destroy();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return result;
    }

}

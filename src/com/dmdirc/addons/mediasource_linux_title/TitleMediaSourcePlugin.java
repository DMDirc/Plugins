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

package com.dmdirc.addons.mediasource_linux_title;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceManager;
import com.dmdirc.plugins.BasePlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * A media source plugin which provides two {@link TitleMediaSource}s, one for
 * the Last.fm linux client and one for Spotify (running under Wine).
 */
public class TitleMediaSourcePlugin extends BasePlugin implements MediaSourceManager {

    /** The sources to be returned. */
    private List<MediaSource> sources = null;

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        sources = new ArrayList<MediaSource>(2);
        sources.add(new TitleMediaSource("grep -E '\\(\"last\\.?fm\" \"Last\\.?fm\"\\)'" +
                    "| grep -vE '(\"Last.fm " +
                    "Options\"|\"Diagnostics\"|\"last\\.?fm\"|\"Share\"|\\(has no " +
                    "name\\)):' | sed -r 's/^[^\"]*?\"(.*)\": \\(\"last\\.?fm.*$/\\1/g'", "Last.fm"));
        sources.add(new TitleMediaSource("grep '\": (\"spotify.exe' | cut -d '\"' -f 2 | "
                    + "cut -d '-' -f 2- | sed -r 's/^\\s+|\\s+$//g' | sed -r 's/-/–/g'", "Spotify"));
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        sources.clear();
        sources = null;
    }

    /** {@inheritDoc} */
    @Override
    public List<MediaSource> getSources() {
        return sources;
    }

}

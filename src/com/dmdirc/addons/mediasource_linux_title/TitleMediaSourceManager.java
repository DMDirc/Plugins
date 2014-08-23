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

package com.dmdirc.addons.mediasource_linux_title;

import com.dmdirc.addons.nowplaying.MediaSource;

import net.engio.mbassy.bus.MBassador;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * A media source plugin which provides two {@link TitleMediaSource}s, one for the Last.fm linux
 * client and one for Spotify (running under Wine).
 */
public class TitleMediaSourceManager {

    /** The sources to be returned. */
    private List<MediaSource> sources = null;
    /** The event bus to post errors to. */
    private final MBassador eventBus;

    @Inject
    public TitleMediaSourceManager(final MBassador eventBus) {
        this.eventBus = eventBus;
    }

    public void onLoad() {
        sources = new ArrayList<>(2);
        sources.add(new TitleMediaSource(eventBus, "grep -E '\\(\"last\\.?fm\" \"Last\\.?fm\"\\)'"
                + "| grep -vE '(\"Last.fm "
                + "Options\"|\"Diagnostics\"|\"last\\.?fm\"|\"Share\"|\\(has no "
                + "name\\)):' | sed -r 's/^[^\"]*?\"(.*)\": \\(\"last\\.?fm.*$/\\1/g'", "Last.fm"));
        sources.add(new TitleMediaSource(eventBus, "grep '\": (\"spotify.exe' | cut -d '\"' -f 2 | "
                + "cut -d '-' -f 2- | sed -r 's/^\\s+|\\s+$//g' | sed -r 's/-/–/g'", "Spotify"));
    }

    public void onUnload() {
        sources.clear();
        sources = null;
    }

    public List<MediaSource> getSources() {
        return sources;
    }
}

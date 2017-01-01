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

package com.dmdirc.addons.mediasource_windows;

import com.dmdirc.addons.nowplaying.MediaSource;
import com.dmdirc.addons.nowplaying.MediaSourceManager;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.implementations.PluginFilesHelper;
import com.dmdirc.util.io.StreamUtils;

import com.google.common.io.CharStreams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Manages all Windows based media sources.
 */
public class WindowsMediaSourceManager implements MediaSourceManager {

    /** Media sources. */
    private final List<MediaSource> sources;
    /** Plugin files helper. */
    private final PluginFilesHelper filesHelper;

    @Inject
    public WindowsMediaSourceManager(
            final PluginManager pluginManager,
            @PluginDomain(WindowsMediaSourcePlugin.class) final PluginInfo pluginInfo) {
        this.filesHelper = new PluginFilesHelper(pluginManager, pluginInfo);
        sources = new ArrayList<>();
        sources.add(new DllSource(this, "Winamp", true));
        sources.add(new DllSource(this, "iTunes", false));
    }

    public void onLoad() {
        // Extract the .dlls and .exe
        try {
            filesHelper.extractResourcesEndingWith(".dll");
            filesHelper.extractResourcesEndingWith(".exe");
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to extract needed files: " + ex.getMessage(),
                    ex);
        }
    }

    @Override
     public List<MediaSource> getSources() {
        return sources;
    }

    /**
     * Get the output from GetMediaInfo.exe for the given player and method
     *
     * @param player Player to ask about
     * @param method Method to call
     *
     * @return a MediaInfoOutput with the results
     */
    protected MediaInfoOutput getOutput(final String player, final String method) {
        try {
            final Process myProcess = Runtime.getRuntime().exec(new String[]{
                    filesHelper.getFilesDirString() + "GetMediaInfo.exe",
                    player,
                    method});
            StreamUtils.readStream(myProcess.getErrorStream());
            final String data = CharStreams
                    .toString(new InputStreamReader(myProcess.getInputStream()));
            try {
                myProcess.waitFor();
            } catch (InterruptedException e) {
            }

            return new MediaInfoOutput(myProcess.exitValue(), data);
        } catch (SecurityException | IOException e) {
        }

        return new MediaInfoOutput(-1, "Error executing GetMediaInfo.exe");
    }
}

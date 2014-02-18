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
import com.dmdirc.addons.nowplaying.MediaSourceManager;
import com.dmdirc.plugins.NoSuchProviderException;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.implementations.BasePlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all DCOP based media sources.
 */
public class DcopMediaSourcePlugin extends BasePlugin
        implements MediaSourceManager {

    /** Media sources. */
    private final List<MediaSource> sources;
    /** This plugins plugin manager. */
    private final PluginManager pluginManager;

    /**
     * Creates a new instance of DcopMediaSourcePlugin.
     *
     * @param pluginManager Plugin manager to retrieve services from
     */
    public DcopMediaSourcePlugin(final PluginManager pluginManager) {
        super();
        this.pluginManager = pluginManager;
        sources = new ArrayList<>();
        sources.add(new AmarokSource(this));
        sources.add(new KaffeineSource(this));
        sources.add(new NoatunSource(this));
    }

    /**
     * Get DCOP Result
     *
     * @param query Query to try
     *
     * @return The result of the dcop query, line-by-line
     */
    @SuppressWarnings("unchecked")
    protected List<String> getDcopResult(final String query) {
        try {
            return (List<String>) pluginManager.getExportedService("dcop")
                    .execute(query);
        } catch (NoSuchProviderException nspe) {
            return new ArrayList<>();
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<MediaSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

}

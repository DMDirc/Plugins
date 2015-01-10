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

package com.dmdirc.addons.qauth;

import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;

import javax.inject.Inject;

/**
 * Provides Q AUth support in DMDirc.
 */
public class QAuthManager {

    private final String domain;
    private final PluginInfo pluginInfo;

    @Inject
    public QAuthManager(
            @PluginDomain(QAuthPlugin.class) final String domain,
            @PluginDomain(QAuthPlugin.class) final PluginInfo pluginInfo) {
        this.domain = domain;
        this.pluginInfo = pluginInfo;
    }

    public void load() {
    }

    public void unload() {
    }

    public void showConfig(final ClientPrefsOpenedEvent event) {
        // TODO: Show a config
    }
}

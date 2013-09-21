/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_web;

import com.dmdirc.ServerManager;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.plugins.implementations.BasePlugin;

import lombok.Getter;

import org.mortbay.jetty.Handler;

/**
 * The main web interface plugin.
 */
public class WebInterfacePlugin extends BasePlugin {

    /** Server manager to use. */
    private final ServerManager serverManager;

    /** Plugin manager to use. */
    private final PluginManager pluginManager;

    /** This plugin's information object. */
    private final PluginInfo pluginInfo;

    /** The UI that we're using. */
    @Getter
    private WebInterfaceUI controller;

    /**
     * Create a new WebInterfacePlugin
     *
     * @param serverManager Server manager to use.
     * @param pluginManager Plugin manager to use.
     * @param pluginInfo This plugin's info object.
     */
    public WebInterfacePlugin(
            final ServerManager serverManager,
            final PluginManager pluginManager,
            final PluginInfo pluginInfo) {
        this.serverManager = serverManager;
        this.pluginManager = pluginManager;
        this.pluginInfo = pluginInfo;
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        if (controller == null) {
            controller = new WebInterfaceUI(getDomain(), serverManager,
                    pluginManager, pluginInfo);
        }
    }

    /**
     * Adds the specified handler to the WebInterface's web server.
     *
     * @param newHandler The handler to be added
     */
    public void addWebHandler(final Handler newHandler) {
        if (controller == null) {
            controller = new WebInterfaceUI(getDomain(), serverManager,
                    pluginManager, pluginInfo);
        }

        controller.addWebHandler(newHandler);
    }
}

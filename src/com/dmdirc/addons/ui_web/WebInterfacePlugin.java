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

package com.dmdirc.addons.ui_web;

import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BasePlugin;

import dagger.ObjectGraph;

/**
 * The main web interface plugin.
 */
public class WebInterfacePlugin extends BasePlugin {

    /** The UI that we're using. */
    private WebInterfaceUI controller;

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);
        setObjectGraph(graph.plus(new WebInterfaceModule(pluginInfo)));
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        controller = getObjectGraph().get(WebInterfaceUI.class);
    }

    /**
     * Exported version of the controller.
     *
     * @return The controller, exported for use in the client.
     */
    @Exported
    public UIController getController() {
        return controller;
    }

}

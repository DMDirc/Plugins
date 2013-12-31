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

package com.dmdirc.addons.dcc;

import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import dagger.ObjectGraph;

/**
 * Adds support for DCC transfers and chats.
 */
public class DCCPlugin extends BaseCommandPlugin {

    private DCCManager dccManager;

    /** {@inheritDoc} */
    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new DCCPluginModule(pluginInfo)));
        dccManager = getObjectGraph().get(DCCManager.class);
        registerCommand(DCCCommand.class, DCCCommand.INFO);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        super.onLoad();
        dccManager.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        super.onUnload();
        dccManager.onUnload();
    }

    /** {@inheritDoc} */
    @Override
    public void showConfig(final PreferencesDialogModel manager) {
        dccManager.showConfig(manager);
    }

}

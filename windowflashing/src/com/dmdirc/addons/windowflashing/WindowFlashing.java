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

package com.dmdirc.addons.windowflashing;

import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import dagger.ObjectGraph;

/**
 * Native notification plugin to make DMDirc support windows task bar flashing.
 */
public class WindowFlashing extends BaseCommandPlugin {

    /** Window flashing manager. */
    private WindowFlashingManager manager;

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new WindowFlashingModule(pluginInfo)));
        registerCommand(FlashWindow.class, FlashWindow.INFO);
        manager = getObjectGraph().get(WindowFlashingManager.class);
    }

    /**
     * Flashes an inactive window under windows, used as a showNotifications exported command
     *
     * @param title   Unused
     * @param message Unused
     */
    @Exported
    public void flashNotification(final String title, final String message) {
        manager.flashWindow();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        manager.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        manager.onUnload();
    }

}

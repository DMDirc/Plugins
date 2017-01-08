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

package com.dmdirc.addons.systray;

import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;
import com.dmdirc.util.validators.ValidationResponse;

import java.awt.SystemTray;

import dagger.ObjectGraph;

/**
 * The Systray plugin shows DMDirc in the user's system tray, and allows notifications to be
 * disabled.
 */
public class SystrayPlugin extends BaseCommandPlugin {

    /** Systray manager. */
    private SystrayManager manager;

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);
        setObjectGraph(graph.plus(new SystrayModule(pluginInfo)));
        registerCommand(PopupCommand.class, PopupCommand.INFO);
        manager = getObjectGraph().get(SystrayManager.class);
    }

    /**
     * Proxy method for notify, this method is used for the exported command to avoid ambiguity when
     * performing reflection.
     *
     * @param title   Title for the notification
     * @param message Text for the notification
     */
    @Exported
    public void showPopup(final String title, final String message) {
        manager.notify(title, message);
    }

    @Override
    public ValidationResponse checkPrerequisites() {
        if (SystemTray.isSupported()) {
            return new ValidationResponse();
        } else {
            return new ValidationResponse("System tray is not supported on "
                    + "this platform.");
        }
    }

    @Override
    public void onLoad() {
        manager.load();
        super.onLoad();
    }

    @Override
    public void onUnload() {
        manager.unload();
        super.onUnload();
    }
}

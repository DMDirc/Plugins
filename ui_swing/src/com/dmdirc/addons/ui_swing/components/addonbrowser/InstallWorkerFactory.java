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

package com.dmdirc.addons.ui_swing.components.addonbrowser;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.io.Downloader;

import javax.inject.Inject;

/**
 * Factory for {@link InstallWorker}s.
 */
public class InstallWorkerFactory {

    private final Downloader downloader;
    private final String tempDirectory;
    private final String pluginDirectory;
    private final String themeDirectory;
    private final PluginManager pluginManager;
    private final DMDircMBassador eventBus;
    private final ActionManager actionManager;

    @Inject
    public InstallWorkerFactory(final Downloader downloader,
            final ActionManager actionManager,
            @Directory(DirectoryType.TEMPORARY) final String tempDirectory,
            @Directory(DirectoryType.PLUGINS) final String pluginDirectory,
            @Directory(DirectoryType.THEMES) final String themeDirectory,
            final PluginManager pluginManager, final DMDircMBassador eventBus) {
        this.actionManager = actionManager;
        this.downloader = downloader;
        this.tempDirectory = tempDirectory;
        this.pluginDirectory = pluginDirectory;
        this.themeDirectory = themeDirectory;
        this.pluginManager = pluginManager;
        this.eventBus = eventBus;
    }
    public InstallWorker getInstallWorker(final AddonInfo info, final InstallerWindow installer) {
        return new InstallWorker(downloader, tempDirectory, pluginDirectory,
                themeDirectory, pluginManager, eventBus, actionManager, info, installer);
    }
}

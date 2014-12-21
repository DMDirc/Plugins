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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.io.Downloader;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.swing.JScrollPane;

/**
 * Factory for {@link DataLoaderWorker}s.
 */
public class DataLoaderWorkerFactory {

    private final Downloader downloader;
    private final AggregateConfigProvider globalConfig;
    private final URLBuilder urlBuilder;
    private final InstallWorkerFactory workerFactory;
    private final UpdateManager updateManager;
    private final Path tempDirectory;
    private final DMDircMBassador eventBus;

    @Inject
    public DataLoaderWorkerFactory(final Downloader downloader,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final URLBuilder urlBuilder,
            final InstallWorkerFactory workerFactory,
            final UpdateManager updateManager,
            final DMDircMBassador eventBus,
            @Directory(DirectoryType.TEMPORARY)
            final Path tempDirectory) {
        this.downloader = downloader;
        this.globalConfig = globalConfig;
        this.urlBuilder = urlBuilder;
        this.workerFactory = workerFactory;
        this.updateManager = updateManager;
        this.tempDirectory = tempDirectory;
        this.eventBus = eventBus;
    }
    public DataLoaderWorker getDataLoaderWorker(final AddonTable list, final boolean download,
            final BrowserWindow browserWindow, final JScrollPane scrollPane) {
        return new DataLoaderWorker(downloader, globalConfig, urlBuilder, workerFactory,
                updateManager, tempDirectory, eventBus, list, download, browserWindow, scrollPane);
    }
}

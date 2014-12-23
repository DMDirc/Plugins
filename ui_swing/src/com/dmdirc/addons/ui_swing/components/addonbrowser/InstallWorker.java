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
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.io.Downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

/**
 * Downloads a given addon from the addon site and loads it into the client.
 */
public class InstallWorker extends LoggingSwingWorker<String, Void> {

    /** Addon info. */
    private final AddonInfo info;
    /** Window to show installer progress. */
    private final InstallerWindow installer;
    /** Temporary directory to download files to. */
    private final String tempDirectory;
    /** Directory to install plugins in. */
    private final String pluginDirectory;
    /** Directory to install themes in. */
    private final String themeDirectory;
    /** Plugin manager to inform of new plugins. */
    private final PluginManager pluginManager;
    /** Downloader to download files. */
    private final Downloader downloader;
    /** Action controller to install actions with. */
    private final ActionManager actionController;

    public InstallWorker(
            final Downloader downloader,
            final String tempDirectory,
            final String pluginDirectory,
            final String themeDirectory,
            final PluginManager pluginManager,
            final DMDircMBassador eventBus,
            final ActionManager actionController,
            final AddonInfo info,
            final InstallerWindow window) {
        super(eventBus);
        this.actionController = actionController;
        this.downloader = downloader;
        this.info = info;
        this.installer = window;
        this.tempDirectory = tempDirectory;
        this.pluginDirectory = pluginDirectory;
        this.themeDirectory = themeDirectory;
        this.pluginManager = pluginManager;
    }

    @Override
    protected String doInBackground() {
        try {
            final Path file = Paths.get(tempDirectory, "." + info.getId());
            downloader.downloadPage(info.getDownload(), file);

            switch (info.getType()) {
                case TYPE_ACTION_PACK:
                    actionController.installActionPack(file);
                    break;
                case TYPE_PLUGIN:
                    final Path newFile = Paths.get(pluginDirectory, info.getTitle() + ".jar");
                    try {
                        Files.move(file, newFile);
                        pluginManager.addPlugin(newFile.getFileName().toString());
                    } catch (IOException ex) {
                        return "Unable to install addon, failed to move file: "
                                + file.toAbsolutePath().toString();
                    }
                    break;
                case TYPE_THEME:
                    try {
                        Files.move(file, Paths.get(themeDirectory, info.getTitle() + ".zip"));
                    } catch (IOException ex) {
                        return "Unable to install addon, failed to move file: "
                                + file.toAbsolutePath().toString();
                    }
                    break;
                default:
                    return "Unknown addon type";
            }
        } catch (IOException ex) {
            return "Unable to download addon: " + ex.getMessage();
        }
        return "";
    }

    @Override
    protected void done() {
        String message = "";
        try {
            message = get();
        } catch (InterruptedException ex) {
            message = "Interrupted during execution, unable to complete.";
        } catch (ExecutionException ex) {
            message = "Unable to install: " + ex.getMessage();
        } finally {
            if (message.isEmpty()) {
                installer.finished("");
            } else {
                installer.finished(message);
            }
        }
    }

}

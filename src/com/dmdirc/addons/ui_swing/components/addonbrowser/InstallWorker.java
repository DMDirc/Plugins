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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;
import com.dmdirc.util.io.Downloader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Downloads a given addon from the addon site and loads it into the client.
 */
@Factory(inject = true)
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

    public InstallWorker(
            @SuppressWarnings("qualifiers") @Directory(DirectoryType.TEMPORARY) final String tempDirectory,
            @SuppressWarnings("qualifiers") @Directory(DirectoryType.PLUGINS) final String pluginDirectory,
            @SuppressWarnings("qualifiers") @Directory(DirectoryType.THEMES) final String themeDirectory,
            final PluginManager pluginManager,
            @Unbound final AddonInfo info,
            @Unbound final InstallerWindow window) {
        super();

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
            final File file = new File(tempDirectory, "." + info.getId());
            Downloader.downloadPage("http://addons.dmdirc.com/addondownload/"
                    + info.getDownload(), file.getAbsolutePath());

            switch (info.getType()) {
                case TYPE_ACTION_PACK:
                    ActionManager.installActionPack(file.getAbsolutePath());
                    break;
                case TYPE_PLUGIN:
                    final File newFile = new File(pluginDirectory, info.getTitle() + ".jar");
                    if (file.renameTo(newFile)) {
                        pluginManager.addPlugin(newFile.getName());
                    } else {
                        return "Unable to install addon, failed to move file: "
                                + file.getAbsolutePath();
                    }
                    break;
                case TYPE_THEME:
                    if (!file.renameTo(new File(themeDirectory, info.getTitle() + ".zip"))) {
                        return "Unable to install addon, failed to move file: "
                                + file.getAbsolutePath();
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

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

package com.dmdirc.addons.dcc.kde;

import com.dmdirc.addons.dcc.DCCManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

/**
 * JFileChooser that uses KDialog to show the actual chooser. This is quite hacky, and not
 * guarenteed to behave identically to JFileChooser, altho it tries to be as close as possible.
 * Almost a drop in replacement for JFileChooser, replace: new JFileChooser(); with:
 * KFileChooser.getFileChooser();
 *
 * There are obviously some differences: - File filters must be set using setKDEFileFilter() not
 * using FileFilter objects. - FileSystemView's are ignored - showOpenDialog and showSaveDialog
 * shell kdialog, so only options available in kdialog work. - getFileChooser() will return a
 * JFileChooser object unless the DCC plugin's config option "general.useKFileChooser" is set to
 * "true" (defaults to false) and kdialog is in either /usr/bin or /bin - Selection mode
 * FILES_AND_DIRECTORIES can not be used
 */
public class KFileChooser extends JFileChooser {

    /** A version number for this class. */
    private static final long serialVersionUID = 200806141;
    /** The plugin that this file chooser is for. */
    private final DCCManager plugin;
    /** Used to read settings from. */
    private final AggregateConfigProvider config;

    /**
     * Constructs a FileChooser pointing to the user's default directory.
     *
     * @param plugin The plugin that owns this KFileChooser
     */
    private KFileChooser(final AggregateConfigProvider config, final DCCManager plugin) {
        super();

        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Constructs a FileChooser using the given File as the path.
     *
     * @param plugin           The plugin that owns this KFileChooser
     * @param currentDirectory Directory to use as the base directory
     */
    private KFileChooser(final AggregateConfigProvider config, final DCCManager plugin,
            final File currentDirectory) {
        super(currentDirectory);

        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Constructs a FileChooser using the given path.
     *
     * @param plugin               The plugin that owns this KFileChooser
     * @param currentDirectoryPath Directory to use as the base directory
     */
    private KFileChooser(final AggregateConfigProvider config, final DCCManager plugin,
            final String currentDirectoryPath) {
        super(currentDirectoryPath);

        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Should a KFileChooser be used rather than a JFileChooser?
     *
     * @param config Config manager
     * @param plugin The DCC Plugin that is requesting a chooser
     *
     * @return return true if getFileChooser() will return a KFileChooser not a JFileChooser
     */
    public static boolean useKFileChooser(final AggregateConfigProvider config,
            final DCCManager plugin) {
        return KDialogProcess.hasKDialog() && config.getOptionBool(plugin.getDomain(),
                "general.useKFileChooser");
    }

    /**
     * Constructs a FileChooser pointing to the user's default directory.
     *
     * @param config Config provider used to retrieve settings
     * @param plugin The DCC Plugin that is requesting a chooser
     *
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final AggregateConfigProvider config,
            final DCCManager plugin) {
        return useKFileChooser(config, plugin) ? new KFileChooser(config, plugin)
                : new JFileChooser();
    }

    /**
     * Constructs a FileChooser using the given File as the path.
     *
     * @param config           Config provider used to retrieve settings
     * @param plugin           The DCC Plugin that is requesting a chooser
     * @param currentDirectory Directory to use as the base directory
     *
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final AggregateConfigProvider config,
            final DCCManager plugin, final File currentDirectory) {
        return useKFileChooser(config, plugin) ? new KFileChooser(config, plugin, currentDirectory)
                : new JFileChooser(currentDirectory);
    }

    /**
     * Constructs a FileChooser using the given path.
     *
     * @param config               Config provider used to retrieve settings
     * @param plugin               The DCC Plugin that is requesting a chooser
     * @param currentDirectoryPath Directory to use as the base directory
     *
     * @return The relevant FileChooser
     */
    public static JFileChooser getFileChooser(final AggregateConfigProvider config,
            final DCCManager plugin, final String currentDirectoryPath) {
        return useKFileChooser(config, plugin) ? new KFileChooser(config, plugin,
                currentDirectoryPath) : new JFileChooser(currentDirectoryPath);
    }

    @Override
    public int showOpenDialog(final Component parent) throws HeadlessException {
        if (!useKFileChooser(config, plugin)) {
            return super.showOpenDialog(parent);
        }

        final ArrayList<String> params = new ArrayList<>();
        if (isMultiSelectionEnabled()) {
            params.add("--multiple");
            params.add("--separate-output");
        }

        if (getDialogTitle() != null && !getDialogTitle().isEmpty()) {
            params.add("--caption");
            params.add(getDialogTitle());
        }

        if (getFileSelectionMode() == DIRECTORIES_ONLY) {
            params.add("--getexistingdirectory");
        } else {
            params.add("--getopenfilename");
        }

        if (getSelectedFile() != null && getFileSelectionMode() != DIRECTORIES_ONLY
                && !getSelectedFile().getPath().isEmpty()) {
            if (getSelectedFile().getPath().charAt(0) != '/') {
                params.add(getCurrentDirectory().getPath() + File.separator + getSelectedFile().
                        getPath());
            } else {
                params.add(getSelectedFile().getPath());
            }
        } else if (getCurrentDirectory() != null) {
            params.add(getCurrentDirectory().getPath());
        }

        final KDialogProcess kdp;
        try {
            kdp = new KDialogProcess(params.toArray(new String[params.size()]));
            kdp.waitFor();
        } catch (IOException | InterruptedException e) {
            return JFileChooser.ERROR_OPTION;
        }

        if (kdp.getProcess().exitValue() == 0) {
            if (isMultiSelectionEnabled()) {
                final List<String> list = kdp.getStdOutStream().getList();
                final File[] fileList = new File[list.size()];
                for (int i = 0; i < list.size(); ++i) {
                    fileList[i] = new File(list.get(i));
                }
                setSelectedFiles(fileList);
            } else {
                setSelectedFile(new File(kdp.getStdOutStream().getList().get(0)));
            }
            return JFileChooser.APPROVE_OPTION;
        } else {
            return JFileChooser.ERROR_OPTION;
        }
    }

    @Override
    public int showSaveDialog(final Component parent) throws HeadlessException {
        if (!useKFileChooser(config, plugin)) {
            return super.showSaveDialog(parent);
        }

        final ArrayList<String> params = new ArrayList<>();
        if (getDialogTitle() != null && !getDialogTitle().isEmpty()) {
            params.add("--caption");
            params.add(getDialogTitle());
        }

        params.add("--getsavefilename");
        if (getSelectedFile() != null && !getSelectedFile().getPath().isEmpty()) {
            if (getSelectedFile().getPath().charAt(0) != '/') {
                params.add(getCurrentDirectory().getPath() + File.separator + getSelectedFile().
                        getPath());
            } else {
                params.add(getSelectedFile().getPath());
            }
        } else if (getCurrentDirectory() != null) {
            params.add(getCurrentDirectory().getPath());
        }

        final KDialogProcess kdp;
        try {
            kdp = new KDialogProcess(params.toArray(new String[params.size()]));
            kdp.waitFor();
        } catch (IOException | InterruptedException e) {
            return JFileChooser.ERROR_OPTION;
        }

        if (kdp.getProcess().exitValue() == 0) {
            setSelectedFile(new File(kdp.getStdOutStream().getList().get(0)));
            return JFileChooser.APPROVE_OPTION;
        } else {
            return JFileChooser.ERROR_OPTION;
        }
    }

}

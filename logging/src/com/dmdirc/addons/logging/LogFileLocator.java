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

package com.dmdirc.addons.logging;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.plugins.PluginDomain;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Facilitates finding a path for log files.
 */
@Singleton
public class LogFileLocator {

    private final DMDircMBassador eventBus;
    private final Provider<String> directoryProvider;

    /** Whether to append a hash of the file name to the file name... */
    @ConfigBinding(key = "advanced.filenamehash")
    private boolean filenamehash;

    /** Whether to create a new folder for each network. */
    @ConfigBinding(key = "general.networkfolders")
    private boolean networkfolders;

    /** Whether to use date formats in file names. */
    @ConfigBinding(key = "advanced.usedate")
    private boolean usedate;

    /** Date format to use in file names if {@link #usedate} is true. */
    @ConfigBinding(key = "advanced.usedateformat")
    private String usedateformat;

    @Inject
    public LogFileLocator(
            final DMDircMBassador eventBus,
            @Directory(LoggingModule.LOGS_DIRECTORY) final Provider<String> directoryProvider,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(LoggingPlugin.class) final String domain) {
        this.eventBus = eventBus;
        this.directoryProvider = directoryProvider;

        globalConfig.getBinder().withDefaultDomain(domain).bind(this, LogFileLocator.class);
    }

    /**
     * Sanitises the log file directory.
     *
     * @return Log directory
     */
    private StringBuffer getLogDirectory() {
        final StringBuffer directory = new StringBuffer();
        directory.append(directoryProvider.get());
        if (directory.charAt(directory.length() - 1) != File.separatorChar) {
            directory.append(File.separatorChar);
        }
        return directory;
    }

    /**
     * Get the name of the log file for a specific object.
     *
     * @param channel Channel to get the name for
     *
     * @return the name of the log file to use for this object.
     */
    public String getLogFile(final ChannelInfo channel) {
        final StringBuffer directory = getLogDirectory();
        final StringBuffer file = new StringBuffer();
        if (channel.getParser() != null) {
            addNetworkDir(directory, file, channel.getParser().getNetworkName());
        }
        file.append(sanitise(channel.getName().toLowerCase()));
        return getPath(directory, file, channel.getName());
    }

    /**
     * Get the name of the log file for a specific object.
     *
     * @param client Client to get the name for
     *
     * @return the name of the log file to use for this object.
     */
    public String getLogFile(final ClientInfo client) {
        final StringBuffer directory = getLogDirectory();
        final StringBuffer file = new StringBuffer();
        if (client.getParser() != null) {
            addNetworkDir(directory, file, client.getParser().getNetworkName());
        }
        file.append(sanitise(client.getNickname().toLowerCase()));
        return getPath(directory, file, client.getNickname());
    }

    /**
     * Get the name of the log file for a specific object.
     *
     * @param descriptor Description of the object to get a log file for.
     *
     * @return the name of the log file to use for this object.
     */
    public String getLogFile(@Nullable final String descriptor) {
        final StringBuffer directory = getLogDirectory();
        final StringBuffer file = new StringBuffer();
        final String md5String;
        if (descriptor == null) {
            file.append("null.log");
            md5String = "";
        } else {
            file.append(sanitise(descriptor.toLowerCase()));
            md5String = descriptor;
        }
        return getPath(directory, file, md5String);
    }

    /**
     * Gets the path for the given file and directory. Only intended to be used from getLogFile
     * methods.
     *
     * @param directory Log file directory
     * @param file      Log file path
     * @param md5String Log file object MD5 hash
     *
     * @return Name of the log file
     */
    public String getPath(final StringBuffer directory, final StringBuffer file,
            final String md5String) {
        if (usedate) {
            final String dateFormat = usedateformat;
            final String dateDir = new SimpleDateFormat(dateFormat).format(new Date());
            directory.append(dateDir);
            if (directory.charAt(directory.length() - 1) != File.separatorChar) {
                directory.append(File.separatorChar);
            }

            if (!new File(directory.toString()).exists()
                    && !new File(directory.toString()).mkdirs()) {
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                        "Unable to create date dirs", ""));
            }
        }

        if (filenamehash) {
            file.append('.');
            file.append(md5(md5String));
        }
        file.append(".log");

        return directory + file.toString();
    }

    /**
     * This function adds the networkName to the log file. It first tries to create a directory for
     * each network, if that fails it will prepend the networkName to the filename instead.
     *
     * @param directory   Current directory name
     * @param file        Current file name
     * @param networkName Name of network
     */
    protected void addNetworkDir(final StringBuffer directory, final StringBuffer file,
            final String networkName) {
        if (!networkfolders) {
            return;
        }

        final String network = sanitise(networkName.toLowerCase());

        boolean prependNetwork = false;

        // Check dir exists
        final File dir = new File(directory + network + System.getProperty(
                "file.separator"));
        if (dir.exists() && !dir.isDirectory()) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                    "Unable to create networkfolders dir (file exists instead)", ""));
            // Prepend network name to file instead.
            prependNetwork = true;
        } else if (!dir.exists() && !dir.mkdirs()) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                    "Unable to create networkfolders dir", ""));
            prependNetwork = true;
        }

        if (prependNetwork) {
            file.insert(0, " -- ");
            file.insert(0, network);
        } else {
            directory.append(network);
            directory.append(System.getProperty("file.separator"));
        }
    }


    /**
     * Sanitise a string to be used as a filename.
     *
     * @param name String to sanitise
     *
     * @return Sanitised version of name that can be used as a filename.
     */
    protected static String sanitise(final String name) {
        // Replace illegal chars with
        return name.replaceAll("[^\\w\\.\\s\\-#&_]", "_");
    }

    /**
     * Get the md5 hash of a string.
     *
     * @param string String to hash
     *
     * @return md5 hash of given string
     */
    protected static String md5(final String string) {
        try {
            final MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(string.getBytes(), 0, string.length());
            return new BigInteger(1, m.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

}

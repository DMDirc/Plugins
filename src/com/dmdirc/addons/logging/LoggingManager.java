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

import com.dmdirc.Channel;
import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.FrameContainer;
import com.dmdirc.Query;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.events.ChannelClosedEvent;
import com.dmdirc.events.ChannelOpenedEvent;
import com.dmdirc.events.QueryActionEvent;
import com.dmdirc.events.QueryClosedEvent;
import com.dmdirc.events.QueryMessageEvent;
import com.dmdirc.events.QueryOpenedEvent;
import com.dmdirc.events.QuerySelfActionEvent;
import com.dmdirc.events.QuerySelfMessageEvent;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.parser.interfaces.ChannelInfo;
import com.dmdirc.parser.interfaces.ClientInfo;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.Styliser;
import com.dmdirc.util.URLBuilder;
import com.dmdirc.util.io.ReverseFileReader;
import com.dmdirc.util.io.StreamUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Manages logging activities.
 */
@Singleton
public class LoggingManager implements ActionListener, ConfigChangeListener {

    /** Date format used for "File Opened At" log. */
    private static final DateFormat OPENED_AT_FORMAT = new SimpleDateFormat(
            "EEEE MMMM dd, yyyy - HH:mm:ss");
    /** This plugin's plugin info. */
    private final String domain;
    /** The action controller to use. */
    private final ActionController actionController;
    /** Global config. */
    private final AggregateConfigProvider config;
    /** The manager to add history windows to. */
    private final WindowManager windowManager;
    /** Map of open files. */
    private final Map<String, OpenFile> openFiles = Collections.synchronizedMap(
            new HashMap<String, OpenFile>());
    private final URLBuilder urlBuilder;
    private final EventBus eventBus;
    private final Provider<String> directoryProvider;
    /** Timer used to close idle files. */
    private Timer idleFileTimer;
    /** Cached boolean settings. */
    private boolean networkfolders;
    private boolean filenamehash;
    private boolean addtime;
    private boolean stripcodes;
    private boolean channelmodeprefix;
    private boolean autobackbuffer;
    private boolean backbufferTimestamp;
    private boolean usedate;
    /** Cached string settings. */
    private String timestamp;
    private String usedateformat;
    private String colour;
    /** Cached int settings. */
    private int historyLines;
    private int backbufferLines;

    @Inject
    public LoggingManager(
            @PluginDomain(LoggingPlugin.class) final String domain,
            final ActionController actionController,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final WindowManager windowManager,
            final URLBuilder urlBuilder,
            final EventBus eventBus,
            @Directory(LoggingModule.LOGS_DIRECTORY) final Provider<String> directoryProvider) {
        this.domain = domain;
        this.actionController = actionController;
        this.config = globalConfig;
        this.windowManager = windowManager;
        this.urlBuilder = urlBuilder;
        this.eventBus = eventBus;
        this.directoryProvider = directoryProvider;
    }

    public void load() {
        setCachedSettings();

        final File dir = new File(directoryProvider.get());
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                Logger.userError(ErrorLevel.LOW,
                        "Unable to create logging dir (file exists instead)");
            }
        } else {
            if (!dir.mkdirs()) {
                Logger.userError(ErrorLevel.LOW, "Unable to create logging dir");
            }
        }

        config.addChangeListener(domain, this);

        actionController.registerListener(this,
                CoreActionType.CHANNEL_MESSAGE,
                CoreActionType.CHANNEL_SELF_MESSAGE,
                CoreActionType.CHANNEL_ACTION,
                CoreActionType.CHANNEL_SELF_ACTION,
                CoreActionType.CHANNEL_GOTTOPIC,
                CoreActionType.CHANNEL_TOPICCHANGE,
                CoreActionType.CHANNEL_JOIN,
                CoreActionType.CHANNEL_PART,
                CoreActionType.CHANNEL_QUIT,
                CoreActionType.CHANNEL_KICK,
                CoreActionType.CHANNEL_NICKCHANGE,
                CoreActionType.CHANNEL_MODECHANGE,
                CoreActionType.QUERY_OPENED,
                CoreActionType.QUERY_CLOSED);

        // Close idle files every hour.
        idleFileTimer = new Timer("LoggingPlugin Timer");
        idleFileTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                timerTask();
            }
        }, 3600000);

        eventBus.register(this);
    }

    public void unload() {
        if (idleFileTimer != null) {
            idleFileTimer.cancel();
            idleFileTimer.purge();
        }

        actionController.unregisterListener(this);

        synchronized (openFiles) {
            for (OpenFile file : openFiles.values()) {
                StreamUtils.close(file.writer);
            }
            openFiles.clear();
        }

        eventBus.unregister(this);
    }

    /**
     * What to do every hour when the timer fires.
     */
    protected void timerTask() {
        // Oldest time to allow
        final long oldestTime = System.currentTimeMillis() - 3480000;

        synchronized (openFiles) {
            final Collection<String> old = new ArrayList<>(openFiles.size());
            for (Map.Entry<String, OpenFile> entry : openFiles.entrySet()) {
                if (entry.getValue().lastUsedTime < oldestTime) {
                    StreamUtils.close(entry.getValue().writer);
                    old.add(entry.getKey());
                }
            }

            openFiles.keySet().removeAll(old);
        }
    }

    @Subscribe
    public void handleQueryOpened(final QueryOpenedEvent event) {
        final Parser parser = event.getQuery().getConnection().getParser();
        final ClientInfo client = parser.getClient(event.getQuery().getHost());
        final String filename = getLogFile(client);
        if (autobackbuffer) {
            showBackBuffer(event.getQuery(), filename);
        }

        appendLine(filename, "*** Query opened at: %s", OPENED_AT_FORMAT.format(new Date()));
        appendLine(filename, "*** Query with User: %s", event.getQuery().getHost());
        appendLine(filename, "");
    }

    @Subscribe
    public void handleQueryClosed(final QueryClosedEvent event) {
        final Parser parser = event.getQuery().getConnection().getParser();
        final ClientInfo client = parser.getClient(event.getQuery().getHost());
        final String filename = getLogFile(client);
        appendLine(filename, "*** Query closed at: %s", OPENED_AT_FORMAT.format(new Date()));
        if (openFiles.containsKey(filename)) {
            StreamUtils.close(openFiles.get(filename).writer);
            openFiles.remove(filename);
        }
    }

    @Subscribe
    public void handleQuerySelfAction(final QuerySelfActionEvent event) {
        final ClientInfo client = event.getClient();
        final String filename = getLogFile(client);
        appendLine(filename, "* %s %s", client.getNickname(), event.getMessage());
    }

    @Subscribe
    public void handleQueryAction(final QueryActionEvent event) {
        final ClientInfo client = event.getClient();
        final String filename = getLogFile(client);
        appendLine(filename, "* %s %s", client.getNickname(), event.getMessage());
    }

    @Subscribe
    public void handleQuerySelfMessage(final QuerySelfMessageEvent event) {
        final ClientInfo client = event.getClient();
        final String filename = getLogFile(client);
        appendLine(filename, "<%s> %s", client.getNickname(), event.getMessage());
    }

    @Subscribe
    public void handleQueryMessage(final QueryMessageEvent event) {
        final ClientInfo client = event.getClient();
        final String filename = getLogFile(client);
        appendLine(filename, "<%s> %s", client.getNickname(), event.getMessage());
    }

    /**
     * Log a channel-related event.
     *
     * @param type      The type of the event to process
     * @param format    Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    protected void handleChannelEvent(final CoreActionType type, final StringBuffer format,
            final Object... arguments) {
        final Channel chan = ((Channel) arguments[0]);
        final ChannelInfo channel = chan.getChannelInfo();
        final String filename = getLogFile(channel);

        final ChannelClientInfo channelClient = (arguments.length > 1
                && arguments[1] instanceof ChannelClientInfo) ? (ChannelClientInfo) arguments[1]
                : null;
        final ClientInfo client = channelClient == null ? null : channelClient.getClient();

        final String message = (arguments.length > 2 && arguments[2] instanceof String)
                ? (String) arguments[2] : null;

        switch (type) {
            case CHANNEL_MESSAGE:
            case CHANNEL_SELF_MESSAGE:
            case CHANNEL_ACTION:
            case CHANNEL_SELF_ACTION:
                if (type == CoreActionType.CHANNEL_MESSAGE || type
                        == CoreActionType.CHANNEL_SELF_MESSAGE) {
                    appendLine(filename, "<%s> %s", getDisplayName(client), message);
                } else {
                    appendLine(filename, "* %s %s", getDisplayName(client), message);
                }
                break;
            case CHANNEL_GOTTOPIC:
                final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                appendLine(filename, "*** Topic is: %s", channel.getTopic());
                appendLine(filename, "*** Set at: %s on %s by %s", timeFormat.format(1000 * channel.
                        getTopicTime()), dateFormat.format(1000 * channel.getTopicTime()), channel.
                        getTopicSetter());
                break;
            case CHANNEL_TOPICCHANGE:
                appendLine(filename, "*** %s Changed the topic to: %s",
                        getDisplayName(channelClient), message);
                break;
            case CHANNEL_JOIN:
                appendLine(filename, "*** %s (%s) joined the channel", getDisplayName(channelClient),
                        client.toString());
                break;
            case CHANNEL_PART:
                if (message.isEmpty()) {
                    appendLine(filename, "*** %s (%s) left the channel", getDisplayName(
                            channelClient), client.toString());
                } else {
                    appendLine(filename, "*** %s (%s) left the channel (%s)", getDisplayName(
                            channelClient), client.toString(), message);
                }
                break;
            case CHANNEL_QUIT:
                if (message.isEmpty()) {
                    appendLine(filename, "*** %s (%s) Quit IRC", getDisplayName(channelClient),
                            client.toString());
                } else {
                    appendLine(filename, "*** %s (%s) Quit IRC (%s)", getDisplayName(channelClient),
                            client.toString(), message);
                }
                break;
            case CHANNEL_KICK:
                final String kickReason = (String) arguments[3];
                final ChannelClientInfo kickedClient = (ChannelClientInfo) arguments[2];

                if (kickReason.isEmpty()) {
                    appendLine(filename, "*** %s was kicked by %s", getDisplayName(kickedClient),
                            getDisplayName(channelClient));
                } else {
                    appendLine(filename, "*** %s was kicked by %s (%s)",
                            getDisplayName(kickedClient), getDisplayName(channelClient), kickReason);
                }
                break;
            case CHANNEL_NICKCHANGE:
                appendLine(filename, "*** %s is now %s", getDisplayName(channelClient, message),
                        getDisplayName(channelClient));
                break;
            case CHANNEL_MODECHANGE:
                if (channelClient.getClient().getNickname().isEmpty()) {
                    appendLine(filename, "*** Channel modes are: %s", message);
                } else {
                    appendLine(filename, "*** %s set modes: %s", getDisplayName(channelClient),
                            message);
                }
                break;
        }
    }

    /**
     * Process an event of the specified type.
     *
     * @param type      The type of the event to process
     * @param format    Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type instanceof CoreActionType) {
            final CoreActionType thisType = (CoreActionType) type;
            handleChannelEvent(thisType, format, arguments);
        }
    }

    @Override
    public void configChanged(final String domain, final String key) {
        setCachedSettings();
    }

    @Subscribe
    public void handleChannelOpened(final ChannelOpenedEvent event) {
        final String filename = getLogFile(event.getChannel());

        if (autobackbuffer) {
            showBackBuffer(event.getChannel(), filename);
        }

        appendLine(filename, "*** Channel opened at: %s", OPENED_AT_FORMAT.format(new Date()));
        appendLine(filename, "");
    }

    @Subscribe
    public void handleChannelClosed(final ChannelClosedEvent event) {
        final String filename = getLogFile(event.getChannel());

        appendLine(filename, "*** Channel closed at: %s", OPENED_AT_FORMAT.format(new Date()));
        if (openFiles.containsKey(filename)) {
            StreamUtils.close(openFiles.get(filename).writer);
            openFiles.remove(filename);
        }
    }

    /**
     * Add a backbuffer to a frame.
     *
     * @param frame    The frame to add the backbuffer lines to
     * @param filename File to get backbuffer from
     */
    protected void showBackBuffer(final FrameContainer frame, final String filename) {
        if (frame == null) {
            Logger.userError(ErrorLevel.LOW, "Given a null frame");
            return;
        }

        final File testFile = new File(filename);
        if (testFile.exists()) {
            try {
                final ReverseFileReader file = new ReverseFileReader(testFile);
                // Because the file includes a newline char at the end, an empty line
                // is returned by getLines. To counter this, we call getLines(1) and do
                // nothing with the output.
                file.getLines(1);
                final Stack<String> lines = file.getLines(backbufferLines);
                while (!lines.empty()) {
                    frame.addLine(getColouredString(colour, lines.pop()), backbufferTimestamp);
                }
                file.close();
                frame.addLine(getColouredString(colour, "--- End of backbuffer\n"),
                        backbufferTimestamp);
            } catch (IOException | SecurityException e) {
                Logger.userError(ErrorLevel.LOW, "Unable to show backbuffer (Filename: " + filename
                        + "): " + e.getMessage());
            }
        }
    }

    /**
     * Get a coloured String. If colour is invalid, IRC Colour 14 will be used.
     *
     * @param colour The colour the string should be (IRC Colour or 6-digit hex colour)
     * @param line   the line to colour
     *
     * @return The given line with the appropriate irc codes appended/prepended to colour it.
     */
    protected static String getColouredString(final String colour, final String line) {
        String res = null;
        if (colour.length() < 3) {
            int num;

            try {
                num = Integer.parseInt(colour);
            } catch (NumberFormatException ex) {
                num = -1;
            }

            if (num >= 0 && num <= 15) {
                res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, num, line);
            }
        } else if (colour.length() == 6) {
            try {
                Color.decode("#" + colour);
                res = String.format("%c%s%s%1$c", Styliser.CODE_HEXCOLOUR, colour, line);
            } catch (NumberFormatException ex) { /* Do Nothing */ }
        }

        if (res == null) {
            res = String.format("%c%02d%s%1$c", Styliser.CODE_COLOUR, 14, line);
        }
        return res;
    }

    /**
     * Add a line to a file.
     *
     * @param filename Name of file to write to
     * @param format   Format of line to add. (NewLine will be added Automatically)
     * @param args     Arguments for format
     *
     * @return true on success, else false.
     */
    protected boolean appendLine(final String filename, final String format, final Object... args) {
        return appendLine(filename, String.format(format, args));
    }

    /**
     * Add a line to a file.
     *
     * @param filename Name of file to write to
     * @param line     Line to add. (NewLine will be added Automatically)
     *
     * @return true on success, else false.
     */
    protected boolean appendLine(final String filename, final String line) {
        final StringBuffer finalLine = new StringBuffer();

        if (addtime) {
            String dateString;
            try {
                final DateFormat dateFormat = new SimpleDateFormat(timestamp);
                dateString = dateFormat.format(new Date()).trim();
            } catch (IllegalArgumentException iae) {
                // Default to known good format
                final DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");
                dateString = dateFormat.format(new Date()).trim();

                Logger.userError(ErrorLevel.LOW, "Dateformat String '" + timestamp
                        + "' is invalid. For more information: http://java.sun.com/javase/6/docs/api/java/text/SimpleDateFormat.html");
            }
            finalLine.append(dateString);
            finalLine.append(' ');
        }

        if (stripcodes) {
            finalLine.append(Styliser.stipControlCodes(line));
        } else {
            finalLine.append(line);
        }

        BufferedWriter out;
        try {
            if (openFiles.containsKey(filename)) {
                final OpenFile of = openFiles.get(filename);
                of.lastUsedTime = System.currentTimeMillis();
                out = of.writer;
            } else {
                out = new BufferedWriter(new FileWriter(filename, true));
                openFiles.put(filename, new OpenFile(out));
            }
            out.write(finalLine.toString());
            out.newLine();
            out.flush();
            return true;
        } catch (IOException e) {
            /*
             * Do Nothing
             *
             * Makes no sense to keep adding errors to the logger when we can't write to the file,
             * as chances are it will happen on every incomming line.
             */
        }
        return false;
    }

    /**
     * Get the name of the log file for a specific object.
     *
     * @param obj Object to get name for
     *
     * @return the name of the log file to use for this object.
     */
    protected String getLogFile(final Object obj) {
        final StringBuffer directory = new StringBuffer();
        final StringBuffer file = new StringBuffer();
        String md5String = "";

        directory.append(directoryProvider.get());
        if (directory.charAt(directory.length() - 1) != File.separatorChar) {
            directory.append(File.separatorChar);
        }

        if (obj == null) {
            file.append("null.log");
        } else if (obj instanceof ChannelInfo) {
            final ChannelInfo channel = (ChannelInfo) obj;
            if (channel.getParser() != null) {
                addNetworkDir(directory, file, channel.getParser().getNetworkName());
            }
            file.append(sanitise(channel.getName().toLowerCase()));
            md5String = channel.getName();
        } else if (obj instanceof ClientInfo) {
            final ClientInfo client = (ClientInfo) obj;
            if (client.getParser() != null) {
                addNetworkDir(directory, file, client.getParser().getNetworkName());
            }
            file.append(sanitise(client.getNickname().toLowerCase()));
            md5String = client.getNickname();
        } else {
            file.append(sanitise(obj.toString().toLowerCase()));
            md5String = obj.toString();
        }

        if (usedate) {
            final String dateFormat = usedateformat;
            final String dateDir = new SimpleDateFormat(dateFormat).format(new Date());
            directory.append(dateDir);
            if (directory.charAt(directory.length() - 1) != File.separatorChar) {
                directory.append(File.separatorChar);
            }

            if (!new File(directory.toString()).exists() && !(new File(directory.toString())).
                    mkdirs()) {
                Logger.userError(ErrorLevel.LOW, "Unable to create date dirs");
            }
        }

        if (filenamehash) {
            file.append('.');
            file.append(md5(md5String));
        }
        file.append(".log");

        return directory.toString() + file.toString();
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
        final File dir = new File(directory.toString() + network + System.getProperty(
                "file.separator"));
        if (dir.exists() && !dir.isDirectory()) {
            Logger.userError(ErrorLevel.LOW,
                    "Unable to create networkfolders dir (file exists instead)");
            // Prepend network name to file instead.
            prependNetwork = true;
        } else if (!dir.exists() && !dir.mkdirs()) {
            Logger.userError(ErrorLevel.LOW, "Unable to create networkfolders dir");
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
        return name.replaceAll("[^\\w\\.\\s\\-\\#\\&\\_]", "_");
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

    /**
     * Get name to display for client.
     *
     * @param client The client to get the display name for
     *
     * @return name to display
     */
    protected String getDisplayName(final ClientInfo client) {
        return getDisplayName(client, "");
    }

    /**
     * Get name to display for client.
     *
     * @param client       The client to get the display name for
     * @param overrideNick Nickname to display instead of real nickname
     *
     * @return name to display
     */
    protected String getDisplayName(final ClientInfo client, final String overrideNick) {
        if (overrideNick.isEmpty()) {
            return (client == null) ? "Unknown Client" : client.getNickname();
        } else {
            return overrideNick;
        }
    }

    /**
     * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
     *
     * @param channelClient The client to get the display name for
     *
     * @return name to display
     */
    protected String getDisplayName(final ChannelClientInfo channelClient) {
        return getDisplayName(channelClient, "");
    }

    /**
     * Get name to display for channelClient (Taking into account the channelmodeprefix setting).
     *
     * @param channelClient The client to get the display name for
     * @param overrideNick  Nickname to display instead of real nickname
     *
     * @return name to display
     */
    protected String getDisplayName(final ChannelClientInfo channelClient, final String overrideNick) {
        if (channelClient == null) {
            return (overrideNick.isEmpty()) ? "Unknown Client" : overrideNick;
        } else if (overrideNick.isEmpty()) {
            return channelmodeprefix ? channelClient.toString() : channelClient.getClient().
                    getNickname();
        } else {
            return channelmodeprefix ? channelClient.getImportantModePrefix() + overrideNick
                    : overrideNick;
        }
    }

    /**
     * Shows the history window for the specified target, if available.
     *
     * @param target The window whose history we're trying to open
     *
     * @return True if the history is available, false otherwise
     */
    protected boolean showHistory(final FrameContainer target) {
        Object component;

        if (target instanceof Channel) {
            component = ((Channel) target).getChannelInfo();
        } else if (target instanceof Query) {
            final Parser parser = ((Query) target).getConnection().getParser();
            component = parser.getClient(((Query) target).getHost());
        } else if (target instanceof Connection) {
            component = ((Connection) target).getParser();
        } else {
            // Unknown component
            return false;
        }

        final String log = getLogFile(component);

        if (!new File(log).exists()) {
            // File doesn't exist
            return false;
        }

        ReverseFileReader reader;

        try {
            reader = new ReverseFileReader(log);
        } catch (IOException | SecurityException ex) {
            return false;
        }

        final HistoryWindow window = new HistoryWindow("History", reader, target, urlBuilder,
                eventBus, historyLines);
        windowManager.addWindow(target, window);

        return true;
    }

    /** Updates cached settings. */
    public void setCachedSettings() {
        networkfolders = config.getOptionBool(domain, "general.networkfolders");
        filenamehash = config.getOptionBool(domain, "advanced.filenamehash");
        addtime = config.getOptionBool(domain, "general.addtime");
        stripcodes = config.getOptionBool(domain, "general.stripcodes");
        channelmodeprefix = config.getOptionBool(domain, "general.channelmodeprefix");
        autobackbuffer = config.getOptionBool(domain, "backbuffer.autobackbuffer");
        backbufferTimestamp = config.getOptionBool(domain, "backbuffer.timestamp");
        usedate = config.getOptionBool(domain, "advanced.usedate");
        timestamp = config.getOption(domain, "general.timestamp");
        usedateformat = config.getOption(domain, "advanced.usedateformat");
        historyLines = config.getOptionInt(domain, "history.lines");
        colour = config.getOption(domain, "backbuffer.colour");
        backbufferLines = config.getOptionInt(domain, "backbuffer.lines");
    }

    /** Open File. */
    private static class OpenFile {

        /** Last used time. */
        public long lastUsedTime = System.currentTimeMillis();
        /** Open file's writer. */
        public final BufferedWriter writer;

        /**
         * Creates a new open file.
         *
         * @param writer Writer that has file open
         */
        protected OpenFile(final BufferedWriter writer) {
            this.writer = writer;
        }

    }

}

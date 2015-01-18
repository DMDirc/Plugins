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

package com.dmdirc.addons.dcc;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.dcc.events.DccChatRequestEvent;
import com.dmdirc.addons.dcc.events.DccSendRequestEvent;
import com.dmdirc.addons.dcc.io.DCC;
import com.dmdirc.addons.dcc.io.DCCChat;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.addons.dcc.kde.KFileChooser;
import com.dmdirc.addons.dcc.ui.PlaceholderPanel;
import com.dmdirc.addons.dcc.ui.TransferPanel;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.components.frames.ComponentFrameFactory;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.prefs.PluginPreferencesCategory;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.config.prefs.PreferencesSetting;
import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.events.ClientPrefsOpenedEvent;
import com.dmdirc.events.ServerCtcpEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.User;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.TabCompleterFactory;
import com.dmdirc.ui.messages.BackBufferFactory;
import com.dmdirc.ui.messages.sink.MessageSinkManager;

import com.google.common.collect.Sets;

import java.awt.Dialog;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.engio.mbassy.listener.Handler;

/**
 * This plugin adds DCC to DMDirc.
 */
@Singleton
public class DCCManager {

    private final BackBufferFactory backBufferFactory;
    /** Our DCC Container window. */
    private PlaceholderContainer container;
    /** Config manager to read settings from. */
    private final AggregateConfigProvider config;
    /** The sink manager to use to dispatch messages. */
    private final MessageSinkManager messageSinkManager;
    /** Window Management. */
    private final WindowManager windowManager;
    /** The command controller to use. */
    private final CommandController commandController;
    /** The factory to use for tab completers. */
    private final TabCompleterFactory tabCompleterFactory;
    /** The client's main window that will parent any new windows. */
    private final Window mainWindow;
    /** The configuration domain to use. */
    private final String domain;
    /** The bus to dispatch events on. */
    private final DMDircMBassador eventBus;
    /** Plugin info. */
    private final PluginInfo pluginInfo;

    /**
     * Creates a new instance of this plugin.
     */
    @Inject
    public DCCManager(
            @MainWindow final Window mainWindow,
            @PluginDomain(DCCPlugin.class) final PluginInfo pluginInfo,
            final IdentityController identityController,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final CommandController commandController,
            final MessageSinkManager messageSinkManager,
            final WindowManager windowManager,
            final TabCompleterFactory tabCompleterFactory,
            final SwingWindowFactory windowFactory,
            final ComponentFrameFactory componentFrameFactory,
            final DMDircMBassador eventBus,
            final GlobalCommandParser commandParser,
            @Directory(DirectoryType.BASE) final String baseDirectory,
            final BackBufferFactory backBufferFactory) {
        this.mainWindow = mainWindow;
        this.messageSinkManager = messageSinkManager;
        this.windowManager = windowManager;
        this.commandController = commandController;
        this.tabCompleterFactory = tabCompleterFactory;
        this.pluginInfo = pluginInfo;
        this.domain = pluginInfo.getDomain();
        this.config = globalConfig;
        this.eventBus = eventBus;
        this.backBufferFactory = backBufferFactory;

        windowFactory.registerImplementation(new ComponentFrameWindowProvider(
                "com.dmdirc.addons.dcc.ui.PlaceholderPanel", componentFrameFactory,
                commandParser, PlaceholderPanel::new));
        windowFactory.registerImplementation(new ComponentFrameWindowProvider(
                "com.dmdirc.addons.dcc.ui.TransferPanel", componentFrameFactory,
                commandParser, () -> new TransferPanel(container, eventBus)));

        final ConfigProvider defaults = identityController.getAddonSettings();
        defaults.setOption(domain, "receive.savelocation",
                baseDirectory + "downloads" + File.separator);
    }

    @Handler
    public void handlePrefsOpened(final ClientPrefsOpenedEvent event) {
        final PreferencesDialogModel manager = event.getModel();
        final PreferencesCategory general = new PluginPreferencesCategory(
                pluginInfo, "DCC", "", "category-dcc");
        final PreferencesCategory firewall = new PluginPreferencesCategory(
                pluginInfo, "Firewall", "");
        final PreferencesCategory sending = new PluginPreferencesCategory(
                pluginInfo, "Sending", "");
        final PreferencesCategory receiving = new PluginPreferencesCategory(
                pluginInfo, "Receiving", "");

        manager.getCategory("Plugins").addSubCategory(general.setInlineAfter());
        general.addSubCategory(firewall.setInline());
        general.addSubCategory(sending.setInline());
        general.addSubCategory(receiving.setInline());

        firewall.addSetting(
                new PreferencesSetting(PreferencesType.TEXT, pluginInfo.getDomain(), "firewall.ip",
                        "Forced IP", "What IP should be sent as our IP (Blank = work it out)",
                        manager.getConfigManager(), manager.getIdentity()));
        firewall.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, pluginInfo.getDomain(),
                "firewall.ports.usePortRange", "Use Port Range",
                "Useful if you have a firewall that only forwards specific " + "ports",
                manager.getConfigManager(), manager.getIdentity()));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "firewall.ports.startPort", "Start Port",
                "Port to try to listen on first", manager.getConfigManager(),
                manager.getIdentity()));
        firewall.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "firewall.ports.endPort", "End Port",
                "Port to try to listen on last", manager.getConfigManager(),
                manager.getIdentity()));
        receiving.addSetting(new PreferencesSetting(PreferencesType.DIRECTORY,
                pluginInfo.getDomain(), "receive.savelocation", "Default save location",
                "Where the save as window defaults to?",
                manager.getConfigManager(), manager.getIdentity()));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "send.reverse", "Reverse DCC",
                "With reverse DCC, the sender connects rather than "
                        + "listens like normal dcc", manager.getConfigManager(),
                manager.getIdentity()));
        sending.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "send.forceturbo", "Use Turbo DCC",
                "Turbo DCC doesn't wait for ack packets. this is "
                        + "faster but not always supported.",
                manager.getConfigManager(), manager.getIdentity()));
        receiving.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                pluginInfo.getDomain(), "receive.reverse.sendtoken",
                "Send token in reverse receive",
                "If you have problems with reverse dcc receive resume,"
                        + " try toggling this.", manager.getConfigManager(),
                manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                pluginInfo.getDomain(), "send.blocksize", "Blocksize to use for DCC",
                "Change the block size for send/receive, this can "
                        + "sometimes speed up transfers.", manager.getConfigManager(),
                manager.getIdentity()));
        general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, pluginInfo.getDomain(),
                "general.percentageInTitle", "Show percentage of transfers in the window title",
                "Show the current percentage of transfers in the DCC window " + "title",
                manager.getConfigManager(), manager.getIdentity()));
    }

    public String getDomain() {
        return domain;
    }

    /**
     * Ask the location to save a file, then start the download.
     *
     * @param nickname Person this dcc is from.
     * @param send     The DCCSend to save for.
     * @param parser   The parser this send was received on
     * @param reverse  Is this a reverse dcc?
     * @param token    Token used in reverse dcc.
     */
    public void saveFile(final String nickname, final DCCTransfer send,
            final Parser parser, final boolean reverse, final String token) {
        // New thread to ask the user where to save in to stop us locking the UI
        new Thread(() -> {
            final JFileChooser jc = KFileChooser.getFileChooser(config, this,
                    config.getOption(getDomain(), "receive.savelocation"));
            final int result;
            if (config.getOptionBool(getDomain(), "receive.autoaccept")) {
                result = JFileChooser.APPROVE_OPTION;
            } else {
                result = showFileChooser(send, jc);
            }
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            send.setFileName(jc.getSelectedFile().getPath());
            if (!handleExists(send, jc, nickname, parser, reverse, token)) {
                return;
            }
            final boolean resume = handleResume(jc);
            if (reverse && !token.isEmpty()) {
                final TransferContainer container1 = new TransferContainer(this, send,
                        config, backBufferFactory, "*Receive: " + nickname, nickname, null,
                        eventBus);
                windowManager.addWindow(getContainer(), container1);
                send.setToken(token);
                if (resume) {
                    if (config.getOptionBool(getDomain(),
                            "receive.reverse.sendtoken")) {
                        parser.sendCTCP(nickname, "DCC", "RESUME "
                                + send.getShortFileName() + " 0 "
                                + jc.getSelectedFile().length() + ' '
                                + token);
                    } else {
                        parser.sendCTCP(nickname, "DCC", "RESUME "
                                + send.getShortFileName() + " 0 "
                                + jc.getSelectedFile().length());
                    }
                } else {
                    if (listen(send)) {
                        parser.sendCTCP(nickname, "DCC", "SEND "
                                + send.getShortFileName() + ' '
                                + DCC.ipToLong(getListenIP(parser))
                                + ' ' + send.getPort() + ' '
                                + send.getFileSize() + ' ' + token);
                    }
                }
            } else {
                final TransferContainer container1 = new TransferContainer(this, send,
                        config, backBufferFactory, "Receive: " + nickname, nickname, null,
                        eventBus);
                windowManager.addWindow(getContainer(), container1);
                if (resume) {
                    parser.sendCTCP(nickname, "DCC", "RESUME "
                            + send.getShortFileName() + ' '
                            + send.getPort() + ' '
                            + jc.getSelectedFile().length());
                } else {
                    send.connect();
                }
            }
        }, "saveFileThread: " + send.getShortFileName()).start();
    }

    /**
     * Checks if the selected file exists and prompts the user as required.
     *
     * @param send     DCC Transfer
     * @param jc       File chooser
     * @param nickname Remote nickname
     * @param parser   Parser
     * @param reverse  Reverse DCC?
     * @param token    DCC token
     *
     * @return true if the user wants to continue, false if they wish to abort
     */
    private boolean handleExists(final DCCTransfer send, final JFileChooser jc,
            final String nickname, final Parser parser, final boolean reverse,
            final String token) {
        if (jc.getSelectedFile().exists() && send.getFileSize() > -1
                && send.getFileSize() <= jc.getSelectedFile().length()) {
            if (config.getOptionBool(getDomain(), "receive.autoaccept")) {
                return false;
            } else {
                JOptionPane.showMessageDialog(
                        mainWindow,
                        "This file has already "
                        + "been completed, or is longer than the file you are "
                        + "receiving.\nPlease choose a different file.",
                        "Problem with selected file",
                        JOptionPane.ERROR_MESSAGE);
                saveFile(nickname, send, parser, reverse, token);
                return false;
            }
        }
        return true;
    }

    /**
     * Prompts the user to resume a transfer if required.
     *
     * @param jc File chooser
     *
     * @return true if the user wants to continue the transfer false otherwise
     */
    private boolean handleResume(final JFileChooser jc) {
        if (jc.getSelectedFile().exists()) {
            if (config.getOptionBool(getDomain(), "receive.autoaccept")) {
                return true;
            } else {
                final int result = JOptionPane.showConfirmDialog(
                        mainWindow, "This file exists already"
                        + ", do you want to resume an exisiting download?",
                        "Resume Download?", JOptionPane.YES_NO_OPTION);
                return result == JOptionPane.YES_OPTION;
            }
        }
        return false;
    }

    /**
     * Sets up and display a file chooser.
     *
     * @param send DCCTransfer object sending the file
     * @param jc   File chooser
     *
     * @return the return state of the file chooser on popdown:
     * <ul>
     * <li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileChooser.ERROR_OPTION if an error occurs or the dialog is dismissed
     * </ul>
     */
    private int showFileChooser(final DCCTransfer send, final JFileChooser jc) {
        jc.setDialogTitle("Save " + send.getShortFileName() + " As - DMDirc");
        jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jc.setMultiSelectionEnabled(false);
        jc.setSelectedFile(new File(send.getFileName()));
        return jc.showSaveDialog(mainWindow);
    }

    /**
     * Make the given DCC start listening. This will either call dcc.listen() or
     * dcc.listen(startPort, endPort) depending on config.
     *
     * @param dcc DCC to start listening.
     *
     * @return True if Socket was opened.
     */
    protected boolean listen(final DCC dcc) {
        final boolean usePortRange = config.
                getOptionBool(getDomain(), "firewall.ports.usePortRange");
        try {
            if (usePortRange) {
                final int startPort = config.getOptionInt(getDomain(), "firewall.ports.startPort");
                final int endPort = config.getOptionInt(getDomain(), "firewall.ports.endPort");
                dcc.listen(startPort, endPort);
            } else {
                dcc.listen();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    @Handler
    public void handleServerCtctpEvent(final ServerCtcpEvent event) {
        final boolean autoAccept = config.getOptionBool(getDomain(), "receive.autoaccept");
        final String[] ctcpData = event.getContent().split(" ");
        if (!"DCC".equalsIgnoreCase(event.getType())) {
            return;
        }
        switch (event.getType().toLowerCase()) {
            case "chat":
                if (ctcpData.length > 3) {
                    handleChat(autoAccept, ctcpData, event.getUser(), event.getConnection());
                }
                break;
            case "send":
                if (ctcpData.length > 3) {
                    handleSend(autoAccept, ctcpData, event.getUser(), event.getConnection());
                }
                break;
            case "resume":
            //Fallthrough
            case "accept":
                if (ctcpData.length > 2) {
                    handleReceive(ctcpData, event.getUser(), event.getConnection());
                }
                break;
            default:
                break;
        }
    }

    /**
     * Handles a DCC chat request.
     *
     * @param dontAsk    Don't ask any questions, assume yes.
     * @param ctcpData   CTCP data bits
     * @param client     Client receiving DCC
     * @param connection Connection DCC received on
     */
    private void handleChat(final boolean dontAsk, final String[] ctcpData,
            final User client, final Connection connection) {
        final String nickname = client.getNickname();
        if (dontAsk) {
            handleDCCChat(connection.getParser().get(), nickname, ctcpData);
        } else {
            eventBus.publish(new DccChatRequestEvent(connection, nickname));
            new StandardQuestionDialog(mainWindow, Dialog.ModalityType.APPLICATION_MODAL,
                    "DCC Chat Request", "User " + nickname + " on " + connection.getAddress()
                    + " would like to start a DCC Chat with you.\n\nDo you want to continue?",
                    () -> handleDCCChat(connection.getParser().get(), nickname, ctcpData)).display();
        }
    }

    void handleDCCChat(final Parser parser, final String nickname, final String[] ctcpData) {
        final long ipAddress;
        final int port;
        try {
            ipAddress = Long.parseLong(ctcpData[2]);
            port = Integer.parseInt(ctcpData[3]);
        } catch (NumberFormatException nfe) {
            return;
        }
        final DCCChat chat = new DCCChat();
        chat.setAddress(ipAddress, port);
        final String myNickname = parser.getLocalClient().getNickname();
        final DCCFrameContainer f = new ChatContainer(
                getContainer(),
                chat,
                config,
                backBufferFactory,
                commandController,
                "Chat: " + nickname,
                myNickname,
                nickname,
                tabCompleterFactory,
                messageSinkManager,
                eventBus);
        windowManager.addWindow(getContainer(), f);
        f.addLine("DCCChatStarting", nickname, chat.getHost(), chat.getPort());
        chat.connect();
    }

    /**
     * Handles a DCC send request.
     *
     * @param dontAsk    Don't ask any questions, assume yes.
     * @param ctcpData   CTCP data bits
     * @param client     Client that received the DCC
     * @param connection Connection the DCC was received on
     */
    private void handleSend(final boolean dontAsk, final String[] ctcpData, final User client,
            final Connection connection) {
        final String nickname = client.getNickname();
        String tmpFilename;
        // Clients tend to put files with spaces in the name in ""
        final StringBuilder filenameBits = new StringBuilder();
        int i;
        final boolean quoted = ctcpData[1].startsWith("\"");
        if (quoted) {
            for (i = 1; i < ctcpData.length; i++) {
                String bit = ctcpData[i];
                if (i == 1) {
                    bit = bit.substring(1);
                }
                if (bit.endsWith("\"")) {
                    filenameBits.append(' ').append(bit.substring(0, bit.length() - 1));
                    break;
                } else {
                    filenameBits.append(' ').append(bit);
                }
            }
            tmpFilename = filenameBits.toString().trim();
        } else {
            tmpFilename = ctcpData[1];
            i = 1;
        }

        // Try to remove path names if sent.
        // Change file separatorChar from other OSs first
        if (File.separatorChar == '/') {
            tmpFilename = tmpFilename.replace('\\', File.separatorChar);
        } else {
            tmpFilename = tmpFilename.replace('/', File.separatorChar);
        }
        // Then get just the name of the file.
        final String filename = new File(tmpFilename).getName();

        final String ip = ctcpData[++i];
        final String port = ctcpData[++i];
        long size;
        if (ctcpData.length + 1 > i) {
            try {
                size = Integer.parseInt(ctcpData[++i]);
            } catch (NumberFormatException nfe) {
                size = -1;
            }
        } else {
            size = -1;
        }
        final String token = ctcpData.length - 1 > i
                && !"T".equals(ctcpData[i + 1]) ? ctcpData[++i] : "";

        // Ignore incorrect ports, or non-numeric IP/Port
        final long ipLong;
        final int portInt;
        try {
            portInt = Integer.parseInt(port);
            if (portInt > 65535 || portInt < 0) {
                return;
            }
            ipLong = Long.parseLong(ip);
        } catch (NumberFormatException nfe) {
            return;
        }

        if (DCCTransfer.findByToken(token) == null && !dontAsk &&
                (token.isEmpty() || "0".equals(port))) {
            // Make sure this is not a reverse DCC Send that we no longer care about.
            eventBus.publish(new DccSendRequestEvent(connection, nickname, filename));
            final long passedSize = size;
            new StandardQuestionDialog(mainWindow, Dialog.ModalityType.APPLICATION_MODAL,
                    "DCC Send Request", "User " + nickname + " on " + connection.getAddress()
                            + " would like to send you a file over DCC.\n\nFile: "
                            + filename + "\n\nDo you want to continue?",
                    () -> handleDCCSend(token, ipLong, portInt, filename, passedSize, nickname,
                            connection.getParser().get())).display();
        }
    }

    void handleDCCSend(final String token, final long ip, final int port, final String filename,
            final long size, final String nickname, final Parser parser) {
        DCCTransfer send = DCCTransfer.findByToken(token);
        final boolean newSend = send == null;
        if (newSend) {
            send = new DCCTransfer(config.getOptionInt(getDomain(), "send.blocksize"));
            send.setTurbo(config.getOptionBool(getDomain(), "send.forceturbo"));
        } else {
            return;
        }
        send.setAddress(ip, port);
        send.setFileName(filename);
        send.setFileSize(size);
        saveFile(nickname, send, parser, port == 0, token);
    }

    /**
     * Handles a DCC chat request.
     *
     * @param ctcpData   CTCP data bits
     * @param client     Client receiving the DCC
     * @param connection Connection the DCC was received on
     */
    private void handleReceive(final String[] ctcpData, final User client,
            final Connection connection) {
        final String filename;
        // Clients tend to put files with spaces in the name in ""
        final StringBuilder filenameBits = new StringBuilder();
        int i;
        final boolean quoted = ctcpData[1].startsWith("\"");
        if (quoted) {
            for (i = 1; i < ctcpData.length; i++) {
                String bit = ctcpData[i];
                if (i == 1) {
                    bit = bit.substring(1);
                }
                if (bit.endsWith("\"")) {
                    filenameBits.append(' ')
                            .append(bit.substring(0, bit.length() - 1));
                    break;
                } else {
                    filenameBits.append(' ').append(bit);
                }
            }
            filename = filenameBits.toString().trim();
        } else {
            filename = ctcpData[1];
            i = 1;
        }

        final int port;
        final int position;
        try {
            port = Integer.parseInt(ctcpData[++i]);
            position = Integer.parseInt(ctcpData[++i]);
        } catch (NumberFormatException nfe) {
            return;
        }
        final String token = ctcpData.length - 1 > i ? ' '
                + ctcpData[++i] : "";

        // Now look for a dcc that matches.
        for (DCCTransfer send : DCCTransfer.getTransfers()) {
            if (send.getPort() == port && new File(send.getFileName())
                    .getName().equalsIgnoreCase(filename)) {
                if (!token.isEmpty() && !send.getToken().isEmpty() &&
                        !token.equals(send.getToken())) {
                    continue;
                }
                final Parser parser = connection.getParser().get();
                final String nick = client.getNickname();
                if ("resume".equalsIgnoreCase(ctcpData[0])) {
                    parser.sendCTCP(nick, "DCC", "ACCEPT " + (quoted ? '"'
                            + filename + '"' : filename) + ' ' + port + ' '
                            + send.setFileStart(position) + token);
                } else {
                    send.setFileStart(position);
                    if (port == 0) {
                        // Reverse dcc
                        if (listen(send)) {
                            if (send.getToken().isEmpty()) {
                                parser.sendCTCP(nick, "DCC", "SEND "
                                        + (quoted ? '"' + filename
                                        + '"' : filename) + ' '
                                        + DCC.ipToLong(send.getHost())
                                        + ' ' + send.getPort()
                                        + ' ' + send.getFileSize());
                            } else {
                                parser.sendCTCP(nick, "DCC", "SEND "
                                        + (quoted ? '"' + filename
                                        + '"' : filename)
                                        + ' ' + DCC.ipToLong(send.getHost())
                                        + ' ' + send.getPort()
                                        + ' ' + send.getFileSize() + ' '
                                        + send.getToken());
                            }
                        }
                    } else {
                        send.connect();
                    }
                }
            }
        }
    }

    /**
     * Retrieves the container for the placeholder.
     *
     * @since 0.6.4
     * @return This plugin's placeholder container
     */
    public synchronized PlaceholderContainer getContainer() {
        if (container == null) {
            createContainer();
        }

        return container;
    }

    /**
     * Removes the cached container.
     *
     * @since 0.6.4
     */
    public synchronized void removeContainer() {
        container = null;
    }

    /**
     * Create the container window.
     */
    protected void createContainer() {
        container = new PlaceholderContainer(this, config, backBufferFactory, mainWindow, eventBus);
        windowManager.addWindow(container);
    }

    /**
     * Called when the plugin is loaded.
     */
    public void onLoad() {
        final File dir = new File(config.getOption(getDomain(),
                "receive.savelocation"));
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                        "Unable to create download dir (file exists instead)", ""));
            }
        } else {
            try {
                dir.mkdirs();
                dir.createNewFile();
            } catch (IOException ex) {
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, null,
                        "Unable to create download dir", ""));
            }
        }

        eventBus.subscribe(this);
    }

    /**
     * Called when this plugin is Unloaded.
     */
    public synchronized void onUnload() {
        eventBus.unsubscribe(this);
        if (container != null) {
            container.close();
        }
    }

    /**
     * Get the IP Address we should send as our listening IP.
     *
     * @param parser Parser the IRC Parser where this dcc is initiated
     *
     * @return The IP Address we should send as our listening IP.
     */
    public String getListenIP(final Parser parser) {
        final String configIP = config.getOption(getDomain(), "firewall.ip");
        if (!configIP.isEmpty()) {
            try {
                return InetAddress.getByName(configIP).getHostAddress();
            } catch (UnknownHostException ex) { //NOPMD - handled below
                //Continue below
            }
        }
        if (parser != null) {
            final String myHost = parser.getLocalClient().getHostname();
            if (!myHost.isEmpty()) {
                try {
                    return InetAddress.getByName(myHost).getHostAddress();
                } catch (UnknownHostException e) { //NOPMD - handled below
                    //Continue below
                }
            }
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // This is almost certainly not what we want, but we can't work out
            // the right one.
            return "127.0.0.1"; //NOPMD
        }
    }

    private static class ComponentFrameWindowProvider implements SwingWindowFactory.WindowProvider {

        private final String component;
        private final ComponentFrameFactory componentFrameFactory;
        private final CommandParser commandParser;
        private final Supplier<? extends JComponent> componentSupplier;

        ComponentFrameWindowProvider(final String component,
                final ComponentFrameFactory componentFrameFactory,
                final CommandParser commandParser,
                final Supplier<? extends JComponent> componentSupplier) {
            this.component = component;
            this.componentFrameFactory = componentFrameFactory;
            this.commandParser = commandParser;
            this.componentSupplier = componentSupplier;
        }

        @Override
        public TextFrame getWindow(final WindowModel container) {
            return componentFrameFactory.getComponentFrame(container, commandParser,
                    Collections.singletonList(componentSupplier));
        }

        @Override
        public Set<String> getComponents() {
            return Sets.newHashSet(component);
        }
    }

}

/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.dcc.actions.DCCActions;
import com.dmdirc.addons.dcc.io.DCC;
import com.dmdirc.addons.dcc.io.DCCChat;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.addons.dcc.kde.KFileChooser;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.commandparser.commands.context.ServerCommandContext;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import java.io.File;
import java.util.concurrent.Callable;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This command allows starting dcc chats/file transfers.
 */
public class DCCCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("dcc",
            "dcc <SEND|CHAT> <target> [params] - starts a DCC",
            CommandType.TYPE_SERVER);

    /** My Plugin. */
    private final DCCPlugin myPlugin;
    /** Main frame instance used as the parent for dialogs. */
    private final MainFrame mainFrame;
    /** Window management. */
    private final WindowManager windowManager;

    /**
     * Creates a new instance of DCCCommand.
     *
     * @param mainFrame mainFrame instance to use
     * @param plugin The DCC Plugin that this command belongs to
     * @param windowManager Window management
     */
    public DCCCommand(final MainFrame mainFrame, final DCCPlugin plugin,
            final WindowManager windowManager) {
        this.mainFrame = mainFrame;
        myPlugin = plugin;
        this.windowManager = windowManager;
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final FrameContainer origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length > 1) {
            final String target = args.getArguments()[1];
            final Server server = ((ServerCommandContext) context).getServer();
            final Parser parser = server.getParser();
            final String myNickname = parser.getLocalClient().getNickname();

            if (parser.isValidChannelName(target)
                    || parser.getStringConverter().equalsIgnoreCase(target,
                    myNickname)) {
                new Thread(new Runnable() {

                    /** {@inheritDoc} */
                    @Override
                    public void run() {
                        if (parser.getStringConverter().equalsIgnoreCase(target,
                                myNickname)) {
                            JOptionPane.showMessageDialog(null,
                                    "You can't DCC yourself.", "DCC Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "You can't DCC a channel.", "DCC Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }

                }, "DCC-Error-Message").start();
                return;
            }
            final String type = args.getArguments()[0];
            if (type.equalsIgnoreCase("chat")) {
                startChat(parser, server, origin, myNickname, target, true);
            } else if (type.equalsIgnoreCase("send")) {
                sendFile(target, origin, server, true,
                        args.getArgumentsAsString(2));
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "Unknown DCC Type: '" + type + "'");
            }
        } else {
            showUsage(origin, true, INFO.getName(), INFO.getHelp());
        }
    }

    /**
     * Starts a DCC Chat.
     *
     * @param parser Parser from which command originated
     * @param server Server from which command originated
     * @param origin Frame container from which command originated
     * @param myNickname My current nickname
     * @param target Target of the command
     * @param isSilent Is this a silent command
     */
    private void startChat(final Parser parser, final Server server,
            final FrameContainer origin, final String myNickname,
            final String target, final boolean isSilent) {
        final DCCChat chat = new DCCChat();
        if (myPlugin.listen(chat)) {
            final ChatContainer window = new ChatContainer(myPlugin, chat,
                    origin.getConfigManager(),
                    "*Chat: " + target, myNickname, target, windowManager);
            parser.sendCTCP(target, "DCC", "CHAT chat " + DCC.ipToLong(
                    myPlugin.getListenIP(parser)) + " " + chat.getPort());
            ActionManager.getActionManager().triggerEvent(
                    DCCActions.DCC_CHAT_REQUEST_SENT, null, server, target);
            sendLine(origin, isSilent, "DCCChatStarting", target,
                    chat.getHost(), chat.getPort());
            window.addLine("DCCChatStarting", target, chat.getHost(),
                    chat.getPort());
        } else {
            sendLine(origin, isSilent, "DCCChatError",
                    "Unable to start chat with " + target
                    + " - unable to create listen socket");
        }
    }

    /**
     * Ask for the file to send, then start the send.
     *
     * @param target Person this dcc is to.
     * @param origin The InputWindow this command was issued on
     * @param server The server instance that this command is being executed on
     * @param isSilent Whether this command is silenced or not
     * @param filename The file to send
     * @since 0.6.3m1
     */
    public void sendFile(final String target, final FrameContainer origin,
            final Server server, final boolean isSilent, final String filename) {
        // New thread to ask the user what file to send
        final File givenFile = new File(filename);
        final File selectedFile = UIUtilities.invokeAndWait(new Callable<File>() {

            @Override
            public File call() {
                final JFileChooser jc = givenFile.exists()
                        ? KFileChooser.getFileChooser(origin.getConfigManager(),
                                myPlugin, givenFile)
                        : KFileChooser.getFileChooser(origin.getConfigManager(),
                                myPlugin);
                final int result = showFileChooser(givenFile, target, jc);

                if (result != JFileChooser.APPROVE_OPTION
                        || !handleInvalidItems(jc)) {
                    return null;
                }
                return jc.getSelectedFile();
            }
        });
        if (selectedFile == null) {
            return;
        }
        new Thread(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final DCCTransfer send = new DCCTransfer(origin
                        .getConfigManager().getOptionInt(myPlugin.getDomain(),
                        "send.blocksize"));
                send.setTurbo(origin.getConfigManager().getOptionBool(
                        myPlugin.getDomain(), "send.forceturbo"));
                send.setType(DCCTransfer.TransferType.SEND);

                ActionManager.getActionManager().triggerEvent(
                        DCCActions.DCC_SEND_REQUEST_SENT,
                        null, server, target, selectedFile);

                sendLine(origin, isSilent, FORMAT_OUTPUT,
                        "Starting DCC Send with: " + target);

                send.setFileName(selectedFile.getAbsolutePath());
                send.setFileSize(selectedFile.length());

                if (origin.getConfigManager().getOptionBool(
                        myPlugin.getDomain(), "send.reverse")) {
                    final Parser parser = server.getParser();
                    new TransferContainer(myPlugin, send,
                            origin.getConfigManager(), "Send: " + target,
                            target, server, windowManager);
                    parser.sendCTCP(target, "DCC", "SEND \""
                            + selectedFile.getName() + "\" "
                            + DCC.ipToLong(myPlugin.getListenIP(parser))
                            + " 0 " + send.getFileSize() + " "
                            + send.makeToken()
                            + (send.isTurbo() ? " T" : ""));
                } else {
                    final Parser parser = server.getParser();
                    if (myPlugin.listen(send)) {
                        new TransferContainer(myPlugin, send,
                                origin.getConfigManager(), "*Send: "
                                + target, target, server, windowManager);
                        parser.sendCTCP(target, "DCC", "SEND \""
                                + selectedFile.getName() + "\" "
                                + DCC.ipToLong(myPlugin.getListenIP(parser))
                                + " " + send.getPort() + " " + send.getFileSize()
                                + (send.isTurbo() ? " T" : ""));
                    } else {
                        sendLine(origin, isSilent, "DCCSendError",
                                "Unable to start dcc send with " + target
                                + " - unable to create listen socket");
                    }
                }
            }

        }, "openFileThread").start();
    }

    /**
     * Checks for invalid items.
     *
     * @param jc File chooser to check
     *
     * @return true iif the selection was valid
     */
    private boolean handleInvalidItems(final JFileChooser jc) {
        if (jc.getSelectedFile().length() == 0) {
            JOptionPane.showMessageDialog(null,
                    "You can't send empty files over DCC.", "DCC Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (!jc.getSelectedFile().exists()) {
            JOptionPane.showMessageDialog(null, "Invalid file specified",
                    "DCC Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Sets up and display a file chooser.
     *
     * @param givenFile File to display
     * @param target DCC target
     * @param jc File chooser
     *
     * @return   the return state of the file chooser on popdown:
     * <ul>
     * <li>JFileChooser.CANCEL_OPTION
     * <li>JFileChooser.APPROVE_OPTION
     * <li>JFileChooser.ERROR_OPTION if an error occurs or the
     *                               dialog is dismissed
     * </ul>
     */
    private int showFileChooser(final File givenFile, final String target,
            final JFileChooser jc) {
        if (givenFile.exists() && givenFile.isFile()) {
            jc.setSelectedFile(givenFile);
            return JFileChooser.APPROVE_OPTION;
        } else {
        jc.setDialogTitle("Send file to " + target + " - DMDirc ");
        jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jc.setMultiSelectionEnabled(false);
        return jc.showOpenDialog(mainFrame);
        }
    }

    /** {@inheritDoc} */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
        final AdditionalTabTargets res = new AdditionalTabTargets();

        if (arg == 0) {
            res.add("SEND");
            res.add("CHAT");
            res.excludeAll();
        } else if (arg == 1) {
            res.exclude(TabCompletionType.COMMAND);
            res.exclude(TabCompletionType.CHANNEL);
        } else {
            res.excludeAll();
        }

        return res;
    }

}


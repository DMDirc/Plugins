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

package com.dmdirc.addons.dcc;

import com.dmdirc.FrameContainer;
import com.dmdirc.ServerState;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.dcc.actions.DCCActions;
import com.dmdirc.addons.dcc.io.DCC;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
import com.dmdirc.util.URLBuilder;

import com.google.common.eventbus.EventBus;

import java.awt.Desktop;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;

/**
 * This class links DCC Send objects to a window.
 */
public class TransferContainer extends FrameContainer implements
        DCCTransferHandler, SocketCloseListener {

    /** The dcc plugin that owns this frame */
    protected final DCCManager plugin;
    /** Config manager. */
    private final AggregateConfigProvider config;
    /** The Window we're using. */
    private boolean windowClosing = false;
    /** The DCCSend object we are a window for */
    private final DCCTransfer dcc;
    /** Other Nickname */
    private final String otherNickname;
    /** Total data transfered */
    private volatile long transferCount = 0;
    /** Time Started */
    private long timeStarted = 0;
    /** Plugin that this send belongs to. */
    private final DCCManager myPlugin;
    /** IRC Parser that caused this send */
    private Parser parser = null;
    /** Connection the send was initiated on. */
    private Connection connection = null;
    /** Show open button. */
    private final boolean showOpen = Desktop.isDesktopSupported()
            && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);

    /**
     * Creates a new instance of DCCTransferWindow with a given DCCTransfer object.
     *
     * @param plugin     the DCC Plugin responsible for this window
     * @param dcc        The DCCTransfer object this window wraps around
     * @param config     Config manager
     * @param title      The title of this window
     * @param targetNick Nickname of target
     * @param connection The connection that the send was that initiated on
     * @param urlBuilder The URL builder to use when finding icons.
     * @param eventBus   The bus to despatch events on.
     */
    public TransferContainer(final DCCManager plugin, final DCCTransfer dcc,
            final AggregateConfigProvider config, final String title,
            final String targetNick, final Connection connection,
            final URLBuilder urlBuilder, final EventBus eventBus) {
        super(plugin.getContainer(), dcc.getType() == DCCTransfer.TransferType.SEND
                ? "dcc-send-inactive" : "dcc-receive-inactive",
                title, title, config, urlBuilder, eventBus,
                Arrays.asList("com.dmdirc.addons.dcc.ui.TransferPanel"));
        this.plugin = plugin;
        this.dcc = dcc;
        this.connection = connection;
        this.config = config;
        parser = connection == null ? null : connection.getParser();
        myPlugin = plugin;

        if (parser != null) {
            parser.getCallbackManager().addNonCriticalCallback(
                    SocketCloseListener.class, this);
        }
        dcc.addHandler(this);

        otherNickname = targetNick;
    }

    @Override
    public void onSocketClosed(final Parser parser, final Date date) {
        // Remove our reference to the parser (and its reference to us)
        this.parser.getCallbackManager().delAllCallback(this);
        this.parser = null;
    }

    /**
     * Get the DCCSend Object associated with this window
     *
     * @return The DCCSend Object associated with this window
     */
    public DCCTransfer getDCC() {
        return dcc;
    }

    /**
     * Retrieves the nickname of the other party involved in this transfer.
     *
     * @return The other party's nickname
     *
     * @since 0.6.4
     */
    public String getOtherNickname() {
        return otherNickname;
    }

    /**
     * Called when data is sent/recieved
     *
     * @param dcc   The DCCSend that this message is from
     * @param bytes The number of new bytes that were transfered
     */
    @Override
    public void dataTransfered(final DCCTransfer dcc, final int bytes) {
        final double percent;
        synchronized (this) {
            transferCount += bytes;
            percent = getPercent();
        }

        final boolean percentageInTitle = config.getOptionBool(
                plugin.getDomain(), "general.percentageInTitle");

        if (percentageInTitle) {
            final StringBuilder title = new StringBuilder();
            if (dcc.isListenSocket()) {
                title.append("*");
            }
            title.append(dcc.getType() == DCCTransfer.TransferType.SEND
                    ? "Sending: " : "Recieving: ");
            title.append(otherNickname);
            title.append(" (")
                    .append(String.format("%.0f", Math.floor(percent)))
                    .append("%)");
            setName(title.toString());
            setTitle(title.toString());
        }

        ActionManager.getActionManager().triggerEvent(
                DCCActions.DCC_SEND_DATATRANSFERED, null, this, bytes);
    }

    /**
     * Retrieves the current percentage progress of this transfer.
     *
     * @since 0.6.4
     * @return The percentage of this transfer that has been completed
     */
    public double getPercent() {
        return (100.00 / dcc.getFileSize()) * (transferCount
                + dcc.getFileStart());
    }

    /**
     * Retrieves the current transfer speed of this transfer.
     *
     * @since 0.6.4
     * @return The speed of this transfer in Bytes/Sec
     */
    public double getBytesPerSecond() {
        final long time = getElapsedTime();

        synchronized (this) {
            return time > 0 ? ((double) transferCount / time) : transferCount;
        }
    }

    /**
     * Retrieves the estimated time remaining for this transfer.
     *
     * @since 0.6.4
     * @return The number of seconds estimated for this transfer to complete
     */
    public double getRemainingTime() {
        final double bytesPerSecond = getBytesPerSecond();
        final long remaningBytes;

        synchronized (this) {
            remaningBytes = dcc.getFileSize() - dcc.getFileStart()
                    - transferCount;
        }

        return bytesPerSecond > 0 ? (remaningBytes / bytesPerSecond) : 1;
    }

    /**
     * Retrieves the timestamp at which this transfer started.
     *
     * @since 0.6.4
     * @return The timestamp (milliseconds since 01/01/1970) at which this transfer started.
     */
    public long getStartTime() {
        return timeStarted;
    }

    /**
     * Retrieves the number of seconds that this transfer has been running for.
     *
     * @since 0.6.4
     * @return The number of seconds elapsed since this transfer started
     */
    public long getElapsedTime() {
        return (System.currentTimeMillis() - timeStarted) / 1000;
    }

    /**
     * Determines whether this transfer is complete or not.
     *
     * @since 0.6.4
     * @return True if the transfer is complete, false otherwise
     */
    public boolean isComplete() {
        return transferCount == dcc.getFileSize() - dcc.getFileStart();
    }

    /**
     * Determines whether the "Open" button should be displayed for this transfer.
     *
     * @since 0.6.4
     * @return True if the open button should be displayed, false otherwise
     */
    public boolean shouldShowOpenButton() {
        return showOpen && dcc.getType() == DCCTransfer.TransferType.RECEIVE;
    }

    /**
     * Called when the socket is closed
     *
     * @param dcc The DCCSend that this message is from
     */
    @Override
    public void socketClosed(final DCCTransfer dcc) {
        ActionManager.getActionManager().triggerEvent(
                DCCActions.DCC_SEND_SOCKETCLOSED, null, this);
        if (!windowClosing) {
            synchronized (this) {
                if (transferCount == dcc.getFileSize() - dcc.getFileStart()) {
                    setIcon(dcc.getType() == DCCTransfer.TransferType.SEND
                            ? "dcc-send-done" : "dcc-receive-done");
                } else {
                    setIcon(dcc.getType() == DCCTransfer.TransferType.SEND
                            ? "dcc-send-failed" : "dcc-receive-failed");
                }
            }
        }
    }

    /**
     * Called when the socket is opened
     *
     * @param dcc The DCCSend that this message is from
     */
    @Override
    public void socketOpened(final DCCTransfer dcc) {
        ActionManager.getActionManager().triggerEvent(
                DCCActions.DCC_SEND_SOCKETOPENED, null, this);
        timeStarted = System.currentTimeMillis();
        setIcon(dcc.getType() == DCCTransfer.TransferType.SEND
                ? "dcc-send-active" : "dcc-receive-active");
    }

    /**
     * Attempts to resend the transfer.
     *
     * @since 0.6.4
     * @return True if the transfer could be resent, false otherwise
     */
    public boolean resend() {
        synchronized (this) {
            transferCount = 0;
        }
        dcc.reset();

        if (connection != null && connection.getState() == ServerState.CONNECTED) {
            final String myNickname = connection.getParser().getLocalClient()
                    .getNickname();
            // Check again incase we have changed nickname to the same nickname
            //that this send is for.
            if (connection.getParser().getStringConverter().equalsIgnoreCase(
                    otherNickname, myNickname)) {
                final Thread errorThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null,
                                "You can't DCC yourself.", "DCC Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }, "DCC-Error-Message");
                errorThread.start();
            } else {
                if (config.getOptionBool(plugin.getDomain(), "send.reverse")) {
                    parser.sendCTCP(otherNickname, "DCC", "SEND \"" + new File(dcc.getFileName()).
                            getName() + "\" "
                            + DCC.ipToLong(myPlugin.getListenIP(parser))
                            + " 0 " + dcc.getFileSize() + " " + dcc.makeToken()
                            + ((dcc.isTurbo()) ? " T" : ""));
                } else if (plugin.listen(dcc)) {
                    parser.sendCTCP(otherNickname, "DCC", "SEND \""
                            + new File(dcc.getFileName()).getName() + "\" "
                            + DCC.ipToLong(myPlugin.getListenIP(parser)) + " "
                            + dcc.getPort() + " " + dcc.getFileSize()
                            + ((dcc.isTurbo()) ? " T" : ""));
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Closes this container (and it's associated frame).
     */
    @Override
    public void close() {
        windowClosing = true;

        super.close();

        dcc.removeFromTransfers();
    }

    public void addSocketCloseCallback(final SocketCloseListener listener) {
        if (connection != null && connection.getParser() != null) {
            connection.getParser().getCallbackManager()
                    .addNonCriticalCallback(SocketCloseListener.class,
                            listener);
        }
    }

    @Override
    public Connection getConnection() {
        return null;
    }

}

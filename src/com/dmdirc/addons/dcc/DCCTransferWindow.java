/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.dcc.actions.DCCActions;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

/**
 * This class links DCC Send objects to a window.
 *
 * @author Shane 'Dataforce' McCormack
 */
public class DCCTransferWindow extends DCCFrame implements DCCTransferHandler,
        ActionListener, SocketCloseListener {

    /** The DCCSend object we are a window for */
    private final DCCTransfer dcc;

    /** Other Nickname */
    private final String otherNickname;

    /** Total data transfered */
    private volatile long transferCount = 0;

    /** Time Started */
    private long timeStarted = 0;

    /** Progress Bar */
    private final JProgressBar progress = new JProgressBar();

    /** Status Label */
    private final JLabel status = new JLabel("Status: Waiting");

    /** Speed Label */
    private final JLabel speed = new JLabel("Speed: Unknown");

    /** Time Label */
    private final JLabel remaining = new JLabel("Time Remaining: Unknown");

    /** Time Taken */
    private final JLabel taken = new JLabel("Time Taken: 00:00");

    /** Button */
    private final JButton button = new JButton("Cancel");

    /** Open Button */
    private final JButton openButton = new JButton("Open");

    /** Plugin that this send belongs to. */
    private final DCCPlugin myPlugin;

    /** IRC Parser that caused this send */
    private Parser parser = null;

    /** Server that caused this send */
    private Server server = null;

    /** Show open button. */
    private boolean showOpen = Desktop.isDesktopSupported() &&
            Desktop.getDesktop().isSupported(Desktop.Action.OPEN);

    /**
     * Creates a new instance of DCCTransferWindow with a given DCCTransfer object.
     *
     * @param plugin the DCC Plugin responsible for this window
     * @param dcc The DCCTransfer object this window wraps around
     * @param title The title of this window
     * @param targetNick Nickname of target
     * @param server The server that initiated this send
     */
    public DCCTransferWindow(final DCCPlugin plugin, final DCCTransfer dcc,
            final String title, final String targetNick, final Server server) {
        super(plugin, title, dcc.getType() == DCCTransfer.TransferType.SEND
                ? "dcc-send-inactive" : "dcc-receive-inactive");
        this.dcc = dcc;
        this.server = server;
        this.parser = server == null ? null : server.getParser();
        this.myPlugin = plugin;

        if (parser != null) {
            parser.getCallbackManager().addNonCriticalCallback(SocketCloseListener.class, this);
        }
        dcc.setHandler(this);

        otherNickname = targetNick;

        getContentPane().setLayout(new MigLayout("hidemode 0"));

        progress.setMinimum(0);
        progress.setMaximum(100);
        progress.setStringPainted(true);
        progress.setValue(0);

        if (dcc.getType() == DCCTransfer.TransferType.SEND) {
            getContentPane().add(new JLabel("Sending: " + dcc.getShortFileName()), "wrap");
            getContentPane().add(new JLabel("To: " + targetNick), "wrap");
        } else {
            getContentPane().add(new JLabel("Recieving: " + dcc.getShortFileName()), "wrap");
            getContentPane().add(new JLabel("From: " + targetNick), "wrap");
        }
        getContentPane().add(status, "wrap");
        getContentPane().add(speed, "wrap");
        getContentPane().add(remaining, "wrap");
        getContentPane().add(taken, "wrap");
        getContentPane().add(progress, "growx, wrap");

        button.addActionListener(this);
        openButton.addActionListener(this);
        openButton.setVisible(false);

        getContentPane().add(openButton, "split 2, align right");
        getContentPane().add(button, "align right");

        plugin.addWindow(this);
    }

    /** {@inheritDoc} */
    @Override
    public void onSocketClosed(final Parser tParser) {
        // Remove our reference to the parser (and its reference to us)
        parser.getCallbackManager().delAllCallback(this);
        parser = null;
        // Can't resend without the parser.
        if ("Resend".equals(button.getText())) {
            button.setText("Close Window");
        }
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
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("Cancel")) {
            if (dcc.getType() == DCCTransfer.TransferType.SEND) {
                button.setText("Resend");
            } else {
                button.setText("Close Window");
            }
            status.setText("Status: Cancelled");
            dcc.close();
        } else if (e.getActionCommand().equals("Resend")) {
            button.setText("Cancel");
            status.setText("Status: Resending...");
            synchronized (this) {
                transferCount = 0;
            }
            dcc.reset();
            if (parser != null && server.getState() == ServerState.CONNECTED) {
                final String myNickname = parser.getLocalClient().getNickname();
                // Check again incase we have changed nickname to the same nickname that
                // this send is for.
                if (parser.getStringConverter().equalsIgnoreCase(otherNickname, myNickname)) {
                    final Thread errorThread = new Thread(new Runnable() {

                        /** {@inheritDoc} */
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null,
                                    "You can't DCC yourself.", "DCC Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    });
                    errorThread.start();
                    return;
                } else {
                    if (IdentityManager.getGlobalConfig().getOptionBool(
                            plugin.getDomain(), "send.reverse")) {
                        parser.sendCTCP(otherNickname, "DCC", "SEND \"" +
                                new File(dcc.getFileName()).getName() + "\" "
                                + DCC.ipToLong(myPlugin.getListenIP(parser))
                                + " 0 " + dcc.getFileSize() + " " + dcc.makeToken()
                                + ((dcc.isTurbo()) ? " T" : ""));
                        return;
                    } else if (plugin.listen(dcc)) {
                        parser.sendCTCP(otherNickname, "DCC", "SEND \""
                                + new File(dcc.getFileName()).getName() + "\" "
                                + DCC.ipToLong(myPlugin.getListenIP(parser)) + " "
                                + dcc.getPort() + " " + dcc.getFileSize()
                                + ((dcc.isTurbo()) ? " T" : ""));
                        return;
                    }
                }
            } else {
                status.setText("Status: Resend failed.");
                button.setText("Close Window");
            }
        } else if (e.getActionCommand().equals("Close Window")) {
            close();
        } else if (e.getSource() == openButton) {
            final File file = new File(dcc.getFileName());
            try {
                Desktop.getDesktop().open(file);
            } catch (IllegalArgumentException ex) {
                    Logger.userError(ErrorLevel.LOW, "Unable to open file: " +
                            file, ex);
                    openButton.setEnabled(false);
            } catch (IOException ex) {
                try {
                    Desktop.getDesktop().open(file.getParentFile());
                } catch (IllegalArgumentException ex1) {
                    Logger.userError(ErrorLevel.LOW, "Unable to open folder: " +
                            file.getParentFile(), ex1);
                    openButton.setEnabled(false);
                } catch (IOException ex1) {
                    Logger.userError(ErrorLevel.LOW, "No associated handler " +
                            "to open file or directory.", ex1);
                    openButton.setEnabled(false);
                }
            }
        }
    }

    /**
     * Called when data is sent/recieved
     *
     * @param dcc The DCCSend that this message is from
     * @param bytes The number of new bytes that were transfered
     */
    @Override
    public void dataTransfered(final DCCTransfer dcc, final int bytes) {
        final double percent;
        synchronized (this) {
            transferCount += bytes;
            percent = (100.00 / dcc.getFileSize()) * (transferCount + dcc.getFileStart());
        }

        boolean percentageInTitle = IdentityManager.getGlobalConfig().getOptionBool(
                            plugin.getDomain(), "general.percentageInTitle");

        if (dcc.getType() == DCCTransfer.TransferType.SEND) {
            status.setText("Status: Sending");
        } else {
            status.setText("Status: Recieving");
        }

        if (percentageInTitle) {
            final StringBuilder title = new StringBuilder();
            if (dcc.isListenSocket()) { title.append("*"); }
            title.append(dcc.getType() == DCCTransfer.TransferType.SEND ? "Sending: " : "Recieving: ");
            title.append(otherNickname);
            title.append(" ("+ String.format("%.0f", Math.floor(percent)) +"%)");
            this.setName(title.toString());
            this.getFrame().setTitle(title.toString());
        }

        updateSpeedAndTime();

        progress.setValue((int) Math.floor(percent));

        ActionManager.processEvent(DCCActions.DCC_SEND_DATATRANSFERED, null, this, bytes);
    }

    /**
     * Update the transfer speed, time remaining and time taken labels.
     */
    public void updateSpeedAndTime() {
        final long time = (System.currentTimeMillis() - timeStarted) / 1000;
        final double bytesPerSecond;
        synchronized (this) {
            bytesPerSecond = (time > 0) ? (transferCount / time) : transferCount;
        }

        if (bytesPerSecond > 1048576) {
            speed.setText(String.format("Speed: %.2f MB/s", (bytesPerSecond / 1048576)));
        } else if (bytesPerSecond > 1024) {
            speed.setText(String.format("Speed: %.2f KB/s", (bytesPerSecond / 1024)));
        } else {
            speed.setText(String.format("Speed: %.2f B/s", bytesPerSecond));
        }

        final long remaningBytes;
        synchronized (this) {
            remaningBytes = dcc.getFileSize() - dcc.getFileStart() - transferCount;
        }
        final double remainingSeconds = bytesPerSecond > 0
                ? (remaningBytes / bytesPerSecond) : 1;

        remaining.setText(String.format("Time Remaining: %s", duration(
                (int) Math.floor(remainingSeconds))));
        taken.setText(String.format("Time Taken: %s", timeStarted == 0
                ? "N/A" : duration(time)));
    }

    /**
     * Get the duration in seconds as a string.
     *
     * @param secondsInput to get duration for
     * @return Duration as a string
     */
    private String duration(final long secondsInput) {
        final StringBuilder result = new StringBuilder();
        final long hours = (secondsInput / 3600);
        final long minutes = (secondsInput / 60 % 60);
        final long seconds = (secondsInput % 60);

        if (hours > 0) {
            result.append(hours + ":");
        }
        result.append(String.format("%0,2d:%0,2d", minutes, seconds));

        return result.toString();
    }

    /**
     * Called when the socket is closed
     *
     * @param dcc The DCCSend that this message is from
     */
    @Override
    public void socketClosed(final DCCTransfer dcc) {
        ActionManager.processEvent(DCCActions.DCC_SEND_SOCKETCLOSED, null, this);
        if (!isWindowClosing()) {
            synchronized (this) {
                if (transferCount == dcc.getFileSize() - dcc.getFileStart()) {
                    status.setText("Status: Transfer Compelete.");

                    if (showOpen && dcc.getType() == DCCTransfer.TransferType.RECEIVE) {
                        openButton.setVisible(true);
                    }
                    progress.setValue(100);
                    setIcon(dcc.getType() == DCCTransfer.TransferType.SEND
                            ? "dcc-send-done" : "dcc-receive-done");
                    button.setText("Close Window");
                } else {
                    status.setText("Status: Transfer Failed.");
                    setIcon(dcc.getType() == DCCTransfer.TransferType.SEND
                            ? "dcc-send-failed" : "dcc-receive-failed");
                    if (dcc.getType() == DCCTransfer.TransferType.SEND) {
                        button.setText("Resend");
                    } else {
                        button.setText("Close Window");
                    }
                }
            }
            updateSpeedAndTime();
        }
    }

    /**
     * Called when the socket is opened
     *
     * @param dcc The DCCSend that this message is from
     */
    @Override
    public void socketOpened(final DCCTransfer dcc) {
        ActionManager.processEvent(DCCActions.DCC_SEND_SOCKETOPENED, null, this);
        status.setText("Status: Socket Opened");
        timeStarted = System.currentTimeMillis();
        setIcon(dcc.getType() == DCCTransfer.TransferType.SEND
                ? "dcc-send-active" : "dcc-receive-active");
    }

    /**
     * Closes this container (and it's associated frame).
     */
    @Override
    public void windowClosing() {
        super.windowClosing();
        dcc.removeFromTransfers();
        dcc.close();
    }

}

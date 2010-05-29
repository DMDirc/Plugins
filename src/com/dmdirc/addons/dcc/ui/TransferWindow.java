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

package com.dmdirc.addons.dcc.ui;

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.dcc.DCCTransferHandler;
import com.dmdirc.addons.dcc.TransferContainer;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

/**
 * A window for displaying the progress of DCC transfers.
 *
 * @author chris
 * @since 0.6.4
 */
public class TransferWindow extends EmptyWindow implements ActionListener,
        SocketCloseListener, DCCTransferHandler {

    /** A version number for this class. */
    private static final long serialVersionUID = 1L;

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

    /** The transfer that this window is showing. */
    private final DCCTransfer dcc;

    /**
     * Creates a new transfer window for the specified UI controller and owner.
     *
     * @param controller The UIController that owns this window
     * @param owner The frame container that owns this window
     */
    public TransferWindow(final SwingController controller, final FrameContainer<?> owner) {
        super(controller, owner);
        
        final TransferContainer container = (TransferContainer) owner;
        dcc = container.getDCC();

        dcc.addHandler(this);
        container.addSocketCloseCallback(this);

        setLayout(new MigLayout("hidemode 0"));

        if (dcc.getType() == DCCTransfer.TransferType.SEND) {
            add(new JLabel("Sending: " + dcc.getShortFileName()), "wrap");
            add(new JLabel("To: " + container.getOtherNickname()), "wrap");
        } else {
            add(new JLabel("Recieving: " + dcc.getShortFileName()), "wrap");
            add(new JLabel("From: " + container.getOtherNickname()), "wrap");
        }

        add(status, "wrap");
        add(speed, "wrap");
        add(remaining, "wrap");
        add(taken, "wrap");
        add(progress, "growx, wrap");

        button.addActionListener(this);
        openButton.addActionListener(this);
        openButton.setVisible(false);

        add(openButton, "split 2, align right");
        add(button, "align right");
    }

    /** {@inheritDoc} */
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

            if (!((TransferContainer) frameParent).resend()) {
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
                Logger.userError(ErrorLevel.LOW, "Unable to open file: " + file, ex);
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

    /** {@inheritDoc} */
    @Override
    public void onSocketClosed(final Parser parser, final Date date) {
        // Can't resend without the parser.
        if ("Resend".equals(button.getText())) {
            button.setText("Close Window");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void socketClosed(final DCCTransfer dcc) {
        UIUtilities.invokeLater(new Runnable() {

            private final TransferContainer container = (TransferContainer) frameParent;

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (container.isComplete()) {
                    status.setText("Status: Transfer Compelete.");

                    if (container.shouldShowOpenButton()) {
                        openButton.setVisible(true);
                    }
                    
                    progress.setValue(100);
                    button.setText("Close Window");
                } else {
                    status.setText("Status: Transfer Failed.");
                    if (dcc.getType() == DCCTransfer.TransferType.SEND) {
                        button.setText("Resend");
                    } else {
                        button.setText("Close Window");
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void socketOpened(final DCCTransfer dcc) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                status.setText("Status: Socket Opened");
            }

        });
    }

    /** {@inheritDoc} */
    @Override
    public void dataTransfered(final DCCTransfer dcc, final int bytes) {
        UIUtilities.invokeLater(new Runnable() {

            private final TransferContainer container = (TransferContainer) frameParent;

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (dcc.getType() == DCCTransfer.TransferType.SEND) {
                    status.setText("Status: Sending");
                } else {
                    status.setText("Status: Recieving");
                }

                progress.setValue((int) container.getPercent());

                final double bytesPerSecond = container.getBytesPerSecond();

                if (bytesPerSecond > 1048576) {
                    speed.setText(String.format("Speed: %.2f MiB/s", bytesPerSecond / 1048576));
                } else if (bytesPerSecond > 1024) {
                    speed.setText(String.format("Speed: %.2f KiB/s", bytesPerSecond / 1024));
                } else {
                    speed.setText(String.format("Speed: %.2f B/s", bytesPerSecond));
                }

                remaining.setText(String.format("Time Remaining: %s",
                        duration((int) container.getRemainingTime())));
                taken.setText(String.format("Time Taken: %s", container.getStartTime() == 0
                        ? "N/A" : duration(container.getElapsedTime())));
            }
        });
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

}

/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.dcc.ui;

import com.dmdirc.addons.dcc.DCCTransferHandler;
import com.dmdirc.addons.dcc.TransferContainer;
import com.dmdirc.addons.dcc.io.DCCTransfer;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.SwingFrameComponent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.EventBus;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.parser.events.SocketCloseEvent;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.util.DateUtils;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.engio.mbassy.listener.Handler;

import static com.dmdirc.util.LogUtils.USER_ERROR;

/**
 * A panel for displaying the progress of DCC transfers.
 *
 * @since 0.6.6
 */
public class TransferPanel extends JPanel implements ActionListener,
        DCCTransferHandler, SwingFrameComponent {

    private static final Logger LOG = LoggerFactory.getLogger(TransferPanel.class);
    /** A version number for this class. */
    private static final long serialVersionUID = 1L;
    /** Parent container. */
    private final TransferContainer transferContainer;
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
    /** The event bus to post errors. */
    private final EventBus errorBus;

    /**
     * Creates a new transfer window for the specified UI controller and owner.
     *
     * @param owner    The frame container that owns this frame
     * @param errorBus The event bus to post errors to
     */
    public TransferPanel(final WindowModel owner, final EventBus errorBus) {
        this.transferContainer = (TransferContainer) owner;
        this.errorBus = errorBus;
        dcc = transferContainer.getDCC();

        dcc.addHandler(this);
        transferContainer.getConnection()
                .flatMap(Connection::getParser)
                .map(Parser::getCallbackManager)
                .ifPresent(cm -> cm.subscribe(this));

        setLayout(new MigLayout("hidemode 0"));

        if (dcc.getType() == DCCTransfer.TransferType.SEND) {
            add(new JLabel("Sending: " + dcc.getShortFileName()), "wrap");
            add(new JLabel("To: " + transferContainer
                    .getOtherNickname()), "wrap");
        } else {
            add(new JLabel("Receiving: " + dcc.getShortFileName()), "wrap");
            add(new JLabel("From: " + transferContainer
                    .getOtherNickname()), "wrap");
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

            if (!(transferContainer.resend())) {
                status.setText("Status: Resend failed.");
                button.setText("Close Window");
            }
        } else if (e.getActionCommand().equals("Close Window")) {
            transferContainer.close();
        } else if (e.getSource() == openButton) {
            final File file = new File(dcc.getFileName());
            try {
                Desktop.getDesktop().open(file);
            } catch (IllegalArgumentException ex) {
                LOG.info(USER_ERROR, "Unable to open file {}", file.getAbsolutePath(), ex);
                openButton.setEnabled(false);
            } catch (IOException ex) {
                try {
                    Desktop.getDesktop().open(file.getParentFile());
                } catch (IllegalArgumentException ex1) {
                    LOG.info(USER_ERROR, "Unable to open folder: {}",
                            file.getParentFile().getAbsolutePath(), ex1);
                    openButton.setEnabled(false);
                } catch (IOException ex1) {
                    LOG.info(USER_ERROR, "No associated handler to open file or directory.", ex1);
                    openButton.setEnabled(false);
                }
            }
        }
    }

    @Handler
    public void onSocketClosed(final SocketCloseEvent event) {
        if ("Resend".equals(button.getText())) {
            button.setText("Close Window");
        }
    }

    @Override
    public void socketClosed(final DCCTransfer dcc) {
        UIUtilities.invokeLater(() -> {
            if (transferContainer.isComplete()) {
                status.setText("Status: Transfer Complete.");

                if (transferContainer.shouldShowOpenButton()) {
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
        });
    }

    @Override
    public void socketOpened(final DCCTransfer dcc) {
        UIUtilities.invokeLater(() -> status.setText("Status: Socket Opened"));
    }

    @Override
    public void dataTransferred(final DCCTransfer dcc, final int bytes) {
        UIUtilities.invokeLater(() -> {
            if (dcc.getType() == DCCTransfer.TransferType.SEND) {
                status.setText("Status: Sending");
            } else {
                status.setText("Status: Receiving");
            }

            progress.setValue((int) transferContainer.getPercent());

            final double bytesPerSecond = transferContainer
                    .getBytesPerSecond();

            if (bytesPerSecond > 1048576) {
                speed.setText(String.format("Speed: %.2f MiB/s",
                        bytesPerSecond / 1048576));
            } else if (bytesPerSecond > 1024) {
                speed.setText(String.format("Speed: %.2f KiB/s",
                        bytesPerSecond / 1024));
            } else {
                speed.setText(String.format("Speed: %.2f B/s",
                        bytesPerSecond));
            }

            remaining.setText(String.format("Time Remaining: %s",
                    DateUtils.formatDurationAsTime((int) transferContainer.getRemainingTime())));
            taken.setText(String.format("Time Taken: %s", transferContainer
                    .getStartTime() == 0 ? "N/A" : DateUtils.formatDurationAsTime(
                    (int) transferContainer.getElapsedTime())));
        });
    }

}

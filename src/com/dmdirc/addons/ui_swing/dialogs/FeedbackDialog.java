/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.SendWorker;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.ui.core.util.Info;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import lombok.AutoGenMethodStub;

import net.miginfocom.swing.MigLayout;

/** Feedback form. */
@AutoGenMethodStub
public final class FeedbackDialog extends StandardDialog implements
        ActionListener, DocumentListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Information label. */
    private TextLabel info;
    /** Name field. */
    private JTextField name;
    /** Email field. */
    private JTextField email;
    /** Feedback area. */
    private JTextArea feedback;
    /** Server info checkbox. */
    private JCheckBox serverCheckbox;
    /** DMDirc info checkbox. */
    private JCheckBox dmdircCheckbox;
    /** Sent. */
    private boolean sentReport = false;

    /**
     * Instantiates the feedback dialog.
     *
     * @param controller Swing controller
     */
    public FeedbackDialog(final SwingController controller) {
        super(controller, ModalityType.MODELESS);

        initComponents();
        layoutComponents();
        addListeners();

        setTitle("Feedback");
        setResizable(false);
    }

    /** Initialises the components. */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());

        getOkButton().setText("Send");
        getOkButton().setActionCommand("Send");
        getOkButton().setEnabled(false);
        getCancelButton().setActionCommand("Close");

        info = new TextLabel("Thank you for using DMDirc. If you have any "
                + "feedback about the client, such as bug reports or feature "
                + "requests, please send it to us using the form below.  "
                + "The name and e-mail address fields are optional if you "
                + "don't want us to contact you about your feedback.\n\n"
                + "Please note that this is for feedback such as bug reports "
                + "and suggestions, not for technical support. For "
                + "technical support, please join #DMDirc using the button "
                + "in the help menu.");
        name = new JTextField();
        email = new JTextField();
        feedback = new JTextArea();
        serverCheckbox =
                new JCheckBox("Include information about connected servers.");
        dmdircCheckbox = new JCheckBox("Include information about DMDirc.");

        UIUtilities.addUndoManager(name);
        UIUtilities.addUndoManager(email);
        UIUtilities.addUndoManager(feedback);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        serverCheckbox.setMargin(new Insets(0, 0, 0, 0));
        serverCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dmdircCheckbox.setMargin(new Insets(0, 0, 0, 0));
        dmdircCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        setLayout(new MigLayout(
                "fill, wmin 600, wmax 600, hmin 400, hmax 400"));

        add(info, "span, growx, wrap, gapbottom unrel");

        add(new JLabel("Name: "), "aligny top, shrink");
        add(name, "growx, pushx, wrap");

        add(new JLabel("Email: "), "aligny top, shrink");
        add(email, "growx, pushx, wrap");

        add(new JLabel("Feedback: "), "aligny top, shrink");
        add(new JScrollPane(feedback), "grow, push, wrap");
        add(serverCheckbox, "skip 1, growx, wrap");
        add(dmdircCheckbox, "skip 1, growx, wrap");

        add(getCancelButton(), "skip, split 2, right, sg button");
        add(getOkButton(), "right, sg button");
    }

    /**
     * Lays out the components.
     *
     * @param error Did the submission error?
     */
    public void layoutComponents2(final StringBuilder error) {
        getContentPane().setVisible(false);
        getContentPane().removeAll();
        getOkButton().setEnabled(true);
        getOkButton().setText("Close");
        getOkButton().setActionCommand("Close");

        setLayout(new MigLayout(
                "fill, wmin 600, wmax 600, hmin 400, hmax 400"));

        info.setText(error.toString());

        add(info, "span 3, grow, push, wrap");

        add(getOkButton(), "skip, right, tag ok, sg button");
        getContentPane().setVisible(true);
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        feedback.getDocument().addDocumentListener(this);
    }

    /** Checks and sends the feedback. */
    private void send() {
        sentReport = true;
        getOkButton().setEnabled(false);
        getCancelButton().setEnabled(false);
        final StringBuilder serverInfo = new StringBuilder();
        final StringBuilder dmdircInfo = new StringBuilder();
        if (serverCheckbox.isSelected()) {
            for (Server server : ServerManager.getServerManager()
                    .getServers()) {
                if (server.getState().isDisconnected()) {
                    continue;
                }
                serverInfo.append("Server name: ").append(server.getName())
                        .append("\n");
                serverInfo.append("Actual name: ").append(server.getParser()
                        .getServerName()).append("\n");
                serverInfo.append("Network: ").append(server.getNetwork())
                        .append("\n");
                serverInfo.append("IRCd: ").append(server.getParser()
                        .getServerSoftware()).append(" - ");
                serverInfo.append(server.getParser().getServerSoftwareType())
                        .append("\n");
                serverInfo.append("Modes: ").append(server.getParser()
                        .getBooleanChannelModes()).append(" ");
                serverInfo.append(server.getParser().getListChannelModes())
                        .append(" ");
                serverInfo.append(server.getParser().getParameterChannelModes())
                        .append(" ");
                serverInfo.append(server.getParser().
                        getDoubleParameterChannelModes());
            }
        }
        if (dmdircCheckbox.isSelected()) {
            dmdircInfo.append("DMDirc version: ").append(
                    Info.getDMDircVersion()).append("\n");
            dmdircInfo.append("Profile directory: ").append(
                    Main.getConfigDir()).append("\n");
            dmdircInfo.append("Java version: ").append(
                    Info.getJavaVersion()).append("\n");
            dmdircInfo.append("OS Version: ").append(
                    Info.getOSVersion()).append("\n");
            dmdircInfo.append("Look & Feel: ").append(
                    SwingController.getLookAndFeel());
        }
        new SendWorker(this, name.getText().trim(), email.getText().trim(),
                feedback.getText().trim(), serverInfo.toString().trim(),
                dmdircInfo.toString().trim()).executeInExecutor();
    }

    /** Validates the input. */
    private void validateInput() {
        if (feedback.getDocument().getLength() > 0) {
            getOkButton().setEnabled(true);
        } else {
            getOkButton().setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals("Send")) {
            if (!sentReport) {
                send();
            }
        } else if (e.getActionCommand().equals("Close")) {
            dispose();
        }
    }

    /**
     * @{inheritDoc}
     *
     * @param e Document event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {
        validateInput();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {
        validateInput();
    }
}

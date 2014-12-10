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

package com.dmdirc.addons.ui_swing.dialogs.feedback;

import com.dmdirc.addons.ui_swing.components.ConsumerDocumentListener;
import com.dmdirc.interfaces.ui.FeedbackDialogModel;
import com.dmdirc.interfaces.ui.FeedbackDialogModelListener;

import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Links the Feedback Dialog with its model.
 */
public class FeedbackModelLinker implements FeedbackDialogModelListener {

    private final FeedbackDialog dialog;
    private final FeedbackDialogModel model;
    private JTextField emailField;
    private JTextField nameField;
    private JTextArea feedbackField;
    private JButton okButton;
    private JCheckBox serverInfo;
    private JCheckBox dmdircInfo;

    public FeedbackModelLinker(final FeedbackDialog dialog, final FeedbackDialogModel model) {
        this.dialog = dialog;
        this.model = model;
    }

    public void init() {
        model.addListener(this);
    }

    public void bindName(final JTextField nameField) {
        this.nameField = nameField;
        nameField.getDocument().addDocumentListener(new ConsumerDocumentListener(
                s -> model.setName(Optional.ofNullable(s))));
        nameField.setText("");
    }

    public void bindEmail(final JTextField emailField) {
        this.emailField = emailField;
        emailField.getDocument().addDocumentListener(new ConsumerDocumentListener(
                s -> model.setEmail(Optional.ofNullable(emailField.getText()))));
        emailField.setText("");
    }

    public void bindFeedback(final JTextArea feedbackField) {
        this.feedbackField = feedbackField;
        feedbackField.getDocument().addDocumentListener(new ConsumerDocumentListener(
                s -> model.setFeedback(Optional.ofNullable(feedbackField.getText()))));
        feedbackField.setText("");
    }

    public void bindDMDircInfo(final JCheckBox dmdircInfo) {
        this.dmdircInfo = dmdircInfo;
        dmdircInfo.addActionListener(e -> model.setIncludeDMDircInfo(dmdircInfo.isSelected()));
        dmdircInfo.setSelected(false);
    }

    public void bindServerInfo(final JCheckBox serverInfo) {
        this.serverInfo = serverInfo;
        serverInfo.addActionListener(e -> model.setIncludeServerInfo(serverInfo.isSelected()));
        serverInfo.setSelected(false);
    }

    public void bindOKButton(final JButton okButton) {
        this.okButton = okButton;
        okButton.addActionListener(e -> {
            model.save();
            dialog.dispose();
        });
        okButton.setEnabled(model.isSaveAllowed());
    }

    public void bindCancelButton(final JButton cancelButton) {
        cancelButton.addActionListener(e -> dialog.dispose());
    }

    @Override
    public void nameChanged(final Optional<String> name) {
        if (!name.equals(Optional.ofNullable(nameField.getText()))) {
            nameField.setText(name.orElse(null));
        }
        okButton.setEnabled(model.isSaveAllowed());
    }

    @Override
    public void emailChanged(final Optional<String> email) {
        if (!email.equals(Optional.ofNullable(emailField.getText()))) {
            emailField.setText(email.orElse(null));
        }
        okButton.setEnabled(model.isSaveAllowed());
    }

    @Override
    public void feedbackChanged(final Optional<String> feedback) {
        if (!feedback.equals(Optional.ofNullable(feedbackField.getText()))) {
            feedbackField.setText(feedback.orElse(null));
        }
        okButton.setEnabled(model.isSaveAllowed());
    }

    @Override
    public void includeServerInfoChanged(final boolean includeServerInfo) {
        if (includeServerInfo != serverInfo.isSelected()) {
            serverInfo.setSelected(includeServerInfo);
        }
        okButton.setEnabled(model.isSaveAllowed());
    }

    @Override
    public void includeDMDircInfoChanged(final boolean includeDMDircInfo) {
        if (includeDMDircInfo != dmdircInfo.isSelected()) {
            dmdircInfo.setSelected(includeDMDircInfo);
        }
        okButton.setEnabled(model.isSaveAllowed());
    }
}

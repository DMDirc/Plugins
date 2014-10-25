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

import com.dmdirc.interfaces.ui.FeedbackDialogModel;
import com.dmdirc.ui.core.feedback.FeedbackDialogModelAdapter;

import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Links the Feedback Dialog with its model.
 */
public class FeedbackModelLinker {

    private final FeedbackDialog dialog;
    private final FeedbackDialogModel model;

    public FeedbackModelLinker(final FeedbackDialog dialog, final FeedbackDialogModel model) {
        this.dialog = dialog;
        this.model = model;
    }

    public void bindName(final JTextField nameField) {
        nameField.getDocument().addDocumentListener(new DocumentListener() {

            private void update() {
                model.setName(Optional.ofNullable(nameField.getText()));
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                update();
            }
        });
        model.addListener(new FeedbackDialogModelAdapter() {

            @Override
            public void nameChanged(final Optional<String> name) {
                if (!name.equals(Optional.ofNullable(nameField.getText()))) {
                    nameField.setText(name.orElse(null));
                }
            }
        });
        nameField.setText("");
    }

    public void bindEmail(final JTextField emailField) {
        emailField.getDocument().addDocumentListener(new DocumentListener() {

            private void update() {
                model.setEmail(Optional.ofNullable(emailField.getText()));
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                update();
            }
        });
        model.addListener(new FeedbackDialogModelAdapter() {

            @Override
            public void emailChanged(final Optional<String> email) {
                if (!email.equals(Optional.ofNullable(emailField.getText()))) {
                    emailField.setText(email.orElse(null));
                }
            }
        });
        emailField.setText("");
    }

    public void bindFeedback(final JTextArea feedbackField) {
        feedbackField.getDocument().addDocumentListener(new DocumentListener() {

            private void update() {
                model.setFeedback(Optional.ofNullable(feedbackField.getText()));
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                update();
            }
        });
        model.addListener(new FeedbackDialogModelAdapter() {

            @Override
            public void feedbackChanged(final Optional<String> feedback) {
                if (!feedback.equals(Optional.ofNullable(feedbackField.getText()))) {
                    feedbackField.setText(feedback.orElse(null));
                }
            }
        });
        feedbackField.setText("");
    }

    public void bindDMDircInfo(final JCheckBox dmdircInfo) {
        dmdircInfo.addActionListener(e -> model.setIncludeDMDircInfo(dmdircInfo.isSelected()));
        model.addListener(new FeedbackDialogModelAdapter() {

            @Override
            public void includeDMDircInfoChanged(final boolean includeDMDircInfo) {
                if (includeDMDircInfo != dmdircInfo.isSelected()) {
                    dmdircInfo.setSelected(includeDMDircInfo);
                }
            }
        });
        dmdircInfo.setSelected(false);
    }

    public void bindServerInfo(final JCheckBox serverInfo) {
        serverInfo.addActionListener(e -> model.setIncludeServerInfo(serverInfo.isSelected()));
        model.addListener(new FeedbackDialogModelAdapter() {

            @Override
            public void includeServerInfoChanged(final boolean includeServerInfo) {
                if (includeServerInfo != serverInfo.isSelected()) {
                    serverInfo.setSelected(includeServerInfo);
                }
            }
        });
        serverInfo.setSelected(false);
    }

    public void bindOKButton(final JButton okButton) {
        okButton.addActionListener(e -> {
            model.save();
            dialog.dispose();
        });
        model.addListener(new FeedbackDialogModelAdapter() {

            private void update() {
                okButton.setEnabled(model.isSaveAllowed());
            }

            @Override
            public void includeDMDircInfoChanged(final boolean includeDMDircInfo) {
                update();
            }

            @Override
            public void includeServerInfoChanged(final boolean includeServerInfo) {
                update();
            }

            @Override
            public void feedbackChanged(final Optional<String> feedback) {
                update();
            }

            @Override
            public void emailChanged(final Optional<String> email) {
                update();
            }

            @Override
            public void nameChanged(final Optional<String> name) {
                update();
            }
        });
        okButton.setEnabled(model.isSaveAllowed());
    }

    public void bindCancelButton(final JButton cancelButton) {
        cancelButton.addActionListener(e -> dialog.dispose());
    }

}

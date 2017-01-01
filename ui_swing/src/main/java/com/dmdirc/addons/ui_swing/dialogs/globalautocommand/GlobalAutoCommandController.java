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

package com.dmdirc.addons.ui_swing.dialogs.globalautocommand;

import com.dmdirc.addons.ui_swing.components.ConsumerDocumentListener;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.interfaces.ui.GlobalAutoCommandsDialogModel;

import javax.swing.JButton;
import javax.swing.JTextArea;

/**
 * Controls a {@link GlobalAutoCommandDialog}.
 */
public class GlobalAutoCommandController {

    private StandardDialog dialog;
    private GlobalAutoCommandsDialogModel model;
    private JTextArea response;
    private JButton okButton;
    private JButton cancelButton;

    public void init(final StandardDialog dialog, final GlobalAutoCommandsDialogModel model,
            final JTextArea response,final JButton okButton, final JButton cancelButton) {
        this.dialog = dialog;
        this.model = model;
        this.response = response;
        this.okButton = okButton;
        this.cancelButton = cancelButton;

        model.load();

        initResponse();
        initOK();
        initCancel();
    }

    private void initResponse() {
        response.setText(model.getResponse());
        response.getDocument().addDocumentListener(new ConsumerDocumentListener(text -> {
            model.setResponse(text);
            okButton.setEnabled(model.isSaveAllowed());
        } ));
    }

    private void initOK() {
        okButton.addActionListener(l -> {
            model.save();
            dialog.dispose();
        });
    }

    private void initCancel() {
        cancelButton.addActionListener(l -> dialog.dispose());
    }
}

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

import com.dmdirc.ClientModule;
import com.dmdirc.addons.ui_swing.components.NoBorderJCheckBox;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidationFactory;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.interfaces.ui.FeedbackDialogModel;
import com.dmdirc.ui.IconManager;

import java.awt.Window;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog to send feedback to the developers.
 */
public class FeedbackDialog extends StandardDialog {

    private static final long serialVersionUID = 1;

    @Inject
    public FeedbackDialog(@MainWindow final Window mainFrame,
            final FeedbackDialogModel model,
            @ClientModule.GlobalConfig final IconManager iconManager) {
        super(mainFrame, ModalityType.DOCUMENT_MODAL);
        final FeedbackModelLinker linker = new FeedbackModelLinker(this, model);
        linker.init();

        setTitle("Feedback");

        final TextLabel info = new TextLabel("Thank you for using DMDirc. If you have any "
                + "feedback about the client, such as bug reports or feature "
                + "requests, please send it to us using the form below.  "
                + "The name and e-mail address fields are optional if you "
                + "don't want us to contact you about your feedback.\n\n"
                + "Please note that this is for feedback such as bug reports "
                + "and suggestions, not for technical support. For "
                + "technical support, please join #DMDirc using the button "
                + "in the help menu.");
        final JTextField name = new JTextField();
        final JTextField email = new JTextField();
        final JTextArea feedback = new JTextArea();
        final JCheckBox serverInfo = new NoBorderJCheckBox("Include information about connected "
                + "servers.");
        final JCheckBox dmdircInfo = new NoBorderJCheckBox("Include information about DMDirc.");

        linker.bindName(name);
        linker.bindEmail(email);
        linker.bindFeedback(feedback);
        linker.bindServerInfo(serverInfo);
        linker.bindDMDircInfo(dmdircInfo);
        linker.bindOKButton(getOkButton());
        linker.bindCancelButton(getCancelButton());

        setLayout(new MigLayout("fill, wmin 600, wmax 600, hmin 400, hmax 400"));

        add(info, "span, growx, wrap 2*unrel");

        add(new JLabel("Name: "), "align label");
        add(ValidationFactory.getValidatorPanel(name, model.getNameValidator(), iconManager),
                "growx, pushx, wrap");

        add(new JLabel("Email: "), "aligny label");
        add(ValidationFactory.getValidatorPanel(email, model.getEmailValidator(), iconManager),
                "growx, pushx, wrap");
        add(new JLabel("Feedback: "), "aligny label");
        add(ValidationFactory.getValidatorPanel(new JScrollPane(feedback), feedback,
                model.getFeedbackValidator(), iconManager), "grow, push, wrap");
        add(serverInfo, "skip 1, growx, wrap");
        add(dmdircInfo, "skip 1, growx, wrap 2*unrel");
        add(getLeftButton(), "span, split 2, right, sg button");
        add(getRightButton(), "right, sg button");
    }

}

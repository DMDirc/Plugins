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

package com.dmdirc.addons.ui_swing.dialogs.newserver;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.components.NoBorderJCheckBox;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidationFactory;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.profile.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.config.profiles.Profile;
import com.dmdirc.interfaces.ui.NewServerDialogModel;
import com.dmdirc.ui.IconManager;

import java.awt.Window;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog to connect to a new server.
 */
public class NewServerDialog extends StandardDialog {

    private static final long serialVersionUID = 1;

    @Inject
    public NewServerDialog(@MainWindow final Window mainFrame, final NewServerDialogModel model,
            @GlobalConfig final IconManager iconManager,
            final DialogProvider<ProfileManagerDialog> profileManagerDialog) {
        super(mainFrame, ModalityType.DOCUMENT_MODAL);
        final NewServerLinker linker = new NewServerLinker(model, this);

        final TextLabel info = new TextLabel("Enter the details of the server you wish to "
                + "connect to.");
        final JTextField hostname = new JTextField();
        final JTextField port = new JTextField();
        final JTextField password = new JTextField();
        final JComboBox<Profile> profiles = new JComboBox<>();
        final JButton edit = new JButton("Edit");
        final JCheckBox ssl = new NoBorderJCheckBox("Use a secure (SSL) connection?");
        final JCheckBox saveAsDefault
                = new NoBorderJCheckBox("Save these settings as the defaults?");

        model.loadModel();
        linker.bindHostname(hostname);
        linker.bindPort(port);
        linker.bindPassword(password);
        linker.bindProfiles(profiles);
        linker.bindEditProfiles(edit, profileManagerDialog);
        linker.bindSSL(ssl);
        linker.bindSaveAsDefault(saveAsDefault);
        linker.bindOKButton(getOkButton());
        linker.bindCancelButton(getCancelButton());

        setResizable(false);

        setTitle("New Server");
        setLayout(new MigLayout("fill"));

        add(info, "span, wrap 2*unrel");
        add(new JLabel("Hostname: "), "align label");
        add(ValidationFactory.getValidatorPanel(hostname, model.getHostnameValidator(),
                iconManager), "growx, wrap");
        add(new JLabel("Port: "), "align label");
        add(ValidationFactory.getIntValidatorPanel(port, model.getPortValidator(), iconManager),
                "growx, wrap");
        add(new JLabel("Password: "), "align label");
        add(ValidationFactory.getValidatorPanel(password, model.getPasswordValidator(),
                iconManager), "growx, wrap");
        add(new JLabel("Profiles: "), "align label");
        add(ValidationFactory.getValidatorPanel(profiles, model.getProfileListValidator(),
                iconManager), "span, split2, growx");
        add(edit, "wrap, sg button");
        add(ssl, "skip, grow, span, wrap");
        add(saveAsDefault, "skip, grow, span, wrap 2*unrel");
        add(getLeftButton(), "span, split 2, right, sg button");
        add(getRightButton(), "right, sg button");
    }

}

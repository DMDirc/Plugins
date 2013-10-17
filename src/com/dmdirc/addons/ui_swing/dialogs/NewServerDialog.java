/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableChangeEvent;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableComboBoxModel;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableComboBoxSelectionListener;
import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.util.validators.PortValidator;
import com.dmdirc.util.validators.ServerNameValidator;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog that allows the user to enter details of a new server to connect to.
 */
public final class NewServerDialog extends StandardDialog implements
        ActionListener, VetoableComboBoxSelectionListener, ConfigProviderListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 8;
    /** Identity Manager. */
    private final IdentityController identityController;
    /** Server manager. */
    private final ServerManager serverManager;
    /** checkbox. */
    private JCheckBox newServerWindowCheck;
    /** checkbox. */
    private JCheckBox sslCheck;
    /** text field. */
    private ValidatingJTextField serverField;
    /** text field. */
    private ValidatingJTextField portField;
    /** text field. */
    private JTextField passwordField;
    /** combo box. */
    private JComboBox<ConfigProvider> identityField;
    /** button. */
    private JButton editProfileButton;
    /**  Opening new server? */
    private boolean openingServer = false;

    /**
     * Creates a new instance of the dialog.
     *
     * @param controller Swing controller
     */
    public NewServerDialog(final SwingController controller) {
        super(controller, controller.getMainFrame(), ModalityType.MODELESS);
        identityController = controller.getIdentityManager();
        serverManager = controller.getServerManager();

        initComponents();
        layoutComponents();
        addListeners();
        setResizable(false);

        identityController.registerIdentityListener("profile", this);
        update();
    }

    /** {@inheritDoc} */
    @Override
    public void display() {
        super.display();
        requestFocusInWindow();
        serverField.selectAll();
        serverField.requestFocus();
    }

    /** Updates the values to defaults. */
    private void update() {
        serverField.setText(getController().getGlobalConfig().getOption("general",
                "server"));
        portField.setText(getController().getGlobalConfig().getOption("general",
                "port"));
        passwordField.setText(getController().getGlobalConfig().getOption("general",
                "password"));
        sslCheck.setSelected(false);
        newServerWindowCheck.setEnabled(false);

        serverField.requestFocusInWindow();

        if (serverManager.numServers() == 0 ||
                getController().getMainFrame().getActiveFrame() == null) {
            newServerWindowCheck.setSelected(true);
            newServerWindowCheck.setEnabled(false);
        } else {
            newServerWindowCheck.setEnabled(true);
        }

        populateProfiles();
    }

    /**
     * Adds listeners for various objects in the dialog.
     */
    private void addListeners() {
        getCancelButton().addActionListener(this);
        getOkButton().addActionListener(this);
        editProfileButton.addActionListener(this);
        ((VetoableComboBoxModel) identityField.getModel()).
                addVetoableSelectionListener(this);
    }

    /**
     * Initialises the components in this dialog.
     */
    private void initComponents() {
        serverField = new ValidatingJTextField(getController().getIconManager(),
                new ServerNameValidator());
        portField = new ValidatingJTextField(getController().getIconManager(),
                new PortValidator());
        passwordField = new JPasswordField();
        newServerWindowCheck = new JCheckBox();
        newServerWindowCheck.setSelected(true);
        sslCheck = new JCheckBox();
        identityField = new JComboBox<>(new VetoableComboBoxModel<ConfigProvider>());
        editProfileButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        orderButtons(new JButton(), new JButton());
        setTitle("Connect to a new server");

        populateProfiles();

        editProfileButton.setText("Edit");

        newServerWindowCheck.setText("Open in a new server window?");
        newServerWindowCheck.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0));
        newServerWindowCheck.setMargin(new Insets(0, 0, 0, 0));

        sslCheck.setText("Use a secure (SSL) connection?");
        sslCheck.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sslCheck.setMargin(new Insets(0, 0, 0, 0));
    }

    /** Populates the profiles list. */
    public void populateProfiles() {
        final List<ConfigProvider> profiles = getController().getIdentityManager()
                .getProvidersByType("profile");
        ((DefaultComboBoxModel) identityField.getModel()).removeAllElements();
        for (ConfigProvider profile : profiles) {
            ((MutableComboBoxModel<ConfigProvider>) identityField.getModel()).addElement(profile);
        }
    }

    /**
     * Lays out the components in the dialog.
     */
    private void layoutComponents() {
        getContentPane().setLayout(new MigLayout("fill"));

        getContentPane().add(new JLabel("Enter the details of the server that " +
                "you wish to connect to."), "span 3, wrap 1.5*unrel");
        getContentPane().add(new JLabel("Server: "), "");
        getContentPane().add(serverField, "growx, pushx, wrap");
        getContentPane().add(new JLabel("Port: "), "");
        getContentPane().add(portField, "growx, pushx, wrap");
        getContentPane().add(new JLabel("Password: "), "");
        getContentPane().add(passwordField, "growx, pushx, wrap");
        getContentPane().add(new JLabel("Profile: "), "");
        getContentPane().add(identityField, "split 2, growx, pushx");
        getContentPane().add(editProfileButton, "sg button, wrap");
        getContentPane().add(sslCheck, "skip, wrap");
        getContentPane().add(newServerWindowCheck, "skip, wrap 1.5*unrel");
        getContentPane().add(getLeftButton(), "split, skip, right, sg button");
        getContentPane().add(getRightButton(), "right, sg button");

        pack();
    }

    /**
     * Saves the dialog changes.
     */
    private void save() {
        if (openingServer) {
            dispose();
            return;
        }
        if (!serverField.validateText()) {
            serverField.requestFocusInWindow();
            return;
        }
        if (!portField.validateText()) {
            portField.requestFocusInWindow();
            return;
        }

        final String host = serverField.getText();
        final String pass = passwordField.getText();
        final int port = Integer.parseInt(portField.getText());

        dispose();
        openingServer = true;

        final ConfigProvider profile = (ConfigProvider) identityField.getSelectedItem();

        try {
            final URI address = new URI("irc" + (sslCheck.isSelected() ? "s" :
                ""), pass, host, port, null, null, null);

            // Open in a new window?
            if (newServerWindowCheck.isSelected()
                    || serverManager.numServers() == 0
                    || getController().getMainFrame().getActiveFrame() == null) {

                new LoggingSwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        serverManager.connectToAddress(address, profile);
                        return null;
                    }
                }.executeInExecutor();
            } else {
                final Connection connection = getController().getMainFrame()
                        .getActiveFrame().getContainer().getServer();

                new LoggingSwingWorker<Void, Void>() {

                    /** {@inheritDoc} */
                    @Override
                    protected Void doInBackground() {
                        if (connection == null) {
                            serverManager.connectToAddress(address, profile);
                        } else {
                            connection.connect(address, profile);
                        }
                        return null;
                    }
                }.executeInExecutor();
            }
        } catch (URISyntaxException ex) {
            Logger.userError(ErrorLevel.MEDIUM, "Unable to create URI", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            save();
        } else if (e.getSource() == editProfileButton) {
            getController().showDialog(ProfileManagerDialog.class);
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean enterPressed() {
        executeAction(getOkButton());
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean selectionChanged(final VetoableChangeEvent e) {
        return e.getNewValue() != null;
    }

    /** {@inheritDoc} */
    @Override
    public void configProviderAdded(final ConfigProvider configProvider) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                populateProfiles();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        identityController.unregisterIdentityListener(this);
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void configProviderRemoved(final ConfigProvider configProvider) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                populateProfiles();
            }
        });
    }
}

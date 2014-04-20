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

package com.dmdirc.addons.ui_swing.dialogs;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableChangeEvent;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableComboBoxModel;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableComboBoxSelectionListener;
import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.validators.PortValidator;
import com.dmdirc.util.validators.ServerNameValidator;

import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;
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
public class NewServerDialog extends StandardDialog implements
        ActionListener, VetoableComboBoxSelectionListener, ConfigProviderListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 8;
    /** Identity Manager. */
    private final IdentityController identityController;
    /** Server manager. */
    private final ServerManager serverManager;
    /** Icon manager. */
    private final IconManager iconManager;
    /** Config. */
    private final AggregateConfigProvider config;
    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
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
    /** Opening new server? */
    private boolean openingServer = false;
    /** Provider to use to retrieve PMDs. */
    private final DialogProvider<ProfileManagerDialog> profileDialogProvider;

    /**
     * Creates a new instance of the dialog.
     *
     * @param mainWindow            Main window to use as a parent.
     * @param activeFrameManager    The active window manager
     * @param config                Config
     * @param iconManager           Icon manager
     * @param identityController    Identity controller
     * @param serverManager         Server manager
     * @param profileDialogProvider Provider to use to retrieve PMDs.
     */
    @Inject
    public NewServerDialog(
            @MainWindow final Window mainWindow,
            final ActiveFrameManager activeFrameManager,
            @GlobalConfig final AggregateConfigProvider config,
            @GlobalConfig final IconManager iconManager,
            final IdentityController identityController,
            final ServerManager serverManager,
            final DialogProvider<ProfileManagerDialog> profileDialogProvider) {
        super(mainWindow, ModalityType.MODELESS);
        this.identityController = identityController;
        this.serverManager = serverManager;
        this.activeFrameManager = activeFrameManager;
        this.iconManager = iconManager;
        this.config = config;
        this.profileDialogProvider = profileDialogProvider;

        initComponents();
        layoutComponents();
        addListeners();
        setResizable(false);

        identityController.registerIdentityListener("profile", this);
        update();
    }

    @Override
    public void display() {
        super.display();
        requestFocusInWindow();
        serverField.selectAll();
        serverField.requestFocus();
    }

    /** Updates the values to defaults. */
    private void update() {
        serverField.setText(config.getOption("general", "server"));
        portField.setText(config.getOption("general", "port"));
        passwordField.setText(config.getOption("general", "password"));
        sslCheck.setSelected(false);
        newServerWindowCheck.setEnabled(false);

        serverField.requestFocusInWindow();

        if (serverManager.numServers() == 0 || activeFrameManager.getActiveFrame() == null) {
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
        ((VetoableComboBoxModel) identityField.getModel()).addVetoableSelectionListener(this);
    }

    /**
     * Initialises the components in this dialog.
     */
    private void initComponents() {
        serverField = new ValidatingJTextField(iconManager, new ServerNameValidator());
        portField = new ValidatingJTextField(iconManager, new PortValidator());
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
        final List<ConfigProvider> profiles = identityController.getProvidersByType("profile");
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

        getContentPane().add(new JLabel("Enter the details of the server that "
                + "you wish to connect to."), "span 3, wrap 1.5*unrel");
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
            final URI address
                    = new URI("irc" + (sslCheck.isSelected() ? "s" : ""), pass, host, port, null,
                            null,
                            null);

            // Open in a new window?
            if (newServerWindowCheck.isSelected()
                    || serverManager.numServers() == 0
                    || activeFrameManager.getActiveFrame() == null) {

                new LoggingSwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        serverManager.connectToAddress(address, profile);
                        return null;
                    }
                }.executeInExecutor();
            } else {
                final Connection connection = activeFrameManager.getActiveFrame().getContainer().
                        getConnection();

                new LoggingSwingWorker<Void, Void>() {

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
            profileDialogProvider.displayOrRequestFocus(this);
        } else if (e.getSource() == getCancelButton()) {
            dispose();
        }
    }

    @Override
    public boolean enterPressed() {
        executeAction(getOkButton());
        return true;
    }

    @Override
    public boolean selectionChanged(final VetoableChangeEvent e) {
        return e.getNewValue() != null;
    }

    @Override
    public void configProviderAdded(final ConfigProvider configProvider) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                populateProfiles();
            }
        });
    }

    @Override
    public void dispose() {
        identityController.unregisterIdentityListener(this);
        super.dispose();
    }

    @Override
    public void configProviderRemoved(final ConfigProvider configProvider) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                populateProfiles();
            }
        });
    }

}

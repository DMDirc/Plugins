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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.dialogs.url.URLSubsitutionsPanel;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.ui.core.util.URLHandler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * URL Protocol configuration panel.
 */
public class URLProtocolPanel extends JPanel implements ActionListener,
        DocumentListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Global config. */
    private final AggregateConfigProvider globalConfig;
    /** User settings. */
    private final ConfigProvider userSettings;
    /** URL. */
    private final URI uri;
    /** Show insets? */
    private final boolean useInsets;
    /** Show file chooser. */
    private JButton showFileChooser;
    /** Command text field. */
    private JTextField commandPath;
    /** Option selection. */
    private ButtonGroup optionType;
    /** DMDirc choice. */
    private JRadioButton dmdirc;
    /** Browser choice. */
    private JRadioButton browser;
    /** Mail choice. */
    private JRadioButton mail;
    /** Custom command choice. */
    private JRadioButton custom;
    /** Substitutions label. */
    private JLabel subsLabel;
    /** example label. */
    private JLabel exampleLabel;
    /** Substitutions panel. */
    private URLSubsitutionsPanel subsPanel;

    /**
     * Instantiates the URLDialog.
     *
     * @param globalConfig Global configuration
     * @param userSettings User settings
     * @param url          URL to open once added
     * @param useInsets    Show insets?
     */
    public URLProtocolPanel(final AggregateConfigProvider globalConfig,
            final ConfigProvider userSettings,
            final URI url,
            final boolean useInsets) {
        super();

        this.globalConfig = globalConfig;
        this.userSettings = userSettings;
        uri = url;
        this.useInsets = useInsets;

        initComponents();
        layoutComponents();
        addListeners();
    }

    /** Initialises the components. */
    private void initComponents() {
        setOpaque(false);
        showFileChooser = new JButton("Browse");
        commandPath = new JTextField();
        optionType = new ButtonGroup();
        dmdirc = new JRadioButton("Use DMDirc");
        browser = new JRadioButton("Use browser (or system registered handler)");
        mail = new JRadioButton("Use mail client");
        custom = new JRadioButton("Custom command");
        subsLabel = new JLabel();
        exampleLabel = new JLabel();

        commandPath.setEnabled(false);
        showFileChooser.setEnabled(false);
        subsLabel.setEnabled(false);
        exampleLabel.setEnabled(false);

        optionType.add(dmdirc);
        optionType.add(browser);
        optionType.add(mail);
        optionType.add(custom);

        subsPanel = new URLSubsitutionsPanel(Arrays.asList(new String[]{
            "url",
            "fragment", "host", "path", "port", "query", "protocol",
            "username",
            "password"
        }));
        subsPanel.setVisible(custom.isSelected());

        updateSelection();
    }

    /** Lays out the components. */
    private void layoutComponents() {
        if (useInsets) {
            setLayout(new MigLayout("fillx, wrap 1, hidemode 3"));
        } else {
            setLayout(new MigLayout("ins 0, fillx, wrap 1, hidemode 3"));
        }

        add(dmdirc, "growx, pushx");
        add(browser, "growx, pushx");
        add(mail, "growx, pushx");
        add(custom, "growx, pushx");
        add(commandPath, "split 2, growx, pushx, sgy line");
        add(showFileChooser, "sgy line");
        add(subsLabel, "growx, pushx");
        add(exampleLabel, "width ::100%" + (useInsets ? "-2*u" : ""));
        add(subsPanel, "growx, pushx");
    }

    /** Adds listeners to the components. */
    private void addListeners() {
        showFileChooser.addActionListener(this);
        dmdirc.addActionListener(this);
        browser.addActionListener(this);
        mail.addActionListener(this);
        custom.addActionListener(this);
        commandPath.getDocument().addDocumentListener(this);
    }

    /** Saves the settings. */
    public void save() {
        userSettings.setOption("protocol", uri.getScheme().toLowerCase(), getSelection());
    }

    /**
     * Returns the selected value.
     *
     * @return Selected value
     */
    public String getSelection() {
        if (optionType.getSelection() == dmdirc.getModel()) {
            return "DMDIRC";
        } else if (optionType.getSelection() == browser.getModel()) {
            return "BROWSER";
        } else if (optionType.getSelection() == mail.getModel()) {
            return "MAIL";
        } else if (optionType.getSelection() == custom.getModel()) {
            return commandPath.getText();
        } else {
            return "";
        }
    }

    /**
     * Updates the example label.
     */
    private void updateExample() {
        if (uri == null) {
            setEnabled(false);
            exampleLabel.setText("Example: ");
        } else {
            exampleLabel.setText("Example: "
                    + URLHandler.substituteParams(uri, commandPath.getText()));
        }
    }

    /**
     * Updates the selection.
     */
    public void updateSelection() {
        if (uri != null && globalConfig.hasOptionString("protocol", uri.getScheme())) {
            final String option = globalConfig.getOption("protocol", uri.getScheme());
            switch (option) {
                case "DMDIRC":
                    optionType.setSelected(dmdirc.getModel(), true);
                    break;
                case "BROWSER":
                    optionType.setSelected(browser.getModel(), true);
                    break;
                case "MAIL":
                    optionType.setSelected(mail.getModel(), true);
                    break;
                default:
                    optionType.setSelected(custom.getModel(), true);
                    commandPath.setText(option);
                    actionPerformed(null);
                    break;
            }
        } else {
            optionType.clearSelection();
            commandPath.setText("");
        }

        updateExample();
    }

    
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        final Enumeration<AbstractButton> buttons = optionType.getElements();
        while (buttons.hasMoreElements()) {
            buttons.nextElement().setEnabled(enabled);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param e action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e != null && e.getSource() == showFileChooser) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new ExecutableFileFilter());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showDialog(this, "Select")
                    == JFileChooser.APPROVE_OPTION) {
                commandPath.setText(fileChooser.getSelectedFile().toString());
            }
        } else {
            subsPanel.setVisible(custom.isSelected());
            if (optionType.getSelection() == custom.getModel()) {
                commandPath.setEnabled(true);
                showFileChooser.setEnabled(true);
                subsLabel.setEnabled(true);
                exampleLabel.setEnabled(true);
            } else {
                commandPath.setEnabled(false);
                showFileChooser.setEnabled(false);
                subsLabel.setEnabled(false);
                exampleLabel.setEnabled(false);
            }
            validate();
        }
    }

    
    @Override
    public void insertUpdate(final DocumentEvent e) {
        updateExample();
    }

    
    @Override
    public void removeUpdate(final DocumentEvent e) {
        updateExample();
    }

    
    @Override
    public void changedUpdate(final DocumentEvent e) {
        // Ignore
    }

}

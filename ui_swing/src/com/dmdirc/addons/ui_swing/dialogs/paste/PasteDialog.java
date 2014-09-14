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

package com.dmdirc.addons.ui_swing.dialogs.paste;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.InputTextFrame;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
import com.dmdirc.addons.ui_swing.components.inputfields.TextAreaInputField;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to confirm and modify a multi-line paste.
 */
public final class PasteDialog extends StandardDialog implements ActionListener,
        KeyListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 4;
    /** Number of lines Label. */
    private TextLabel infoLabel;
    /** Text area scrollpane. */
    private JScrollPane scrollPane;
    /** Text area. */
    private TextAreaInputField textField;
    /** Parent frame. */
    private final InputTextFrame parent;
    /** Edit button. */
    private JButton editButton;
    /** Parent window. */
    private final Window parentWindow;
    /** Icon manager to retrieve icons with. */
    private final IconManager iconManager;
    /** Plugin Manager to retrieve tab completers with. */
    private final PluginManager pluginManager;
    /** Config to read settings from. */
    private final AggregateConfigProvider config;
    /** The controller to use to retrieve command information. */
    private final CommandController commandController;

    /**
     * Creates a new instance of PreferencesDialog.
     *
     * @param iconManager       Icon manager to retrieve icons with
     * @param config            Config to read settings from
     * @param pluginManager     to retrieve tab completers with
     * @param commandController The controller to use to retrieve command information.
     * @param eventBus          The bus to dispatch events on.
     * @param newParent         The frame that owns this dialog
     * @param text              text to show in the paste dialog
     * @param parentWindow      Parent window
     */
    public PasteDialog(
            final IconManager iconManager,
            final AggregateConfigProvider config,
            final PluginManager pluginManager,
            final CommandController commandController,
            final DMDircMBassador eventBus,
            final InputTextFrame newParent,
            final String text,
            final Window parentWindow,
            final ColourManagerFactory colourManagerFactory) {
        super(parentWindow, ModalityType.MODELESS);

        parent = newParent;
        this.parentWindow = parentWindow;
        this.iconManager = iconManager;
        this.config = config;
        this.pluginManager = pluginManager;
        this.commandController = commandController;

        initComponents(eventBus, text, colourManagerFactory);
        initListeners();

        setFocusTraversalPolicy(new PasteDialogFocusTraversalPolicy(
                getCancelButton(), editButton, getOkButton()));

        setFocusable(true);
        getOkButton().requestFocusInWindow();
        getOkButton().setSelected(true);
    }

    /**
     * Initialises GUI components.
     *
     * @param text text to show in the dialog
     */
    private void initComponents(final DMDircMBassador eventBus, final String text,
            final ColourManagerFactory colourManagerFactory) {
        scrollPane = new JScrollPane();
        textField = new TextAreaInputField(iconManager, colourManagerFactory, config, text);
        editButton = new JButton("Edit");
        infoLabel = new TextLabel();

        UIUtilities.addUndoManager(eventBus, textField);

        orderButtons(new JButton(), new JButton());
        getOkButton().setText("Send");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Multi-line paste");
        setResizable(false);

        infoLabel.setText("This will be sent as "
                + parent.getContainer().getNumLines(textField.getText())
                + " lines. Are you sure you want to continue?");

        textField.setColumns(50);
        textField.setRows(10);

        new SwingInputHandler(pluginManager, textField, commandController,
                parent.getContainer().getCommandParser(),
                parent.getContainer(), eventBus).setTypes(false, false, true, false);

        scrollPane.setViewportView(textField);
        scrollPane.setVisible(false);

        getContentPane().setLayout(new MigLayout("fill, hidemode 3"));
        getContentPane().add(infoLabel, "wrap, growx, pushx, span 3");
        getContentPane().add(scrollPane, "wrap, grow, push, span 3");
        getContentPane().add(getLeftButton(), "right, sg button");
        getContentPane().add(editButton, "right, sg button");
        getContentPane().add(getRightButton(), "right, sg button");
    }

    /**
     * Initialises listeners for this dialog.
     */
    private void initListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        editButton.addActionListener(this);
        textField.addKeyListener(this);

        getRootPane().getActionMap().put("rightArrowAction",
                new AbstractAction("rightArrowAction") {
                    private static final long serialVersionUID = 1;

                    @Override
                    public void actionPerformed(final ActionEvent evt) {
                        final JButton button = (JButton) getFocusTraversalPolicy().
                        getComponentAfter(PasteDialog.this, getFocusOwner());
                        button.requestFocusInWindow();
                        button.setSelected(true);
                    }
                });

        getRootPane().getActionMap().put("leftArrowAction",
                new AbstractAction("leftArrowAction") {
                    private static final long serialVersionUID = 1;

                    @Override
                    public void actionPerformed(final ActionEvent evt) {
                        final JButton button = (JButton) getFocusTraversalPolicy().
                        getComponentBefore(PasteDialog.this, getFocusOwner());
                        button.requestFocusInWindow();
                        button.setSelected(true);
                    }
                });

        getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "rightArrowAction");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "leftArrowAction");
    }

    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (getOkButton().equals(actionEvent.getSource())) {
            if (!textField.getText().isEmpty()) {
                final String[] lines = textField.getText().split("(\n|\r\n|\r)",
                        Integer.MAX_VALUE);
                for (final String line : lines) {
                    if (!line.isEmpty()) {
                        parent.getContainer().sendLine(line);
                        parent.getInputHandler().addToBuffer(line);
                    }
                }
            }
            dispose();
        } else if (editButton.equals(actionEvent.getSource())) {
            editButton.setEnabled(false);
            setResizable(true);
            scrollPane.setVisible(true);
            infoLabel.setText("This will be sent as "
                    + parent.getContainer().getNumLines(textField.getText())
                    + " lines.");
            setResizable(true);
            pack();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setLocationRelativeTo(parentWindow);
                }
            });
        } else if (getCancelButton().equals(actionEvent.getSource())) {
            dispose();
        }
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        infoLabel.setText("This will be sent as "
                + parent.getContainer().getNumLines(textField.getText())
                + " lines.");
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        // Do nothing
    }

}
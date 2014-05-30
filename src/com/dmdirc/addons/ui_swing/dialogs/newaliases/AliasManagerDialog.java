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

package com.dmdirc.addons.ui_swing.dialogs.newaliases;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.components.validating.ValidatableJTextField;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ui.AliasDialogModel;
import com.dmdirc.ui.IconManager;

import java.awt.Dimension;
import java.awt.Window;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;

/**
 * Dialog to list and change command aliases.
 */
public class AliasManagerDialog extends StandardDialog {

    private static final long serialVersionUID = 1;
    private final AliasManagerModel model;
    private final AliasManagerController controller;
    private final AliasManagerLinker linker;

    @Inject
    public AliasManagerDialog(@MainWindow final Window mainFrame,
            final AliasDialogModel dialogModel,
            @GlobalConfig final IconManager iconManager,
            final CommandController commandController) {
        super(mainFrame, ModalityType.DOCUMENT_MODAL);
        this.model = new AliasManagerModel(dialogModel, commandController);
        controller = new AliasManagerController(this, model);
        linker = new AliasManagerLinker(controller, model, this, iconManager);
        setTitle("Alias Manager");
        final JTable aliasList = new JTable();
        final ValidatableJTextField command = new ValidatableJTextField(iconManager);
        final JSpinner argumentsNumber = new JSpinner();
        final JTextArea response = new JTextArea();
        final JButton addAlias = new JButton("Add Alias");
        final JButton deleteAlias = new JButton("Delete Alias");
        getOkButton();
        getCancelButton();
        setLayout(new MigLayout("fill, pack"));
        setMinimumSize(new Dimension(800, 400));
        final JScrollPane scrollPane = new JScrollPane(aliasList);
        aliasList.setPreferredScrollableViewportSize(new Dimension(800, 150));
        scrollPane.setMinimumSize(new Dimension(750, 150));
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollPane,
                getAliasDetails(command, argumentsNumber, response));
        splitPane.setDividerSize((int) PlatformDefaults.getPanelInsets(0).getValue());

        add(splitPane, "spanx 5, grow, push, wrap");
        add(addAlias, "split 2, sgx button");
        add(deleteAlias, "sgx button");
        add(getLeftButton(), "sgx button");
        add(getRightButton(), "sgx button");

        linker.bindCommandList(aliasList);
        linker.bindCommand(command);
        linker.bindArgumentsNumber(argumentsNumber);
        linker.bindResponse(response);
        linker.bindAddAlias(addAlias);
        linker.bindDeleteAlias(deleteAlias);
        linker.bindOKButton(getOkButton());
        linker.bindCancelButton(getCancelButton());
        model.load();
    }

    /**
     * Creates a panel showing all alias details.
     *
     * @param command         Command name
     * @param argumentsNumber Number of arguments
     * @param response        Alias substitution
     *
     * @return Panel to display
     */
    private JPanel getAliasDetails(final ValidatableJTextField command,
            final JSpinner argumentsNumber, final JTextArea response) {
        final JPanel aliasDetails = new JPanel();
        aliasDetails.setLayout(new MigLayout("fill, ins 0"));
        aliasDetails.add(new JLabel("Command: "));
        aliasDetails.add(command, "sgy args, growx, pushx");
        aliasDetails.add(new JLabel("#Arguments: "));
        aliasDetails.add(argumentsNumber, "sgy args, growx, pushx, wrap");
        aliasDetails.add(new JLabel("Response: "));
        aliasDetails.add(new JScrollPane(response), "span 3, grow, push, wrap");
        return aliasDetails;
    }

}

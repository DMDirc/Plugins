/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs.aliases;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidationFactory;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.commandparser.aliases.Alias;
import com.dmdirc.interfaces.ui.AliasDialogModel;
import com.dmdirc.util.validators.NotEmptyValidator;

import java.awt.Dimension;
import java.awt.Window;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * Dialog to list and change command aliases.
 */
public class AliasManagerDialog extends StandardDialog {

    private static final long serialVersionUID = 1;
    private final AliasDialogModel model;
    private final IconManager iconManager;
    private final DMDircMBassador eventBus;
    private JList<Alias> aliasList;
    private JTextField command;
    private JSpinner argumentsNumber;
    private JTextArea response;
    private JButton addAlias;
    private JButton deleteAlias;
    private JScrollPane responseScroll;

    @Inject
    public AliasManagerDialog(@MainWindow final Window mainFrame, final AliasDialogModel model,
            final IconManager iconManager, final DMDircMBassador eventBus) {
        super(mainFrame, ModalityType.DOCUMENT_MODAL);
        this.model = model;
        this.iconManager = iconManager;
        this.eventBus = eventBus;
        initComponents();
        layoutComponents();
    }

    @Override
    public void display() {
        final AliasManagerLinker linker = new AliasManagerLinker(model, this, iconManager);
        UIUtilities.addUndoManager(eventBus, response);
        linker.bindCommandList(aliasList);
        linker.bindCommand(command);
        linker.bindArgumentsNumber(argumentsNumber);
        linker.bindResponse(response, responseScroll);
        linker.bindAddAlias(addAlias);
        linker.bindDeleteAlias(deleteAlias);
        linker.bindOKButton(getOkButton());
        linker.bindCancelButton(getCancelButton());
        linker.init();
        model.loadModel();
        super.display();
    }

    private void initComponents() {
        setTitle("Alias Manager");
        aliasList = new JList<>();
        command = new JTextField();
        argumentsNumber = new JSpinner();
        response = new JTextArea();
        addAlias = new JButton("Add Alias");
        deleteAlias = new JButton("Delete Alias");
        responseScroll = new JScrollPane(response);
    }

    private void layoutComponents() {
        setMinimumSize(new Dimension(800, 400));
        setPreferredSize(new Dimension(800, 400));
        setLayout(new MigLayout("flowy, fill", "[][][grow]", "[][][][grow][]"));

        add(new TextLabel("Aliases allow you to rename commands, to aggregate multiple commands "
                + "into a single command, or to make shortcuts to common commands."), "spanx, pushy");
        add(new JScrollPane(aliasList),
                "growy, pushy, spany 3, split 3, wmin 200, wmax 200");
        add(addAlias, "growx");
        add(deleteAlias, "growx, wrap");

        add(new JLabel("Command: "), "align label, sgx label");
        add(new JLabel("Minimum Arguments: "), "align label, sgx label");
        add(new JLabel("Response: "), "align label, sgx label, wrap");

        add(ValidationFactory.getValidatorPanel(command, model.getCommandValidator(), iconManager),
                "growx, pushx");
        add(argumentsNumber, "growx, pushx");
        add(ValidationFactory.getValidatorPanel(responseScroll, response,
                new NotEmptyValidator(), iconManager), "spanx 2, grow, push");

        add(getLeftButton(), "flowx, split 3, right, sg button");
        add(getRightButton(), "sg button");
    }
}

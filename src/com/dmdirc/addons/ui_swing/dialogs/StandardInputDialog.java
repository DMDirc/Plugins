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
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.util.validators.Validator;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

/**
 * Standard input dialog.
 */
@Factory(inject = true)
public class StandardInputDialog extends StandardDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Validator. */
    private Validator<String> validator;
    /** Text field. */
    private ValidatingJTextField textField;
    /** Blurb label. */
    private TextLabel blurb;
    /** Message. */
    private String message;
    /** The icon manager to use for validating text fields. */
    private final IconManager iconManager;
    /** Dialog close listener called when saving or cancelling. */
    private final InputDialogCloseListener listener;
    /** Are we saving? */
    protected final AtomicBoolean saving = new AtomicBoolean(false);

    /**
     * Instantiates a new standard input dialog.
     *
     * @param owner       Dialog owner
     * @param iconManager The icon manager to use for validating text fields.
     * @param title       Dialog title
     * @param message     Dialog message
     * @param listener    Listener to call on dialog close
     */
    public StandardInputDialog(
            @Unbound final Window owner,
            @SuppressWarnings("qualifiers") @GlobalConfig final IconManager iconManager,
            @Unbound final String title,
            @Unbound final String message,
            @Unbound final InputDialogCloseListener listener) {
        this(owner, iconManager, title, message, listener, new Validator<String>() {
            @Override
            public ValidationResponse validate(final String object) {
                return new ValidationResponse();
            }
        });
    }

    /**
     * Instantiates a new standard input dialog.
     *
     * @param owner       Dialog owner
     * @param iconManager The icon manager to use for validating text fields.
     * @param title       Dialog title
     * @param message     Dialog message
     * @param listener    Listener to call on dialog close
     * @param validator   Textfield validator
     */
    public StandardInputDialog(
            @Unbound final Window owner,
            @SuppressWarnings("qualifiers") @GlobalConfig final IconManager iconManager,
            @Unbound final String title,
            @Unbound final String message,
            @Unbound final InputDialogCloseListener listener,
            @Unbound final Validator<String> validator) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.validator = validator;
        this.message = message;
        this.iconManager = iconManager;
        this.listener = listener;

        setTitle(title);
        setDefaultCloseOperation(StandardInputDialog.DISPOSE_ON_CLOSE);

        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        textField = new ValidatingJTextField(iconManager, validator);
        blurb = new TextLabel(message);
        validateText();
    }

    /**
     * Adds the listeners.
     */
    private void addListeners() {
        getOkButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (listener.save(getText())) {
                    dispose();
                }
            }
        });
        getCancelButton().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                listener.cancelled();
                dispose();
            }
        });
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(final WindowEvent e) {
                textField.requestFocusInWindow();
            }

            @Override
            public void windowClosed(final WindowEvent e) {
                listener.cancelled();
            }
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(final DocumentEvent e) {
                validateText();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                validateText();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                //Ignore
            }
        });
    }

    @Override
    public boolean enterPressed() {
        executeAction(getOkButton());
        return true;
    }

    /**
     * Validates the change.
     */
    private void validateText() {
        getOkButton().setEnabled(!validator.validate(getText()).isFailure());
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1"));

        add(blurb, "growx");
        add(textField, "growx");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");
    }

    /**
     * Returns the text in the input field.
     *
     * @return Input text
     */
    public final String getText() {
        return textField.getText();
    }

    /**
     * Sets the dialogs text to the specified text.
     *
     * @param text New test
     */
    public final void setText(final String text) {
        textField.setText(text);
    }

}

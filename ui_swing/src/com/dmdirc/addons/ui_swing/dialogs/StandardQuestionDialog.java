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

import com.dmdirc.addons.ui_swing.components.text.TextLabel;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BooleanSupplier;

import javax.swing.JButton;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

/**
 * Standard input dialog.
 */
public class StandardQuestionDialog extends StandardDialog {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Message. */
    private final String message;
    /** Blurb label. */
    private TextLabel blurb;
    /** Question result. */
    private boolean result;
    /** Function to call when the dialog is saved. */
    private final BooleanSupplier save;
    /** Function to call when the dialog is cancelled. */
    private final Runnable cancel;

    /**
     * Instantiates a new standard input dialog.
     *
     * @param owner   Dialog owner
     * @param modal   modality type
     * @param title   Dialog title
     * @param message Dialog message
     */
    public StandardQuestionDialog(
            final Window owner, final ModalityType modal, final String title,
            final String message,
            final BooleanSupplier save) {
        this(owner, modal, title, message, save, () -> {});
    }

    /**
     * Instantiates a new standard input dialog.
     *
     * @param owner   Dialog owner
     * @param modal   modality type
     * @param title   Dialog title
     * @param message Dialog message
     */
    public StandardQuestionDialog(
            final Window owner, final ModalityType modal, final String title,
            final String message,
            final Runnable save) {
        this(owner, modal, title, message, () -> {save.run(); return true; }, () -> {});
    }

    /**
     * Instantiates a new standard input dialog.
     *
     * @param owner   Dialog owner
     * @param modal   modality type
     * @param title   Dialog title
     * @param message Dialog message
     */
    public StandardQuestionDialog(
            final Window owner, final ModalityType modal, final String title,
            final String message,
            final BooleanSupplier save,
            final Runnable cancel) {
        super(owner, modal);

        this.message = message;
        this.save = save;
        this.cancel = cancel;

        setTitle(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        initComponents();
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        getOkButton().setText("Yes");
        getCancelButton().setText("No");
        blurb = new TextLabel(message);
    }

    /**
     * Adds the listeners.
     */
    private void addListeners() {
        getOkButton().addActionListener(e -> {
            if (save.getAsBoolean()) {
                result = true;
                dispose();
            }
        });
        getCancelButton().addActionListener(e -> {
            cancel.run();
            dispose();
        });
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(final WindowEvent e) {
                //Ignore
            }

            @Override
            public void windowClosed(final WindowEvent e) {
                if (!result) {
                    cancel.run();
                }
            }
        });
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, hidemode 3"));

        add(blurb, "growx");
        add(getLeftButton(), "split 2, right");
        add(getRightButton(), "right");
    }

    /**
     * Returns the result of the question.
     *
     * @return true iif the user pressed Yes
     */
    public boolean getResult() {
        return result;
    }

}

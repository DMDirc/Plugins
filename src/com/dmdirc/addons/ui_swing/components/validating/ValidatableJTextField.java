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

package com.dmdirc.addons.ui_swing.components.validating;

import com.dmdirc.ui.IconManager;
import com.dmdirc.util.validators.Validatable;
import com.dmdirc.util.validators.ValidationResponse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.miginfocom.swing.MigLayout;

/**
 * Validating Text field.
 */
public class ValidatableJTextField extends JComponent implements Validatable {

    /** Serial Version UID. */
    private static final long serialVersionUID = 1;
    /** TextField. */
    private final JTextField textField;
    /** Error icon. */
    private final JLabel errorIcon;

    /**
     * Instantiates a new Validating text field.
     *
     * @param iconManager Icon manager
     */
    public ValidatableJTextField(final IconManager iconManager) {
        this(iconManager, new JTextField());
    }

    /**
     * Instantiates a new Validating text field.
     *
     * @param iconManager Icon manager
     * @param textField JTextField to wrap
     */
    public ValidatableJTextField(final IconManager iconManager,
            final JTextField textField) {
        this(iconManager.getIcon("input-error"), textField);
    }

    /**
     * Instantiates a new Validating text field.
     *
     * @param icon Icon to show on error
     * @param textField JTextField to wrap
     */
    public ValidatableJTextField(final Icon icon, final JTextField textField) {
        super();
        this.textField = textField;
        errorIcon = new JLabel(icon);

        setLayout(new MigLayout("fill, ins 0, hidemode 3, gap 0"));
        add(textField, "grow, pushx");
        add(errorIcon);

        errorIcon.setVisible(false);
    }

    /** {@inheritDoc} */
    @Override
    public void setValidation(final ValidationResponse validation) {
        if (validation.isFailure()) {
            errorIcon.setVisible(true);
            errorIcon.setToolTipText(validation.getFailureReason());
        } else {
            errorIcon.setVisible(false);
            errorIcon.setToolTipText(null);
        }
    }

    /**
     * Returns the text field used by this validating text field.
     *
     * @since 0.6.3m1
     *
     * @return This field's text field
     */
    public JTextField getTextField() {
        return textField;
    }

    @Override
    public void setFont(final Font f) {
        textField.setFont(f);
    }

    public synchronized void addActionListener(final ActionListener l) {
        textField.addActionListener(l);
    }

    public void setUI(final TextUI ui) {
        textField.setUI(ui);
    }

    public void addCaretListener(final CaretListener listener) {
        textField.addCaretListener(listener);
    }

    public Document getDocument() {
        return textField.getDocument();
    }

    public void setMargin(final Insets m) {
        textField.setMargin(m);
    }

    public void setCaretColor(final Color c) {
        textField.setCaretColor(c);
    }

    public void setSelectionColor(final Color c) {
        textField.setSelectionColor(c);
    }

    public void setSelectedTextColor(final Color c) {
        textField.setSelectedTextColor(c);
    }

    public void setDisabledTextColor(final Color c) {
        textField.setDisabledTextColor(c);
    }

    public void replaceSelection(final String content) {
        textField.replaceSelection(content);
    }

    public String getText(final int offs, final int len) throws BadLocationException {
        return textField.getText(offs, len);
    }

    public void cut() {
        textField.cut();
    }

    public void copy() {
        textField.copy();
    }

    public void paste() {
        textField.paste();
    }

    public void setCaretPosition(final int position) {
        textField.setCaretPosition(position);
    }

    public void setText(final String t) {
        textField.setText(t);
    }

    public String getText() {
        return textField.getText();
    }

    public String getSelectedText() {
        return textField.getSelectedText();
    }

    public boolean isEditable() {
        return textField.isEditable();
    }

    public void setEditable(final boolean b) {
        textField.setEditable(b);
    }

    public void setSelectionStart(final int selectionStart) {
        textField.setSelectionStart(selectionStart);
    }

    public void setSelectionEnd(final int selectionEnd) {
        textField.setSelectionEnd(selectionEnd);
    }

    public void select(final int selectionStart, final int selectionEnd) {
        textField.select(selectionStart, selectionEnd);
    }

    public void selectAll() {
        textField.selectAll();
    }

    @Override
    public void requestFocus() {
        textField.requestFocus();
    }

    @Override
    public boolean requestFocus(final boolean temporary) {
        return textField.requestFocus(temporary);
    }

    @Override
    public boolean requestFocusInWindow() {
        return textField.requestFocusInWindow();
    }

    @Override
    public void setBorder(final Border border) {
        textField.setBorder(border);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        textField.setEnabled(enabled);
    }

    @Override
    public void setForeground(final Color fg) {
        textField.setForeground(fg);
    }

    @Override
    public void setBackground(final Color bg) {
        textField.setBackground(bg);
    }

    @Override
    public Color getForeground() {
        return textField.getForeground();
    }

    @Override
    public Color getBackground() {
        return textField.getBackground();
    }

    @Override
    public Font getFont() {
        return textField.getFont();
    }

    @Override
    public synchronized void addFocusListener(final FocusListener l) {
        textField.addFocusListener(l);
    }

    @Override
    public synchronized void addKeyListener(final KeyListener l) {
        textField.addKeyListener(l);
    }

    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        textField.addMouseListener(l);
    }

    @Override
    public synchronized void addMouseMotionListener(final MouseMotionListener l) {
        textField.addMouseMotionListener(l);
    }
}

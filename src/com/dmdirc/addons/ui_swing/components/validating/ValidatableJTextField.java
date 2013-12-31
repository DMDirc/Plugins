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

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import lombok.Delegate;

import net.miginfocom.swing.MigLayout;

/**
 * Validating Text field.
 */
public class ValidatableJTextField extends JComponent implements Validatable {

    /** Serial Version UID. */
    private static final long serialVersionUID = 1;
    /** TextField. */
    @Delegate
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
}

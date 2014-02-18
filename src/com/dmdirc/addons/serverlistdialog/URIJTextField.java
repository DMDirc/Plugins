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

package com.dmdirc.addons.serverlistdialog;

import com.dmdirc.addons.ui_swing.components.validating.ValidatingJTextField;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.validators.Validator;

/**
 * Simple extention of JTextField to return an URI usable string (defaulting to an irc scheme if
 * none specified).
 */
public class URIJTextField extends ValidatingJTextField {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;

    public URIJTextField(final IconManager iconManager, final String text,
            final Validator<String> validator) {
        super(iconManager, text, validator);
    }

    @Override
    public String getText() {
        final String hostname = super.getText();
        if (hostname.indexOf("://") == -1) {
            return "irc://" + hostname;
        } else {
            return hostname;
        }
    }

}

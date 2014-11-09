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


import java.util.function.Consumer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple @{link DocumentListener} that calls the specified function on any change.
 */
public class ConsumerDocumentListener implements DocumentListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConsumerDocumentListener.class);
    private final Consumer<String> function;

    public ConsumerDocumentListener(final Consumer<String> function) {
        this.function = function;
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        changed(e);
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        changed(e);
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        changed(e);
    }

    private void changed(final DocumentEvent e) {
        try {
            function.accept(e.getDocument().getText(0, e.getDocument().getLength()));
        } catch (BadLocationException e1) {
            LOGGER.warn("Unable to fire document change", (Throwable) e);
        }
    }
}

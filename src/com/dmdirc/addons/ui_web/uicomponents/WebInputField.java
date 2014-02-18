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

package com.dmdirc.addons.ui_web.uicomponents;

import com.dmdirc.addons.ui_web.Client;
import com.dmdirc.addons.ui_web.Event;
import com.dmdirc.interfaces.ui.InputField;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

/**
 * An input field for a specific client.
 */
public class WebInputField implements InputField {

    private Client client;
    private String text;
    private int selStart;
    private int selEnd;

    public WebInputField() {
        super();
    }

    public WebInputField(final Client client) {
        this.client = client;
    }

    /** {@inheritDoc} */
    @Override
    public void addActionListener(final ActionListener listener) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addKeyListener(final KeyListener listener) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasFocus() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void removeActionListener(final ActionListener listener) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void removeKeyListener(final KeyListener listener) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public String getSelectedText() {
        return text.substring(selStart, selEnd);
    }

    /** {@inheritDoc} */
    @Override
    public int getSelectionEnd() {
        return selEnd;
    }

    /** {@inheritDoc} */
    @Override
    public int getSelectionStart() {
        return selStart;
    }

    /** {@inheritDoc} */
    @Override
    public String getText() {
        return text;
    }

    /** {@inheritDoc} */
    @Override
    public void setText(final String text) {
        client.addEvent(new Event("settext", text));
    }

    /** {@inheritDoc} */
    @Override
    public int getCaretPosition() {
        return selEnd;
    }

    /** {@inheritDoc} */
    @Override
    public void setCaretPosition(final int position) {
        client.addEvent(new Event("setcaret", position));
    }

    /** {@inheritDoc} */
    @Override
    public void showColourPicker(final boolean irc, final boolean hex) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void hideColourPicker() {
        // Do nothing
    }

    public void setSelEnd(final int selEnd) {
        this.selEnd = selEnd;
    }

    public void setSelStart(final int selStart) {
        this.selStart = selStart;
    }

    public void setContent(final String text) {
        this.text = text;
    }

}

/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.ui.interfaces.InputField;
import com.dmdirc.addons.ui_web.DynamicRequestHandler;
import com.dmdirc.addons.ui_web.Event;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

/**
 *
 * @author chris
 */
public class WebInputField implements InputField {
    
    private String clientID;
    
    private String text;
    
    private int selStart, selEnd;

    public WebInputField() {
    }

    public WebInputField(String clientID) {
        this.clientID = clientID;
    }

    /** {@inheritDoc} */
    @Override
    public void addActionListener(ActionListener listener) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void addKeyListener(KeyListener listener) {
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
    public void removeActionListener(ActionListener listener) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void removeKeyListener(KeyListener listener) {
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
    public void setText(String text) {
        DynamicRequestHandler.addEvent(clientID, new Event("settext", text));
    }

    /** {@inheritDoc} */
    @Override
    public int getCaretPosition() {
        return selEnd;
    }

    /** {@inheritDoc} */
    @Override
    public void setCaretPosition(int position) {
        DynamicRequestHandler.addEvent(clientID, new Event("setcaret", position));
    }

    /** {@inheritDoc} */
    @Override
    public void showColourPicker(boolean irc, boolean hex) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void hideColourPicker() {
        // Do nothing
    }

    public void setSelEnd(int selEnd) {
        this.selEnd = selEnd;
    }

    public void setSelStart(int selStart) {
        this.selStart = selStart;
    }
    
    public void setContent(final String text) {
        this.text = text;
    }

}

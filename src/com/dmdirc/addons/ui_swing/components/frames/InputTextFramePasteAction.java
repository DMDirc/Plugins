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

package com.dmdirc.addons.ui_swing.components.frames;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Paste action for input frames.
 */
public final class InputTextFramePasteAction extends AbstractAction {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Clipboard to paste from. */
    private final Clipboard clipboard;
    /** Text component to be acted upon. */
    private final InputTextFrame inputFrame;

    /**
     * Instantiates a new paste action.
     *
     * @param clipboard Clipboard to paste from
     * @param inputFrame Component to be acted upon
     */
    public InputTextFramePasteAction(final Clipboard clipboard, final InputTextFrame inputFrame) {
        super("Paste");

        this.clipboard = clipboard;
        this.inputFrame = inputFrame;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        inputFrame.doPaste();
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingNPE")
    public boolean isEnabled() {
        try {
            return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        } catch (NullPointerException | IllegalStateException ex) { //https://bugs.openjdk.java.net/browse/JDK-7000965
            return false;
        }
    }

}
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

package com.dmdirc.addons.ui_swing.actions;

import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;

/**
 * Paste action that replaces matching regexes.
 */
public final class ReplacePasteAction extends AbstractAction {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Regex to match for replacement. */
    private final String replacementRegex;
    /** Replacement string. */
    private final String replacementString;

    /**
     * Creates a new instance of regex replacement paste action.
     *
     * @param replacementRegex  Regex to match for replacement
     * @param replacementString Replacement string
     */
    public ReplacePasteAction(final String replacementRegex,
            final String replacementString) {
        super("NoSpacesPasteAction");

        this.replacementRegex = replacementRegex;
        this.replacementString = replacementString;
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (Toolkit.getDefaultToolkit().getSystemClipboard() == null) {
            return;
        }
        if (!(e.getSource() instanceof JTextComponent)
                || !Toolkit.getDefaultToolkit().getSystemClipboard().
                isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            return;
        }

        try {
            //Get clipboard clipboard contents
            //Replace spaces with nothing
            //Replace the current selection with the contents of the clipboard
            ((JTextComponent) e.getSource()).replaceSelection(((String) Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getData(DataFlavor.stringFlavor))
                    .replaceAll(replacementRegex, replacementString));
        } catch (IOException ex) {
            Logger.userError(ErrorLevel.LOW, "Unable to get clipboard "
                    + "contents: " + ex.getMessage());
        } catch (UnsupportedFlavorException ex) {
            Logger.appError(ErrorLevel.LOW, "Unable to get clipboard "
                    + "contents", ex);
        }
    }

}

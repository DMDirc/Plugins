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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.addons.ui_swing.dialogs.paste.PasteDialogFactory;
import com.dmdirc.addons.ui_swing.injection.MainWindow;

import java.awt.Window;
import java.awt.datatransfer.Clipboard;

import javax.inject.Inject;

/**
 * Factory to create an {@link InputTextFramePasteAction}.
 */
public class InputTextFramePasteActionFactory {

    private final Clipboard clipboard;
    private final DMDircMBassador eventBus;
    private final PasteDialogFactory pasteDialogFactory;
    private final Window window;

    @Inject
    public InputTextFramePasteActionFactory(
            final Clipboard clipboard,
            final DMDircMBassador eventBus,
            final PasteDialogFactory pasteDialogFactory,
            @MainWindow final Window window) {
        this.clipboard = clipboard;
        this.eventBus = eventBus;
        this.pasteDialogFactory = pasteDialogFactory;
        this.window = window;
    }

    public InputTextFramePasteAction getInputTextFramePasteAction(
            final InputTextFrame inputFrame,
            final SwingInputField inputField,
            final FrameContainer container) {
        return new InputTextFramePasteAction(inputFrame, inputField, container, clipboard,
                eventBus, pasteDialogFactory, window);
    }
}

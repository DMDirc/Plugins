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

import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import com.google.common.eventbus.EventBus;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Handles undo's on text components.
 */
public final class UndoAction extends AbstractAction {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Undo manager. */
    private final UndoManager undoManager;
    /** The event bus to post errors to. */
    private final EventBus eventBus;

    /**
     * Creates a new instance of UndoAction.
     *
     * @param eventBus    The event bus to post errors to
     * @param undoManager UndoManager to use for this redo action
     */
    public UndoAction(final EventBus eventBus, final UndoManager undoManager) {
        super("Undo");

        this.undoManager = undoManager;
        this.eventBus = eventBus;
    }

    @Override
    public void actionPerformed(final ActionEvent evt) {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } catch (CannotUndoException ex) {
            eventBus.post(new UserErrorEvent(ErrorLevel.LOW, ex, "Unable to undo", ""));
        }
    }

}

/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_dummy;

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.interfaces.ui.InputField;
import com.dmdirc.interfaces.ui.UIController;

/**
 * Dummy input handler.
 */
public class DummyInputHandler extends InputHandler {

    /**
     * Creates a new instance of InputHandler. Adds listeners to the target
     * that we need to operate.
     *
     * @param controller The controller that owns us.
     * @param target The text field this input handler is dealing with.
     * @param commandParser The command parser to use for this text field.
     * @param parentWindow The window that owns this input handler
     */
    public DummyInputHandler(final UIController controller,
            final InputField target,
            final CommandParser commandParser,
            final WritableFrameContainer parentWindow) {
        super(controller, target, commandParser, parentWindow);
    }

    /** {@inheritDoc} */
    @Override
    protected void addUpHandler() {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    protected void addDownHandler() {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    protected void addTabHandler() {
        //Ignore
    }

    /** {@inheritDoc} */
    @Override
    protected void addKeyHandler() {
        //Ignore
    }

    @Override
    protected void addEnterHandler() {
        //Ignore
    }

}

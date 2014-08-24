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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.ui.InputField;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.input.TabCompleter;

/**
 * An input handler for the web ui.
 */
public class WebInputHandler extends InputHandler {

    public WebInputHandler(
            final ServiceManager serviceManager,
            final InputField target,
            final CommandController commandController,
            final CommandParser commandParser,
            final FrameContainer parentWindow,
            final DMDircMBassador eventBus) {
        super(serviceManager, target, commandController, commandParser, parentWindow, eventBus);
    }

    public InputField getTarget() {
        return target;
    }

    @Override
    protected void addUpHandler() {
        // Do nothing
    }

    @Override
    protected void addDownHandler() {
        // Do nothing
    }

    @Override
    protected void addTabHandler() {
        // Do nothing
    }

    @Override
    protected void addKeyHandler() {
        // Do nothing
    }

    @Override
    protected void addEnterHandler() {
        // Do nothing
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    @Override
    @SuppressWarnings("PMD")
    public void doTabCompletion(final boolean shiftPressed) {
        super.doTabCompletion(shiftPressed);
    }

    @Override
    @SuppressWarnings("PMD")
    public void doBufferDown() {
        super.doBufferDown();
    }

    @Override
    @SuppressWarnings("PMD")
    public void doBufferUp() {
        super.doBufferUp();
    }

    @Override
    @SuppressWarnings("PMD")
    public void handleKeyPressed(final String line, final int caretPosition,
            final int keyCode, final boolean shiftPressed,
            final boolean ctrlPressed) {
        super.handleKeyPressed(line, caretPosition, keyCode, shiftPressed, ctrlPressed);
    }

}

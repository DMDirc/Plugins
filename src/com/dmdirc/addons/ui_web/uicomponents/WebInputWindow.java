/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.ui_web.WebInterfaceUI;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.input.InputHandler;
import com.dmdirc.ui.interfaces.InputWindow;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chris
 */
public class WebInputWindow extends WebWindow implements InputWindow {

    private final WritableFrameContainer parent;

    private final CommandParser commandparser;

    private final WebInputHandler inputhandler;

    private final Map<String, WebInputHandler> inputHandlers
            = new HashMap<String, WebInputHandler>();

    public WebInputWindow(final WebInterfaceUI controller,
            final WritableFrameContainer parent) {
        super(controller, parent);
        this.parent = parent;
        this.commandparser = parent.getCommandParser();
        this.inputhandler = new WebInputHandler(new WebInputField(),
                commandparser, getContainer());
    }

    /** {@inheritDoc} */
    @Override
    public InputHandler getInputHandler() {
        return inputhandler;
    }

    public InputHandler getInputHandler(final String clientID) {
        if (!inputHandlers.containsKey(clientID)) {
            final WebInputHandler ih = new WebInputHandler(
                    new WebInputField(clientID), commandparser, getContainer());
            ih.setTabCompleter(inputhandler.getTabCompleter());
            inputHandlers.put(clientID, ih);
        }

        return inputHandlers.get(clientID);
    }

    public InputHandler getInputHandler(final String clientID,
            final String text, final String selStart, final String selEnd) {
        int sel1, sel2;

        try {
            sel1 = Integer.parseInt(selStart);
            sel2 = Integer.parseInt(selEnd);
        } catch (NumberFormatException ex) {
            sel1 = 0;
            sel2 = 0;
        }

        final WebInputHandler ih = (WebInputHandler) getInputHandler(clientID);
        final WebInputField field = (WebInputField) ih.getTarget();
        field.setContent(text);
        field.setSelStart(sel1);
        field.setSelEnd(sel2);

        return ih;
    }

    /** {@inheritDoc} */
    @Override
    public WritableFrameContainer getContainer() {
        return parent;
    }

}

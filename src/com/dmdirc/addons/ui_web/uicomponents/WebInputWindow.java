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

import com.dmdirc.WritableFrameContainer;
import com.dmdirc.addons.ui_web.Client;
import com.dmdirc.addons.ui_web.WebInterfaceUI;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.interfaces.ui.InputWindow;
import com.dmdirc.ui.input.InputHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * A Web-UI specific input window.
 */
public class WebInputWindow extends WebWindow implements InputWindow {

    private final WritableFrameContainer container;
    private final CommandParser commandparser;
    private final WebInputHandler inputHandler;
    private final Map<Client, WebInputHandler> inputHandlers = new HashMap<>();
    private final WebInterfaceUI controller;

    public WebInputWindow(final WebInterfaceUI controller,
            final WritableFrameContainer parent, final String id) {
        super(controller, parent, id);
        this.container = parent;
        this.commandparser = parent.getCommandParser();
        this.controller = controller;
        this.inputHandler = new WebInputHandler(controller.getPluginManager(),
                new WebInputField(), controller.getCommandController(),
                commandparser, getContainer(), controller.getEventBus());
    }

    @Override
    public WritableFrameContainer getContainer() {
        return container;
    }

    @Override
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public WebInputHandler getInputHandler(final Client client) {
        if (!inputHandlers.containsKey(client)) {
            final WebInputHandler ih = new WebInputHandler(
                    controller.getPluginManager(), new WebInputField(client),
                    controller.getCommandController(),
                    commandparser, getContainer(), controller.getEventBus());
            ih.setTabCompleter(inputHandler.getTabCompleter());
            inputHandlers.put(client, ih);
        }

        return inputHandlers.get(client);
    }

    public WebInputHandler getInputHandler(final Client client,
            final String text, final String selStart, final String selEnd) {
        int sel1;
        int sel2;

        try {
            sel1 = Integer.parseInt(selStart);
            sel2 = Integer.parseInt(selEnd);
        } catch (NumberFormatException ex) {
            sel1 = 0;
            sel2 = 0;
        }

        final WebInputHandler ih = getInputHandler(client);
        final WebInputField field = (WebInputField) ih.getTarget();
        field.setContent(text);
        field.setSelStart(sel1);
        field.setSelEnd(sel2);

        return ih;
    }

}

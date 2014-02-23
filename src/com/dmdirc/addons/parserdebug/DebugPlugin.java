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

package com.dmdirc.addons.parserdebug;

import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.util.URLBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This causes parser debugging to be spammed to the console.
 */
public final class DebugPlugin extends BaseCommandPlugin implements
        DebugInfoListener, ActionListener {

    /** Map of parsers registered. */
    protected final Map<Parser, DebugWindow> registeredParsers = new HashMap<>();
    /** The action controller to use. */
    private final ActionController actionController;

    /**
     * Creates a new instance of this plugin.
     *
     * @param actionController  The action controller to register listeners with
     * @param commandController Command controller to register commands
     * @param windowManager     Window Manager
     * @param urlBuilder        The URL builder to use when finding icons.
     */
    public DebugPlugin(
            final ActionController actionController,
            final CommandController commandController,
            final WindowManager windowManager,
            final URLBuilder urlBuilder) {
        super(commandController);

        this.actionController = actionController;

        registerCommand(new ParserDebugCommand(commandController, this, windowManager, urlBuilder),
                ParserDebugCommand.INFO);
    }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        actionController.registerListener(this, CoreActionType.SERVER_DISCONNECTED);
        super.onLoad();
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        actionController.unregisterListener(this);

        final ArrayList<DebugWindow> windowList = new ArrayList<>();
        for (Parser parser : registeredParsers.keySet()) {
            try {
                parser.getCallbackManager().delCallback(DebugInfoListener.class, this);
                final DebugWindow window = registeredParsers.get(parser);
                windowList.add(window);
            } catch (Exception e) {
            }
        }
        for (DebugWindow window : windowList) {
            window.close();
        }
        registeredParsers.clear();
        super.onUnload();
    }

    /** {@inheritDoc} */
    @Override
    public void onDebugInfo(final Parser parser, final Date date, final int level, final String data) {
        final DebugWindow window = registeredParsers.get(parser);
        if (window != null) {
            window.addLine(String.format("[%d] %s%n", level, data), true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.SERVER_DISCONNECTED) {
            final Connection connection = (Connection) arguments[0];
            final Parser parser = connection.getParser();
            if (registeredParsers.containsKey(parser)) {
                try {
                    final DebugWindow window = registeredParsers.get(parser);
                    registeredParsers.remove(parser);
                    window.unsetParser();
                    parser.getCallbackManager().delCallback(DebugInfoListener.class, this);
                    window.addLine("======================", true);
                    window.addLine("No Longer Monitoring: " + parser + " (Server Disconnected)",
                            true);
                    window.addLine("======================", true);
                } catch (Exception e) {
                }
            }
        }
    }

}
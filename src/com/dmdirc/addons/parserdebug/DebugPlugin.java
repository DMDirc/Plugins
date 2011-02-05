/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Server;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.plugins.Plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This causes parser debugging to be spammed to the console.
 *
 * @author Shane 'Dataforce' McCormack
 */
public final class DebugPlugin extends Plugin implements DebugInfoListener, ActionListener {

    /** The ParserDebugCommand we created. */
    private ParserDebugCommand command = null;

    /** Map of parsers registered. */
    protected final Map<Parser, DebugWindow> registeredParsers
            = new HashMap<Parser, DebugWindow>();

    /**
     * Creates a new instance of the Debug Plugin.
     */
    public DebugPlugin() { super(); }

    /** {@inheritDoc} */
    @Override
    public void onLoad() {
        ActionManager.getActionManager().unregisterListener(this,
                CoreActionType.SERVER_DISCONNECTED);
        command = new ParserDebugCommand(this);
        CommandManager.registerCommand(command);
    }

    /** {@inheritDoc} */
    @Override
    public void onUnload() {
        CommandManager.unregisterCommand(command);
        final ArrayList<DebugWindow> windowList = new ArrayList<DebugWindow>();
        for (Parser parser : registeredParsers.keySet()) {
            try {
                parser.getCallbackManager().delCallback(DebugInfoListener.class, this);
                DebugWindow window = registeredParsers.get(parser);
                windowList.add(window);
            } catch (Exception e) { }
        }
        for (DebugWindow window : windowList) {
            window.close();
        }
        registeredParsers.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void onDebugInfo(final Parser parser, final Date date, final int level, final String data) {
        DebugWindow window = registeredParsers.get(parser);
        if (window != null) {
            window.addLine(String.format("[%d] %s%n", level, data), true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        if (type == CoreActionType.SERVER_DISCONNECTED) {
            final Server thisServer = (Server) arguments[0];
            final Parser parser = thisServer.getParser();
            if (registeredParsers.containsKey(parser)) {
                try {
                    DebugWindow window = registeredParsers.get(parser);
                    registeredParsers.remove(parser);
                    window.unsetParser();
                    parser.getCallbackManager().delCallback(DebugInfoListener.class, this);
                    window.addLine("======================", true);
                    window.addLine("No Longer Monitoring: "+parser+" (Server Disconnected)", true);
                    window.addLine("======================", true);
                } catch (Exception e) { }
            }
        }
    }
}


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

import com.dmdirc.Server;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.parser.common.CallbackNotFoundException;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DebugInfoListener;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.util.URLBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class ParserDebugManager implements ActionListener, DebugInfoListener {

    /** Map of parsers registered. */
    protected final Map<Parser, DebugWindow> registeredParsers;
    /** Action controller. */
    private final ActionController actionController;
    /** URL Builder. */
    private final URLBuilder urlBuilder;
    /** Window manager. */
    private final WindowManager windowManager;

    @Inject
    public ParserDebugManager(final ActionController actionController,
            final URLBuilder urlBuilder,
            final WindowManager windowManager) {
        this.actionController = actionController;
        this.urlBuilder = urlBuilder;
        this.windowManager = windowManager;
        registeredParsers = new HashMap<>();
    }

    /**
     * Adds action listener.
     */
    public void addActionListener() {
        actionController.registerListener(this, CoreActionType.SERVER_DISCONNECTED);
    }

    /**
     * Remove action listener.
     */
    public void removeActionListener() {
        actionController.unregisterListener(this);
    }

    /**
     * Are we listener on the specified parser?
     *
     * @param parser Parser to check
     *
     * @return true if we're listening
     */
    public boolean containsParser(final Parser parser) {
        return registeredParsers.containsKey(parser);
    }

    /**
     * Adds a parser to this manager, adding any required call backs.
     *
     * @param parser Parser to add
     * @param server The server associated with the parser
     *
     * @return Whether we added the parser without error
     */
    public boolean addParser(final Parser parser, final Server server) {
        try {
            parser.getCallbackManager().addCallback(DebugInfoListener.class, this);
            final DebugWindow window = new DebugWindow(this, "Parser Debug", parser,
                    server, urlBuilder);
            windowManager.addWindow(server, window);
            registeredParsers.put(parser, window);
            window.addLine("======================", true);
            window.addLine("Started Monitoring: " + parser, true);
            window.addLine("======================", true);
            return true;
        } catch (CallbackNotFoundException ex) {
            return false;
        }
    }

    /**
     * Removes the parser from this manager, removing any call backs as required.
     *
     * @param parser Parser to add
     * @param close  Close debug window?
     *
     * @return Whether removed the parser without error
     */
    public boolean removeParser(final Parser parser, final boolean close) {
        try {
            parser.getCallbackManager().delCallback(DebugInfoListener.class, this);
            final DebugWindow window = registeredParsers.get(parser);
            window.addLine("======================", true);
            window.addLine("No Longer Monitoring: " + parser + " (User Requested)", true);
            window.addLine("======================", true);
            if (close) {
                window.close();
            }
            registeredParsers.remove(parser);
            return true;
        } catch (CallbackNotFoundException ex) {
            return false;
        }
    }

    /**
     * Removes all parser listeners and closes.
     *
     * @param close Close debug windows?
     */
    public void removeAllParserListeners(final boolean close) {
        for (Parser parser : registeredParsers.keySet()) {
            removeParser(parser, close);
        }
    }

    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (type == CoreActionType.SERVER_DISCONNECTED) {
            final Parser parser = ((Connection) arguments[0]).getParser();
            if (registeredParsers.containsKey(parser)) {
                removeParser(parser, false);
            }
        }
    }

    @Override
    public void onDebugInfo(final Parser parser, final Date date, final int level, final String data) {
        final DebugWindow window = registeredParsers.get(parser);
        if (window != null) {
            window.addLine(String.format("[%d] %s%n", level, data), true);
        }
    }

}

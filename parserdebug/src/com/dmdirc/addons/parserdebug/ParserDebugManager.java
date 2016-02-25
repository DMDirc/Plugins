/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.events.CommandOutputEvent;
import com.dmdirc.events.ServerDisconnectedEvent;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.events.DebugInfoEvent;
import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.BackBufferFactory;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

public class ParserDebugManager {

    /** Event bus to subscribe to events on. */
    private final DMDircMBassador eventBus;
    /** Map of parsers registered. */
    protected final Map<Parser, DebugWindow> registeredParsers;
    /** Window manager. */
    private final WindowManager windowManager;
    private final BackBufferFactory backBufferFactory;

    @Inject
    public ParserDebugManager(
            final WindowManager windowManager,
            final DMDircMBassador eventBus,
            final BackBufferFactory backBufferFactory) {
        this.windowManager = windowManager;
        this.eventBus = eventBus;
        this.backBufferFactory = backBufferFactory;
        registeredParsers = new HashMap<>();
    }

    /**
     * Adds action listener.
     */
    public void addActionListener() {
        eventBus.subscribe(this);
    }

    /**
     * Remove action listener.
     */
    public void removeActionListener() {
        eventBus.unsubscribe(this);
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
     * @param connection The connection associated with the parser
     */
    public void addParser(final Parser parser, final Connection connection) {
        parser.getCallbackManager().subscribe(this);
        final DebugWindow window = new DebugWindow(this, "Parser Debug", parser,
                connection, eventBus, backBufferFactory);
        windowManager.addWindow(connection.getWindowModel(), window);
        registeredParsers.put(parser, window);
        window.getEventBus().publishAsync(new CommandOutputEvent(window,
                "======================\n" +
                "Started Monitoring: " + parser + '\n' +
                "======================"));
    }

    /**
     * Removes the parser from this manager, removing any call backs as required.
     *
     * @param parser Parser to add
     * @param close  Close debug window?
     */
    public void removeParser(final Parser parser, final boolean close) {
        parser.getCallbackManager().unsubscribe(this);
        final DebugWindow window = registeredParsers.get(parser);
        window.getEventBus().publishAsync(new CommandOutputEvent(window,
                "======================\n" +
                "No Longer Monitoring: " + parser + " (User Requested)\n" +
                "======================"));
        if (close) {
            window.close();
        }
        registeredParsers.remove(parser);
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

    @Handler
    public void handleServerDisconnected(final ServerDisconnectedEvent event) {
        final Parser parser = event.getConnection().getParser().get();
        if (registeredParsers.containsKey(parser)) {
            removeParser(parser, false);
        }
    }

    @Handler
    public void onDebugInfo(final DebugInfoEvent event) {
        final DebugWindow window = registeredParsers.get(event.getParser());
        if (window != null) {
            window.getEventBus().publishAsync(new CommandOutputEvent(window,
                    String.format("[%d] %s%n", event.getLevel(), event.getData())));
        }
    }

}

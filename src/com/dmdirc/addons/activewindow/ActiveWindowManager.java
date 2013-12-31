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

package com.dmdirc.addons.activewindow;

import com.dmdirc.messages.MessageSinkManager;

import javax.inject.Inject;

/**
 * Manages the active window sink.
 */
public class ActiveWindowManager {

    /** The message sink to register and unregister. */
    private final ActiveWindowMessageSink sink;

    /** The manager to add and remove the sink from. */
    private final MessageSinkManager sinkManager;

    /**
     * Creates a new instance of {@link ActiveWindowManager}.
     *
     * @param sinkManager The manager to add and remove sinks from.
     * @param sink The sink to be added and removed.
     */
    @Inject
    public ActiveWindowManager(
            final MessageSinkManager sinkManager,
            final ActiveWindowMessageSink sink) {
        this.sink = sink;
        this.sinkManager = sinkManager;
    }

    /**
     * Registers the sink with the manager.
     */
    public void register() {
        sinkManager.addSink(sink);
    }

    /**
     * Unregisters the sink from the manager.
     */
    public void unregister() {
        sinkManager.removeSink(sink);
    }

}

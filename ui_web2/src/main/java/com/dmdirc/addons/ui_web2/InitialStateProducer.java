/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_web2;

import com.dmdirc.addons.ui_web2.serialisers.BackBufferSimpleSerializer;
import com.dmdirc.addons.ui_web2.serialisers.WindowModelSerialiser;
import com.dmdirc.interfaces.WindowModel;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.BackBuffer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

/**
 * Handles compiling the initial burst of state that will be sent to new web clients.
 */
public class InitialStateProducer {

    private final Gson serialiser;
    private final WindowManager windowManager;

    @Inject
    public InitialStateProducer(
            final WindowManager windowManager,
            final WindowModelSerialiser windowSerialiser) {
        serialiser = new GsonBuilder()
                .registerTypeHierarchyAdapter(WindowModel.class, windowSerialiser)
                .registerTypeAdapter(BackBuffer.class, new BackBufferSimpleSerializer())
                .create();
        this.windowManager = windowManager;
    }

    public String getInitialState() {
        final InitialState state = new InitialState(windowManager.getRootWindows());
        return serialiser.toJson(state);
    }

    /** Class to serialize and pass to the client with initial state. */
    private static class InitialState {

        private final Collection<WindowModel> windows;

        private InitialState(final Collection<WindowModel> windows) {
            this.windows = windows;
        }

        public Collection<WindowModel> getWindows() {
            return Collections.unmodifiableCollection(windows);
        }

    }

}

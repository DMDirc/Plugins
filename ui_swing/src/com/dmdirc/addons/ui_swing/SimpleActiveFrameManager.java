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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingActiveWindowChangeRequestEvent;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.interfaces.ui.Window;

import java.util.Optional;

import javax.inject.Inject;

import net.engio.mbassy.listener.Handler;

/**
 * Simple active frame manager used only to keep track of the active frame.
 */
public class SimpleActiveFrameManager implements ActiveFrameManager {

    private Optional<Window> window;

    @Inject
    public SimpleActiveFrameManager() {
        window = Optional.empty();
    }

    @Handler
    public void setActiveFrame(final SwingActiveWindowChangeRequestEvent event) {
        this.window = event.getWindow();
    }

    @Override
    public TextFrame getActiveFrame() {
        return (TextFrame) window.orElse(null);
    }
}

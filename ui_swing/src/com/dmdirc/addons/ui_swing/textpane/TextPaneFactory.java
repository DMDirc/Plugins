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

package com.dmdirc.addons.ui_swing.textpane;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.util.URLBuilder;

import java.awt.datatransfer.Clipboard;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for {@link TextPane}s
 */
@Singleton
public class TextPaneFactory {

    private final String configDomain;
    private final URLBuilder urlBuilder;
    private final Clipboard clipboard;
    private final DMDircMBassador eventBus;

    @Inject
    public TextPaneFactory(@PluginDomain(SwingController.class) final String configDomain,
            final URLBuilder urlBuilder, final Clipboard clipboard, final DMDircMBassador eventBus) {
        this.configDomain = configDomain;
        this.urlBuilder = urlBuilder;
        this.clipboard = clipboard;
        this.eventBus = eventBus;
    }

    public TextPane getTextPane(final TextFrame frame) {
        return new TextPane(eventBus, configDomain, urlBuilder, clipboard, frame);
    }

}

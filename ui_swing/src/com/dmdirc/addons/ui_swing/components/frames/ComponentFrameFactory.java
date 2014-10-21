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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.parsers.CommandParser;

import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JComponent;

import static com.dmdirc.addons.ui_swing.components.frames.TextFrame.TextFrameDependencies;

/**
 * Factory for {@link ComponentFrame}s.
 */
@Singleton
public class ComponentFrameFactory {

    private final DMDircMBassador eventBus;
    private final TextFrameDependencies dependencies;

    @Inject
    public ComponentFrameFactory(
            final DMDircMBassador eventBus,
            final TextFrameDependencies dependencies) {
        this.eventBus = eventBus;
        this.dependencies = dependencies;
    }

    public ComponentFrame getComponentFrame(
            final FrameContainer owner,
            final CommandParser commandParser,
            final Iterable<Supplier<? extends JComponent>> componentSupplier) {
        final ComponentFrame frame = new ComponentFrame(dependencies, owner, commandParser,
                componentSupplier);
        eventBus.subscribe(frame);
        return frame;
    }

}

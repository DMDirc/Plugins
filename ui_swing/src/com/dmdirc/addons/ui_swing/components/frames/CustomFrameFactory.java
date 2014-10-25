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

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.ui.core.components.WindowComponent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.dmdirc.addons.ui_swing.components.frames.TextFrame.TextFrameDependencies;

/**
 * Factory for {@link CustomFrame}s.
 */
@Singleton
public class CustomFrameFactory implements SwingWindowFactory.WindowProvider {

    private final GlobalCommandParser commandParser;
    private final Provider<TextFrameDependencies> dependencies;

    @Inject
    public CustomFrameFactory(
            final GlobalCommandParser commandParser,
            final Provider<TextFrameDependencies> dependencies) {
        this.commandParser = commandParser;
        this.dependencies = dependencies;
    }

    @Override
    public TextFrame getWindow(final FrameContainer container) {
        return new CustomFrame(dependencies.get(), commandParser, container);
    }

    @Override
    public Set<String> getComponents() {
        return new HashSet<>(Collections.singletonList(WindowComponent.TEXTAREA.getIdentifier()));
    }

}

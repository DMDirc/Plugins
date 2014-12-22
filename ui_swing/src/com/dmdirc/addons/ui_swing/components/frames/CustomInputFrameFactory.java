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
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.ui.core.components.WindowComponent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.dmdirc.addons.ui_swing.components.frames.TextFrame.TextFrameDependencies;

/**
 * Factory for {@link CustomInputFrame}s.
 */
@Singleton
public class CustomInputFrameFactory implements SwingWindowFactory.WindowProvider {

    private final Provider<TextFrameDependencies> dependencies;
    private final Provider<SwingInputField> inputFieldProvider;
    private final DMDircMBassador eventBus;
    private final InputTextFramePasteActionFactory inputTextFramePasteActionFactory;

    @Inject
    public CustomInputFrameFactory(
            final DMDircMBassador eventBus,
            final InputTextFramePasteActionFactory inputTextFramePasteActionFactory,
            final Provider<TextFrameDependencies> dependencies,
            final Provider<SwingInputField> inputFieldProvider) {
        this.eventBus = eventBus;
        this.dependencies = dependencies;
        this.inputFieldProvider = inputFieldProvider;
        this.inputTextFramePasteActionFactory = inputTextFramePasteActionFactory;
    }

    @Override
    public TextFrame getWindow(final FrameContainer container) {
        final CustomInputFrame frame = new CustomInputFrame(dependencies.get(), inputFieldProvider,
                inputTextFramePasteActionFactory, container);
        eventBus.subscribe(frame);
        return frame;
    }

    @Override
    public Set<String> getComponents() {
        return new HashSet<>(Arrays.asList(
                WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier()));
    }

}

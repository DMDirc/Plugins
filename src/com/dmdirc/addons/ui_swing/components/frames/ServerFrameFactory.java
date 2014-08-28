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
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.ui.core.components.WindowComponent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.dmdirc.addons.ui_swing.components.frames.TextFrame.TextFrameDependencies;

/**
 * Factory for {@link ServerFrame}s
 */
@Singleton
public class ServerFrameFactory implements SwingWindowFactory.WindowProvider {

    private final Provider<TextFrameDependencies> dependencies;
    private final Provider<SwingInputField> inputFieldProvider;
    private final Provider<KeyedDialogProvider<Connection, ServerSettingsDialog>> dialogProvider;
    private final DMDircMBassador eventBus;

    @Inject
    public ServerFrameFactory(
            final DMDircMBassador eventBus,
            final Provider<TextFrameDependencies> dependencies,
            final Provider<SwingInputField> inputFieldProvider,
            final Provider<KeyedDialogProvider<Connection, ServerSettingsDialog>> dialogProvider) {
        this.eventBus = eventBus;
        this.dependencies = dependencies;
        this.inputFieldProvider = inputFieldProvider;
        this.dialogProvider = dialogProvider;
    }

    @Override
    public TextFrame getWindow(final FrameContainer container) {
        final ServerFrame frame =  new ServerFrame(dependencies.get(), inputFieldProvider,
                dialogProvider.get(), container.getConnection());
        eventBus.subscribe(frame);
        return frame;
    }

    @Override
    public Set<String> getComponents() {
        return new HashSet<>(Arrays.asList(
                WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier(),
                WindowComponent.CERTIFICATE_VIEWER.getIdentifier()));
    }

}

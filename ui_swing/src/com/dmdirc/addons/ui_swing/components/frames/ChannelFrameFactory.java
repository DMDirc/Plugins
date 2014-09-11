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

import com.dmdirc.Channel;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.components.TopicBarFactory;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.core.components.WindowComponent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.dmdirc.addons.ui_swing.components.frames.TextFrame.TextFrameDependencies;

/**
 * Factory for {@link ChannelFrame}s.
 */
@Singleton
public class ChannelFrameFactory implements SwingWindowFactory.WindowProvider {

    private final String domain;
    private final Provider<TextFrameDependencies> dependencies;
    private final Provider<SwingInputField> inputFieldProvider;
    private final IdentityFactory identityFactory;
    private final Provider<KeyedDialogProvider<Channel, ChannelSettingsDialog>> dialogProvider;
    private final TopicBarFactory topicBarFactory;
    private final DMDircMBassador eventBus;

    @Inject
    public ChannelFrameFactory(
            final DMDircMBassador eventBus,
            @PluginDomain(SwingController.class) final String domain,
            final Provider<TextFrameDependencies> dependencies,
            final Provider<SwingInputField> inputFieldProvider,
            final IdentityFactory identityFactory,
            final Provider<KeyedDialogProvider<Channel, ChannelSettingsDialog>> dialogProvider,
            final TopicBarFactory topicBarFactory) {
        this.eventBus = eventBus;
        this.domain = domain;
        this.dependencies = dependencies;
        this.inputFieldProvider = inputFieldProvider;
        this.identityFactory = identityFactory;
        this.dialogProvider = dialogProvider;
        this.topicBarFactory = topicBarFactory;
    }

    @Override
    public TextFrame getWindow(final FrameContainer container) {
        final ChannelFrame frame = new ChannelFrame(domain, dependencies.get(), inputFieldProvider,
                identityFactory, dialogProvider.get(), topicBarFactory, (Channel) container);
        eventBus.subscribe(frame);
        return frame;
    }

    @Override
    public Set<String> getComponents() {
        return new HashSet<>(Arrays.asList(
                WindowComponent.TEXTAREA.getIdentifier(),
                WindowComponent.INPUTFIELD.getIdentifier(),
                WindowComponent.TOPICBAR.getIdentifier(),
                WindowComponent.USERLIST.getIdentifier()));
    }

}

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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.Channel;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Window;
import java.awt.datatransfer.Clipboard;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import static com.dmdirc.ClientModule.GlobalConfig;

/**
 * Factory for {@link TopicBar}s.
 */
@Singleton
public class TopicBarFactory {

    private final Provider<Window> parentWindow;
    private final AggregateConfigProvider globalConfig;
    private final String domain;
    private final ColourManagerFactory colourManagerFactory;
    private final PluginManager pluginManager;
    private final Clipboard clipboard;
    private final CommandController commandController;
    private final DMDircMBassador eventBus;

    @Inject
    public TopicBarFactory(
            @MainWindow final Provider<Window> parentWindow,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(SwingController.class) final String domain,
            final ColourManagerFactory colourManagerFactory,
            final PluginManager pluginManager,
            final Clipboard clipboard,
            final CommandController commandController,
            final DMDircMBassador eventBus) {
        this.parentWindow = parentWindow;
        this.globalConfig = globalConfig;
        this.domain = domain;
        this.colourManagerFactory = colourManagerFactory;
        this.pluginManager = pluginManager;
        this.clipboard = clipboard;
        this.commandController = commandController;
        this.eventBus = eventBus;
    }

    public TopicBar getTopicBar(
            final Channel channel,
            final ChannelFrame window,
            final IconManager iconManager) {
        return new TopicBar(parentWindow.get(), globalConfig, domain,
                colourManagerFactory.getColourManager(channel.getConfigManager()),
                pluginManager, clipboard, commandController, channel, window, iconManager,
                eventBus);
    }

}

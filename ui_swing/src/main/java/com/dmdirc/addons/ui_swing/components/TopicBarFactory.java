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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.ServiceManager;
import com.dmdirc.ui.input.TabCompleterUtils;
import com.dmdirc.ui.messages.ColourManagerFactory;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Factory for {@link TopicBar}s.
 */
@Singleton
public class TopicBarFactory {

    private final Provider<Window> parentWindow;
    private final AggregateConfigProvider globalConfig;
    private final String domain;
    private final ColourManagerFactory colourManagerFactory;
    private final ServiceManager serviceManager;
    private final Clipboard clipboard;
    private final CommandController commandController;
    private final TabCompleterUtils tabCompleterUtils;
    private final IconManager iconManager;

    @Inject
    public TopicBarFactory(
            @MainWindow final Provider<Window> parentWindow,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @PluginDomain(SwingController.class) final String domain,
            final ColourManagerFactory colourManagerFactory,
            final ServiceManager serviceManager,
            final Clipboard clipboard,
            final CommandController commandController,
            final TabCompleterUtils tabCompleterUtils,
            final IconManager iconManager) {
        this.parentWindow = parentWindow;
        this.globalConfig = globalConfig;
        this.domain = domain;
        this.colourManagerFactory = colourManagerFactory;
        this.serviceManager = serviceManager;
        this.clipboard = clipboard;
        this.commandController = commandController;
        this.tabCompleterUtils = tabCompleterUtils;
        this.iconManager = iconManager;
    }

    public TopicBar getTopicBar(
            final GroupChat channel,
            final ChannelFrame window) {
        return new TopicBar(parentWindow.get(), globalConfig, domain,
                colourManagerFactory.getColourManager(channel.getWindowModel().getConfigManager()),
                serviceManager, clipboard, commandController, channel, window, iconManager,
                tabCompleterUtils);
    }

}

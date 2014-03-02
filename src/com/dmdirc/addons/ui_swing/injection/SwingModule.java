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

package com.dmdirc.addons.ui_swing.injection;

import com.dmdirc.ClientModule;
import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.QuitWorker;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingManager;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.commands.ChannelSettings;
import com.dmdirc.addons.ui_swing.commands.Input;
import com.dmdirc.addons.ui_swing.commands.PopInCommand;
import com.dmdirc.addons.ui_swing.commands.PopOutCommand;
import com.dmdirc.addons.ui_swing.commands.ServerSettings;
import com.dmdirc.addons.ui_swing.components.addonpanel.PluginPanel;
import com.dmdirc.addons.ui_swing.components.addonpanel.ThemePanel;
import com.dmdirc.addons.ui_swing.components.statusbar.InviteLabel;
import com.dmdirc.addons.ui_swing.components.statusbar.MessageLabel;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.components.statusbar.UpdaterLabel;
import com.dmdirc.addons.ui_swing.dialogs.prefs.URLConfigPanel;
import com.dmdirc.addons.ui_swing.dialogs.prefs.UpdateConfigPanel;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.ui.core.util.URLHandler;
import com.dmdirc.util.URLBuilder;

import com.google.common.eventbus.EventBus;

import java.util.concurrent.Callable;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides Swing-specific dependencies.
 */
@Module(
        addsTo = ClientModule.class,
        includes = DialogModule.class,
        injects = {
            SwingManager.class,
            PopInCommand.class,
            PopOutCommand.class,
            Input.class,
            ServerSettings.class,
            ChannelSettings.class
        })
public class SwingModule {

    /** The controller to return to clients. */
    private final SwingController controller;
    /** The domain for plugin settings. */
    private final String domain;

    public SwingModule(final SwingController controller, final String domain) {
        this.controller = controller;
        this.domain = domain;
    }

    @Provides
    @PluginDomain(SwingController.class)
    public String getSettingsDomain() {
        return domain;
    }

    @Provides
    public SwingController getController() {
        return controller;
    }

    @Provides
    @Singleton
    public MainFrame getMainFrame(
            final Apple apple,
            final SwingController swingController,
            final SwingWindowFactory windowFactory,
            final LifecycleController lifecycleController,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final Provider<QuitWorker> quitWorker,
            final URLBuilder urlBuilder,
            final WindowManager windowManager,
            final EventBus eventBus) {
        return UIUtilities.invokeAndWait(new Callable<MainFrame>() {

            @Override
            public MainFrame call() {
                return new MainFrame(
                        apple,
                        swingController,
                        windowFactory,
                        lifecycleController,
                        globalConfig,
                        quitWorker,
                        new IconManager(globalConfig, urlBuilder),
                        windowManager,
                        eventBus);
            }
        });
    }

    @Provides
    @Singleton
    public SwingStatusBar getSwingStatusBar(
            final SwingController swingController,
            final MainFrame mainFrame,
            final InviteLabel inviteLabel,
            final UpdaterLabel updaterLabel,
            final MessageLabel messageLabel) {
        return UIUtilities.invokeAndWait(new Callable<SwingStatusBar>() {
            @Override
            public SwingStatusBar call() {
                return new SwingStatusBar(swingController, mainFrame, inviteLabel, updaterLabel,
                        messageLabel);
            }
        });
    }

    @Provides
    @Singleton
    public URLHandler getURLHandler(
            final SwingController swingController,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final ServerManager serverManager,
            final StatusBarManager statusBarManager) {
        return new URLHandler(swingController, globalConfig, serverManager, statusBarManager);
    }

    @Provides
    public PreferencesDialogModel getPrefsDialogModel(
            final PluginPanel pluginPanel,
            final ThemePanel themePanel,
            final UpdateConfigPanel updatePanel,
            final URLConfigPanel urlPanel,
            @GlobalConfig final AggregateConfigProvider configManager,
            @UserConfig final ConfigProvider identity,
            final ActionManager actionManager,
            final PluginManager pluginManager) {
        return new PreferencesDialogModel(pluginPanel, themePanel, updatePanel, urlPanel,
                configManager, identity, actionManager, pluginManager);
    }

}

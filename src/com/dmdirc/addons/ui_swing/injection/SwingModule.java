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

    /**
     * Creates a new instance of {@link SwingModule}.
     *
     * @param controller The controller to return. This should be removed when SwingController is
     *                   separated from the plugin implementation.
     * @param domain     The domain for plugin settings.
     */
    public SwingModule(final SwingController controller, final String domain) {
        this.controller = controller;
        this.domain = domain;
    }

    /**
     * Provides the domain that the swing settings should be stored under.
     *
     * @return The settings domain for the swing plugin.
     */
    @Provides
    @PluginDomain(SwingController.class)
    public String getSettingsDomain() {
        return domain;
    }

    /**
     * Gets the swing controller to use.
     *
     * @return The swing controller.
     */
    @Provides
    public SwingController getController() {
        return controller;
    }

    /**
     * Gets the main DMDirc window.
     *
     * @param apple               Apple instance
     * @param swingController     The controller that will own the frame.
     * @param windowFactory       The window factory to use to create and listen for windows.
     * @param lifecycleController The controller to use to quit the application.
     * @param globalConfig        The config to read settings from.
     * @param quitWorker          The worker to use when quitting the application.
     * @param urlBuilder          The URL builder to use to find icons.
     * @param windowManager       The core window manager to use to find windows.
     *
     * @return The main window.
     */
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
            final WindowManager windowManager) {
        return UIUtilities.invokeAndWait(new Callable<MainFrame>() {
            /** {@inheritDoc} */
            @Override
            public MainFrame call() {
                return new MainFrame(
                        apple,
                        controller,
                        windowFactory,
                        lifecycleController,
                        globalConfig,
                        quitWorker,
                        new IconManager(globalConfig, urlBuilder),
                        windowManager);
            }
        });
    }

    /**
     * Provides an URL handler for use in the swing UI.
     *
     * @param swingController  The controller that will own the handler.
     * @param globalConfig     The global configuration to read settings from.
     * @param serverManager    The server manager to use to connect to servers.
     * @param statusBarManager The status bar manager to add messages to.
     *
     * @return The URL handler to use.
     */
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

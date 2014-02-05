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

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.dialogs.FeedbackDialog;
import com.dmdirc.addons.ui_swing.dialogs.NewServerDialog;
import com.dmdirc.addons.ui_swing.dialogs.about.AboutDialog;
import com.dmdirc.addons.ui_swing.dialogs.actionsmanager.ActionsManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.aliases.AliasManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialogFactory;
import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialogFactory;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingUpdaterDialog;
import com.dmdirc.interfaces.LifecycleController;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Facilitates injection of dialogs.
 */
@Module(library = true, complete = false)
public class DialogModule {

    /**
     * Qualifier that indicates a restart dialog is needed for updates to be applied.
     */
    @Qualifier
    public static @interface ForUpdates {}

    /**
     * Qualifier that indicates a restart dialog is needed for settings to be applied.
     */
    @Qualifier
    public static @interface ForSettings {}

    @Provides
    @Singleton
    public DialogProvider<NewServerDialog> getNewServerDialogProvider(
            final Provider<NewServerDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    public DialogProvider<ProfileManagerDialog> getProfileManagerDialogProvider(
            final Provider<ProfileManagerDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    public DialogProvider<ActionsManagerDialog> getActionsManagerDialogProvider(
            final Provider<ActionsManagerDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    public DialogProvider<AliasManagerDialog> getAliasManagerDialogProvider(
            final Provider<AliasManagerDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    public DialogProvider<FeedbackDialog> getFeedbackDialogProvider(
            final Provider<FeedbackDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    public DialogProvider<AboutDialog> getAboutDialogProvider(
            final Provider<AboutDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    public KeyedDialogProvider<Server, ServerSettingsDialog> getServerSettingsDialogProvider(
            final ServerSettingsDialogFactory factory) {
        return new KeyedDialogProvider<Server, ServerSettingsDialog>() {
            @Override
            protected ServerSettingsDialog getInstance(final Server key) {
                return factory.getServerSettingsDialog(key);
            }
        };
    }

    @Provides
    @Singleton
    public KeyedDialogProvider<Channel, ChannelSettingsDialog> getChannelSettingsDialogProvider(
            final ChannelSettingsDialogFactory factory) {
        return new KeyedDialogProvider<Channel, ChannelSettingsDialog>() {
            @Override
            protected ChannelSettingsDialog getInstance(final Channel key) {
                return factory.getChannelSettingsDialog(key);
            }
        };
    }

    @Provides
    @Singleton
    public DialogProvider<SwingPreferencesDialog> getSwingPreferencesDialogProvider(
            final Provider<SwingPreferencesDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    public DialogProvider<SwingUpdaterDialog> getSwingUpdaterDialogProvider(
            final Provider<SwingUpdaterDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    @ForUpdates
    public DialogProvider<SwingRestartDialog> getSwingRestartDialogProviderForUpdates(
            @ForUpdates final Provider<SwingRestartDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @Singleton
    @ForSettings
    public DialogProvider<SwingRestartDialog> getSwingRestartDialogProviderForSettings(
            @ForSettings final Provider<SwingRestartDialog> provider) {
        return new DialogProvider<>(provider);
    }

    @Provides
    @ForUpdates
    public SwingRestartDialog getRestartDialogForUpdates(
            final MainFrame mainFrame,
            final LifecycleController lifecycleController) {
        return new SwingRestartDialog(mainFrame, lifecycleController, "finish updating");
    }

    @Provides
    @ForSettings
    public SwingRestartDialog getRestartDialogForSettings(
            final MainFrame mainFrame,
            final LifecycleController lifecycleController) {
        return new SwingRestartDialog(mainFrame, lifecycleController, "apply settings");
    }

}

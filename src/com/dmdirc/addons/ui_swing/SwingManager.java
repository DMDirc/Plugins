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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.Channel;
import com.dmdirc.Server;
import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
import com.dmdirc.addons.ui_swing.components.statusbar.FeedbackNag;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.dialogs.DialogKeyListener;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.addons.ui_swing.dialogs.url.URLDialogFactory;
import com.dmdirc.addons.ui_swing.framemanager.ctrltab.CtrlTabWindowManager;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.addons.ui_swing.wizard.firstrun.FirstRunWizardExecutor;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.StatusBarManager;

import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Manages swing components and dependencies.
 */
@Singleton
public class SwingManager {

    /** The event queue to use. */
    private final DMDircEventQueue eventQueue;

    /** The window factory in use. */
    private final SwingWindowFactory windowFactory;

    /** The status bar manager to register our status bar with. */
    private final StatusBarManager statusBarManager;

    /** The status bar in use. */
    private final SwingStatusBar statusBar;

    /** The window manager to listen on for events. */
    private final WindowManager windowManager;

    /** The main frame of the Swing UI. */
    private final MainFrame mainFrame;

    /** The key listener that supports dialogs. */
    private final DialogKeyListener dialogKeyListener;

    /** Provider of first run executors. */
    private final Provider<FirstRunWizardExecutor> firstRunExecutor;

    /** Provider of prefs dialogs. */
    private final DialogProvider<SwingPreferencesDialog> prefsDialogProvider;
    /** Provider of server settings dialogs. */
    private final KeyedDialogProvider<Server, ServerSettingsDialog> serverSettingsDialogProvider;
    /** Provider of channel settings dialogs. */
    private final KeyedDialogProvider<Channel, ChannelSettingsDialog> channelSettingsDialogProvider;

    /** Provider of feedback nags. */
    private final Provider<FeedbackNag> feedbackNagProvider;

    /** Factory to use to create URL dialogs. */
    private final URLDialogFactory urlDialogFactory;

    /**
     * Creates a new instance of {@link SwingManager}.
     *
     * @param eventQueue The event queue to use.
     * @param windowFactory The window factory in use.
     * @param windowManager The window manager to listen on for events.
     * @param statusBarManager The core status bar manager to register our status bar with.
     * @param mainFrame The main frame of the Swing UI.
     * @param menuBar The menu bar to use for the main frame.
     * @param statusBar The status bar to use in the main frame.
     * @param ctrlTabManager The window manager that handles ctrl+tab behaviour.
     * @param dialogKeyListener The key listener that supports dialogs.
     * @param firstRunExecutor A provider of first run executors.
     * @param prefsDialogProvider Provider of prefs dialogs.
     * @param serverSettingsDialogProvider Provider of server settings dialogs.
     * @param channelSettingsDialogProvider Provider of channel settings dialogs.
     * @param feedbackNagProvider Provider of feedback nags.
     * @param urlDialogFactory Factory to use to create URL dialogs.
     */
    @Inject
    public SwingManager(
            final DMDircEventQueue eventQueue,
            final SwingWindowFactory windowFactory,
            final WindowManager windowManager,
            final StatusBarManager statusBarManager,
            final MainFrame mainFrame,
            final MenuBar menuBar,
            final SwingStatusBar statusBar,
            final CtrlTabWindowManager ctrlTabManager,
            final DialogKeyListener dialogKeyListener,
            final Provider<FirstRunWizardExecutor> firstRunExecutor,
            final DialogProvider<SwingPreferencesDialog> prefsDialogProvider,
            final KeyedDialogProvider<Server, ServerSettingsDialog> serverSettingsDialogProvider,
            final KeyedDialogProvider<Channel, ChannelSettingsDialog> channelSettingsDialogProvider,
            final Provider<FeedbackNag> feedbackNagProvider,
            final URLDialogFactory urlDialogFactory) {
        this.eventQueue = eventQueue;
        this.windowFactory = windowFactory;
        this.windowManager = windowManager;
        this.statusBar = statusBar;
        this.statusBarManager = statusBarManager;
        this.dialogKeyListener = dialogKeyListener;
        this.firstRunExecutor = firstRunExecutor;
        this.prefsDialogProvider = prefsDialogProvider;
        this.serverSettingsDialogProvider = serverSettingsDialogProvider;
        this.channelSettingsDialogProvider = channelSettingsDialogProvider;
        this.feedbackNagProvider = feedbackNagProvider;
        this.urlDialogFactory = urlDialogFactory;

        this.mainFrame = mainFrame;
        this.mainFrame.setMenuBar(menuBar);
        this.mainFrame.setWindowManager(ctrlTabManager);
        this.mainFrame.setStatusBar(statusBar);
        this.mainFrame.initComponents();
    }

    /**
     * Handles loading of the UI.
     */
    public void load() {
        installEventQueue();
        installKeyListener();

        windowManager.addListenerAndSync(windowFactory);
        statusBarManager.registerStatusBar(statusBar);
    }

    /**
     * Handles unloading of the UI.
     */
    public void unload() {
        uninstallEventQueue();
        uninstallKeyListener();

        windowManager.removeListener(windowFactory);
        windowFactory.dispose();
        mainFrame.dispose();
        statusBarManager.unregisterStatusBar(statusBar);
    }

    /**
     * Gets a first run wizard executor to use.
     *
     * @return A first run wizard executor.
     */
    public FirstRunWizardExecutor getFirstRunExecutor() {
        return firstRunExecutor.get();
    }

    @Deprecated
    public DialogProvider<SwingPreferencesDialog> getPrefsDialogProvider() {
        return prefsDialogProvider;
    }

    @Deprecated
    public KeyedDialogProvider<Server, ServerSettingsDialog> getServerSettingsDialogProvider() {
        return serverSettingsDialogProvider;
    }

    @Deprecated
    public KeyedDialogProvider<Channel, ChannelSettingsDialog> getChannelSettingsDialogProvider() {
        return channelSettingsDialogProvider;
    }

    @Deprecated
    public Provider<FeedbackNag> getFeedbackNagProvider() {
        return feedbackNagProvider;
    }

    @Deprecated
    public URLDialogFactory getUrlDialogFactory() {
        return urlDialogFactory;
    }

    /**
     * Retrieves the window factory to use.
     *
     * @return A swing window factory instance.
     * @deprecated Should be injected.
     */
    @Deprecated
    public SwingWindowFactory getWindowFactory() {
        return windowFactory;
    }

    /**
     * Retrieves the main frame.
     *
     * @return A main frame instance.
     * @deprecated Should be injected.
     */
    @Deprecated
    public MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Installs the DMDirc event queue.
     */
    private void installEventQueue() {
        UIUtilities.invokeAndWait(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);
            }
        });
    }

    /**
     * Removes the DMDirc event queue.
     */
    private void uninstallEventQueue() {
        eventQueue.pop();
    }

    /**
     * Installs the dialog key listener.
     */
    private void installKeyListener() {
        UIUtilities.invokeAndWait(new Runnable() {
            /** {@inheritDoc} */
            @Override
            public void run() {
                KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .addKeyEventDispatcher(dialogKeyListener);
            }
        });
    }

    /**
     * Removes the dialog key listener.
     */
    private void uninstallKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(dialogKeyListener);
    }

}

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

import com.dmdirc.addons.ui_swing.components.addonbrowser.DataLoaderWorkerFactory;
import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.dialogs.DialogKeyListener;
import com.dmdirc.addons.ui_swing.dialogs.DialogManager;
import com.dmdirc.addons.ui_swing.framemanager.ctrltab.CtrlTabWindowManager;
import com.dmdirc.addons.ui_swing.wizard.firstrun.FirstRunWizardExecutor;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.ui.core.util.URLHandler;
import com.dmdirc.updater.manager.CachingUpdateManager;

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

    /** The dialog manager to use. */
    private final DialogManager dialogManager;

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

    /** The URL handler to use. */
    private final URLHandler urlHandler;

    /** The key listener that supports dialogs. */
    private final DialogKeyListener dialogKeyListener;

    /** Factory used to create data loader workers. */
    private final DataLoaderWorkerFactory dataLoaderWorkerFactory;

    /** The update manager to use. */
    private final CachingUpdateManager cachingUpdateManager;

    /** Provider of first run executors. */
    private final Provider<FirstRunWizardExecutor> firstRunExecutor;

    /**
     * Creates a new instance of {@link SwingManager}.
     *
     * @param dialogManager Dialog manager to use
     * @param eventQueue The event queue to use.
     * @param windowFactory The window factory in use.
     * @param windowManager The window manager to listen on for events.
     * @param statusBarManager The core status bar manager to register our status bar with.
     * @param mainFrame The main frame of the Swing UI.
     * @param menuBar The menu bar to use for the main frame.
     * @param statusBar The status bar to use in the main frame.
     * @param ctrlTabManager The window manager that handles ctrl+tab behaviour.
     * @param urlHandler The URL handler to use.
     * @param dialogKeyListener The key listener that supports dialogs.
     * @param dataLoaderWorkerFactory Factory used to create data loader workers.
     * @param cachingUpdateManager Update manager to use.
     * @param firstRunExecutor A provider of first run executors.
     */
    @Inject
    public SwingManager(
            final DialogManager dialogManager,
            final DMDircEventQueue eventQueue,
            final SwingWindowFactory windowFactory,
            final WindowManager windowManager,
            final StatusBarManager statusBarManager,
            final MainFrame mainFrame,
            final MenuBar menuBar,
            final SwingStatusBar statusBar,
            final CtrlTabWindowManager ctrlTabManager,
            final URLHandler urlHandler,
            final DialogKeyListener dialogKeyListener,
            final DataLoaderWorkerFactory dataLoaderWorkerFactory,
            final CachingUpdateManager cachingUpdateManager,
            final Provider<FirstRunWizardExecutor> firstRunExecutor) {
        this.dialogManager = dialogManager;
        this.eventQueue = eventQueue;
        this.windowFactory = windowFactory;
        this.windowManager = windowManager;
        this.statusBar = statusBar;
        this.statusBarManager = statusBarManager;
        this.urlHandler = urlHandler;
        this.dialogKeyListener = dialogKeyListener;
        this.dataLoaderWorkerFactory = dataLoaderWorkerFactory;
        this.cachingUpdateManager = cachingUpdateManager;
        this.firstRunExecutor = firstRunExecutor;

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

    /**
     * Retrieves the dialog manager to use.
     *
     * @return The dialog manager to use
     *
     * @deprecated Should be injected
     */
    @Deprecated
    public DialogManager getDialogManager() {
        return dialogManager;
    }

    @Deprecated
    public DataLoaderWorkerFactory getDataLoaderWorkerFactory() {
        return dataLoaderWorkerFactory;
    }

    @Deprecated
    public CachingUpdateManager getCachingUpdateManager() {
        return cachingUpdateManager;
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
     * Gets the URL handler to use.
     *
     * @return The URL handler to use.
     * @deprecated Should be injected.
     */
    @Deprecated
    public URLHandler getUrlHandler() {
        return urlHandler;
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

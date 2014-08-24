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

import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
import com.dmdirc.addons.ui_swing.components.statusbar.FeedbackNag;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.dialogs.error.ErrorListDialog;
import com.dmdirc.addons.ui_swing.dialogs.url.URLDialogFactory;
import com.dmdirc.addons.ui_swing.framemanager.buttonbar.ButtonBarProvider;
import com.dmdirc.addons.ui_swing.framemanager.ctrltab.CtrlTabWindowManager;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManagerProvider;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.SwingEventBus;
import com.dmdirc.addons.ui_swing.wizard.SwingWindowManager;
import com.dmdirc.addons.ui_swing.wizard.firstrun.FirstRunWizardExecutor;
import com.dmdirc.events.FeedbackNagEvent;
import com.dmdirc.events.FirstRunEvent;
import com.dmdirc.events.UnknownURLEvent;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.ui.WindowManager;

import java.awt.Window;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.listener.Handler;

/**
 * Manages swing components and dependencies.
 */
@Singleton
public class SwingManager {

    /** The window factory in use. */
    private final Provider<SwingWindowFactory> windowFactory;
    private final Provider<MenuBar> menuBar;
    /** The status bar in use. */
    private final Provider<SwingStatusBar> statusBar;
    /** The window manager to listen on for events. */
    private final WindowManager windowManager;
    private final CtrlTabWindowManager ctrlTabManager;
    /** Provider of first run executors. */
    private final Provider<FirstRunWizardExecutor> firstRunExecutor;
    /** Provider of feedback nags. */
    private final Provider<FeedbackNag> feedbackNagProvider;
    /** Factory to use to create URL dialogs. */
    private final URLDialogFactory urlDialogFactory;
    /** Link handler for swing links. */
    private final SwingLinkHandler linkHandler;
    /** Bus to listen on for events. */
    private final MBassador eventBus;
    /** The event bus for this plugin. */
    private final MBassador swingEventBus;
    /** The provider to use to create tree-based frame managers. */
    private final TreeFrameManagerProvider treeProvider;
    /** The provider to use to create button-based frame managers. */
    private final ButtonBarProvider buttonProvider;
    /** The provider to use to create new main frames. */
    private final Provider<MainFrame> mainFrameProvider;
    /** Swing window manager. */
    private final Provider<SwingWindowManager> swingWindowManager;
    /** Error list dialog provider. */
    private final DialogProvider<ErrorListDialog> errorListDialogProvider;
    /** The main frame of the Swing UI. */
    private MainFrame mainFrame;
    /** Swing UI initialiser. */
    private final SwingUIInitialiser uiInitialiser;

    /**
     * Creates a new instance of {@link SwingManager}.
     *
     * @param windowFactory           The window factory in use.
     * @param windowManager           The window manager to listen on for events.
     * @param mainFrameProvider       The provider to use for the main frame.
     * @param menuBar                 The menu bar to use for the main frame.
     * @param statusBar               The status bar to use in the main frame.
     * @param ctrlTabManager          The window manager that handles ctrl+tab behaviour.
     * @param firstRunExecutor        A provider of first run executors.
     * @param feedbackNagProvider     Provider of feedback nags.
     * @param urlDialogFactory        Factory to use to create URL dialogs.
     * @param linkHandler             The handler to use when users click links.
     * @param eventBus                The bus to listen on for events.
     * @param treeProvider            Provider to use for tree-based frame managers.
     * @param buttonProvider          Provider to use for button-based frame managers.
     * @param swingWindowManager      Swing window manager
     * @param errorListDialogProvider Error list dialog provider
     * @param uiInitialiser           Initialiser to set system/swing settings.
     */
    @Inject
    public SwingManager(
            final Provider<SwingWindowFactory> windowFactory,
            final WindowManager windowManager,
            final Provider<MainFrame> mainFrameProvider,
            final Provider<MenuBar> menuBar,
            final Provider<SwingStatusBar> statusBar,
            final CtrlTabWindowManager ctrlTabManager,
            final Provider<FirstRunWizardExecutor> firstRunExecutor,
            final Provider<FeedbackNag> feedbackNagProvider,
            final URLDialogFactory urlDialogFactory,
            final SwingLinkHandler linkHandler,
            final MBassador eventBus,
            @SwingEventBus final MBassador swingEventBus,
            final TreeFrameManagerProvider treeProvider,
            final ButtonBarProvider buttonProvider,
            final Provider<SwingWindowManager> swingWindowManager,
            final DialogProvider<ErrorListDialog> errorListDialogProvider,
            final SwingUIInitialiser uiInitialiser) {
        this.windowFactory = windowFactory;
        this.windowManager = windowManager;
        this.menuBar = menuBar;
        this.statusBar = statusBar;
        this.mainFrameProvider = mainFrameProvider;
        this.ctrlTabManager = ctrlTabManager;
        this.firstRunExecutor = firstRunExecutor;
        this.feedbackNagProvider = feedbackNagProvider;
        this.urlDialogFactory = urlDialogFactory;
        this.linkHandler = linkHandler;
        this.eventBus = eventBus;
        this.swingEventBus = swingEventBus;
        this.treeProvider = treeProvider;
        this.buttonProvider = buttonProvider;
        this.swingWindowManager = swingWindowManager;
        this.errorListDialogProvider = errorListDialogProvider;
        this.uiInitialiser = uiInitialiser;
    }

    /**
     * Handles loading of the UI.
     */
    public void load() {
        uiInitialiser.load();
        this.mainFrame = mainFrameProvider.get();
        this.mainFrame.setMenuBar(menuBar.get());
        this.mainFrame.setWindowManager(ctrlTabManager);
        this.mainFrame.setStatusBar(statusBar.get());
        this.mainFrame.initComponents();
        swingEventBus.subscribe(mainFrame);
        swingEventBus.subscribe(ctrlTabManager);

        windowManager.addListenerAndSync(windowFactory.get());
        eventBus.subscribe(statusBar.get());
        eventBus.subscribe(this);
        eventBus.subscribe(mainFrame);
        eventBus.subscribe(linkHandler);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                errorListDialogProvider.get().load(ErrorManager.getErrorManager());
            }
        });
    }

    /**
     * Handles unloading of the UI.
     */
    public void unload() {
        for (final Window window : swingWindowManager.get().getTopLevelWindows()) {
            window.dispose();
        }
        windowManager.removeListener(windowFactory.get());
        windowFactory.get().dispose();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                errorListDialogProvider.get().dispose();
            }
        });
        swingEventBus.unsubscribe(mainFrame);
        swingEventBus.unsubscribe(ctrlTabManager);
        mainFrame.dispose();
        eventBus.unsubscribe(statusBar.get());
        eventBus.unsubscribe(this);
        eventBus.unsubscribe(mainFrame);
        eventBus.unsubscribe(linkHandler);
        uiInitialiser.unload();
    }

    /**
     * Retrieves the main frame.
     *
     * @return A main frame instance.
     *
     * @deprecated Should be injected.
     */
    @Deprecated
    public MainFrame getMainFrame() {
        return mainFrame;
    }

    public TreeFrameManagerProvider getTreeProvider() {
        return treeProvider;
    }

    public ButtonBarProvider getButtonProvider() {
        return buttonProvider;
    }

    @Handler
    public void showFirstRunWizard(final FirstRunEvent event) {
        if (!event.isHandled()) {
            firstRunExecutor.get().showWizardAndWait();
            event.setHandled(true);
        }
    }

    @Handler
    public void showURLDialog(final UnknownURLEvent event) {
        if (!event.isHandled()) {
            event.setHandled(true);
            UIUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    urlDialogFactory.getURLDialog(event.getURI()).display();
                }
            });
        }
    }

    @Handler
    public void showFeedbackNag(final FeedbackNagEvent event) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                feedbackNagProvider.get();
            }
        });
    }

}

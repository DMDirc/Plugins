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

import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.components.SplitPane;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
import com.dmdirc.addons.ui_swing.dialogs.ConfirmQuitDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
import com.dmdirc.addons.ui_swing.framemanager.FramemanagerPosition;
import com.dmdirc.addons.ui_swing.framemanager.ctrltab.CtrlTabWindowManager;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.events.ClientFocusGainedEvent;
import com.dmdirc.events.ClientFocusLostEvent;
import com.dmdirc.events.ClientMinimisedEvent;
import com.dmdirc.events.ClientUnminimisedEvent;
import com.dmdirc.events.FrameTitleChangedEvent;
import com.dmdirc.interfaces.LifecycleController;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.CoreUIUtils;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.collections.ListenerList;
import com.dmdirc.util.collections.QueuedLinkedHashSet;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;

import javax.inject.Provider;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import static com.dmdirc.addons.ui_swing.SwingPreconditions.checkOnEDT;

/**
 * The main application frame.
 */
public class MainFrame extends JFrame implements WindowListener,
        ConfigChangeListener, NotificationListener, ActiveFrameManager {

    /** A version number for this class. */
    private static final long serialVersionUID = 9;
    /** Focus queue. */
    private final QueuedLinkedHashSet<TextFrame> focusOrder;
    /** Apple instance. */
    private final Apple apple;
    /** Controller to use to end the program. */
    private final LifecycleController lifecycleController;
    /** The global config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** The icon manager to use to get icons. */
    private final IconManager iconManager;
    /** The quit worker to use when quitting the app. */
    private final Provider<QuitWorker> quitWorker;
    /** Client Version. */
    private final String version;
    /** Frame manager used for ctrl tab frame switching. */
    private CtrlTabWindowManager frameManager;
    /** The listeners registered with this class. */
    private final ListenerList listeners = new ListenerList();
    /** Provider of frame managers. */
    private final Provider<FrameManager> frameManagerProvider;
    /** The bus to despatch events on. */
    private final EventBus eventBus;
    /** The main application icon. */
    private ImageIcon imageIcon;
    /** The frame manager that's being used. */
    private FrameManager mainFrameManager;
    /** Active frame. */
    private TextFrame activeFrame;
    /** Panel holding frame. */
    private JPanel framePanel;
    /** Main panel. */
    private JPanel frameManagerPanel;
    /** Frame manager position. */
    private FramemanagerPosition position;
    /** Show version? */
    private boolean showVersion;
    /** Exit code. */
    private int exitCode = 0;
    /** Status bar. */
    private SwingStatusBar statusBar;
    /** Main split pane. */
    private SplitPane mainSplitPane;
    /** Are we quitting or closing? */
    private boolean quitting;
    /** Have we initialised our settings and listeners? */
    private boolean initDone;

    /**
     * Creates new form MainFrame.
     *
     * @param apple                Apple instance
     * @param lifecycleController  Controller to use to end the application.
     * @param globalConfig         The config to read settings from.
     * @param quitWorker           The quit worker to use when quitting the app.
     * @param iconManager          The icon manager to use to get icons.
     * @param frameManagerProvider Provider to use to retrieve frame managers.
     * @param eventBus             The event bus to post events to.
     */
    public MainFrame(
            final Apple apple,
            final LifecycleController lifecycleController,
            final AggregateConfigProvider globalConfig,
            final Provider<QuitWorker> quitWorker,
            final IconManager iconManager,
            final Provider<FrameManager> frameManagerProvider,
            final EventBus eventBus) {
        checkOnEDT();
        this.apple = apple;
        this.lifecycleController = lifecycleController;
        this.globalConfig = globalConfig;
        this.quitWorker = quitWorker;
        this.iconManager = iconManager;
        this.frameManagerProvider = frameManagerProvider;
        this.eventBus = eventBus;
        version = globalConfig.getOption("version", "version");
        focusOrder = new QueuedLinkedHashSet<>();
    }

    @Override
    public void setVisible(final boolean visible) {
        if (!initDone) {
            imageIcon = new ImageIcon(iconManager.getImage("icon"));
            setIconImage(imageIcon.getImage());

            CoreUIUtils.centreWindow(this);

            addWindowListener(this);

            showVersion = globalConfig.getOptionBool("ui", "showversion");
            globalConfig.addChangeListener("ui", "showversion", this);
            globalConfig.addChangeListener("ui", "framemanager", this);
            globalConfig.addChangeListener("ui", "framemanagerPosition", this);
            globalConfig.addChangeListener("icon", "icon", this);

            addWindowFocusListener(new WindowFocusListener() {

                @Override
                public void windowGainedFocus(final WindowEvent e) {
                    eventBus.post(new ClientFocusGainedEvent());
                }

                @Override
                public void windowLostFocus(final WindowEvent e) {
                    eventBus.post(new ClientFocusLostEvent());
                }
            });

            setTitle(getTitlePrefix());
            initDone = true;
        }
        super.setVisible(visible);
    }

    public SwingStatusBar getStatusBar() {
        return statusBar;
    }

    /**
     * Returns the size of the frame manager.
     *
     * @return Frame manager size.
     */
    public int getFrameManagerSize() {
        if (position == FramemanagerPosition.LEFT || position == FramemanagerPosition.RIGHT) {
            return frameManagerPanel.getWidth();
        } else {
            return frameManagerPanel.getHeight();
        }
    }

    @Override
    public MenuBar getJMenuBar() {
        return (MenuBar) super.getJMenuBar();
    }

    @Override
    public void setTitle(final String title) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (title == null || activeFrame == null) {
                    MainFrame.super.setTitle(getTitlePrefix());
                } else {
                    MainFrame.super.setTitle(getTitlePrefix() + " - " + title);
                }
            }
        });
    }

    /**
     * Gets the string which should be prefixed to this frame's title.
     *
     * @return This frame's title prefix
     */
    private String getTitlePrefix() {
        return "DMDirc" + (showVersion ? " " + version : "");
    }

    @Override
    public void windowOpened(final WindowEvent windowEvent) {
        //ignore
    }

    @Override
    public void windowClosing(final WindowEvent windowEvent) {
        quit(exitCode);
    }

    @Override
    public void windowClosed(final WindowEvent windowEvent) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                lifecycleController.quit(exitCode);
            }
        }, "Quit thread").start();
    }

    @Override
    public void windowIconified(final WindowEvent windowEvent) {
        eventBus.post(new ClientMinimisedEvent());
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowDeiconified(final WindowEvent windowEvent) {
        eventBus.post(new ClientUnminimisedEvent());
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowActivated(final WindowEvent windowEvent) {
        //ignore
    }

    /**
     * {@inheritDoc}.
     *
     * @param windowEvent Window event
     */
    @Override
    public void windowDeactivated(final WindowEvent windowEvent) {
        //ignore
    }

    /** Initialiases the frame managers. */
    private void initFrameManagers() {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                frameManagerPanel.removeAll();
                if (mainFrameManager != null) {
                    removeSelectionListener(mainFrameManager);
                }
                mainFrameManager = frameManagerProvider.get();
                mainFrameManager.setParent(frameManagerPanel);
                addSelectionListener(mainFrameManager);
            }
        });
    }

    /**
     * Initialises the components for this frame.
     */
    public void initComponents() {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                frameManagerPanel = new JPanel();
                activeFrame = null;
                framePanel = new JPanel(new MigLayout("fill, ins 0"));
                initFrameManagers();
                mainSplitPane = initSplitPane();

                setPreferredSize(new Dimension(800, 600));

                getContentPane().setLayout(new MigLayout(
                        "fill, ins rel, wrap 1, hidemode 2"));
                layoutComponents();

                setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

                pack();
            }
        });
    }

    /**
     * Sets the menu bar that this frame will use.
     *
     * <p>
     * Must be called prior to {@link #initComponents()}.
     *
     * @param menuBar The menu bar to use.
     */
    public void setMenuBar(final MenuBar menuBar) {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                apple.setMenuBar(menuBar);
                setJMenuBar(menuBar);
            }
        });
    }

    /**
     * Sets the window manager that this frame will use.
     *
     * <p>
     * Must be called prior to {@link #initComponents()}.
     *
     * @param windowManager The window manager to use.
     */
    public void setWindowManager(final CtrlTabWindowManager windowManager) {
        this.frameManager = windowManager;
    }

    /**
     * Sets the status bar that will be used.
     *
     * <p>
     * Must be called prior to {@link #initComponents()}.
     *
     * @param statusBar The status bar to be used.
     */
    public void setStatusBar(final SwingStatusBar statusBar) {
        this.statusBar = statusBar;
    }

    /**
     * Lays out the this component.
     */
    private void layoutComponents() {
        getContentPane().add(mainSplitPane, "grow, push");
        getContentPane().add(statusBar, "wmax 100%-2*rel, "
                + "wmin 100%-2*rel, south, gap rel rel 0 rel");
    }

    /**
     * Initialises the split pane.
     *
     * @return Returns the initialised split pane
     */
    private SplitPane initSplitPane() {
        final SplitPane splitPane = new SplitPane(globalConfig, SplitPane.Orientation.HORIZONTAL);
        position = FramemanagerPosition.getPosition(
                globalConfig.getOption("ui", "framemanagerPosition"));

        if (position == FramemanagerPosition.UNKNOWN) {
            position = FramemanagerPosition.LEFT;
        }

        if (!mainFrameManager.canPositionVertically() && (position
                == FramemanagerPosition.LEFT || position
                == FramemanagerPosition.RIGHT)) {
            position = FramemanagerPosition.BOTTOM;
        }
        if (!mainFrameManager.canPositionHorizontally() && (position
                == FramemanagerPosition.TOP || position
                == FramemanagerPosition.BOTTOM)) {
            position = FramemanagerPosition.LEFT;
        }

        switch (position) {
            case TOP:
                splitPane.setTopComponent(frameManagerPanel);
                splitPane.setBottomComponent(framePanel);
                splitPane.setResizeWeight(0.0);
                splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        Integer.MAX_VALUE, globalConfig.getOptionInt("ui", "frameManagerSize")));
                break;
            case LEFT:
                splitPane.setLeftComponent(frameManagerPanel);
                splitPane.setRightComponent(framePanel);
                splitPane.setResizeWeight(0.0);
                splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        globalConfig.getOptionInt("ui", "frameManagerSize"), Integer.MAX_VALUE));
                break;
            case BOTTOM:
                splitPane.setTopComponent(framePanel);
                splitPane.setBottomComponent(frameManagerPanel);
                splitPane.setResizeWeight(1.0);
                splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        Integer.MAX_VALUE, globalConfig.getOptionInt("ui", "frameManagerSize")));
                break;
            case RIGHT:
                splitPane.setLeftComponent(framePanel);
                splitPane.setRightComponent(frameManagerPanel);
                splitPane.setResizeWeight(1.0);
                splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                frameManagerPanel.setPreferredSize(new Dimension(
                        globalConfig.getOptionInt("ui", "frameManagerSize"), Integer.MAX_VALUE));
                break;
            default:
                break;
        }

        return splitPane;
    }

    /**
     * Exits with an "OK" status code.
     */
    public void quit() {
        quit(0);
    }

    /**
     * Exit code call to quit.
     *
     * @param exitCode Exit code
     */
    public void quit(final int exitCode) {
        if (exitCode == 0 && globalConfig.getOptionBool("ui", "confirmQuit")) {
            final StandardQuestionDialog dialog = new ConfirmQuitDialog(this) {
                /** Serial version UID. */
                private static final long serialVersionUID = 9;

                @Override
                protected void handleQuit() {
                    doQuit(exitCode);
                }
            };
            dialog.display();
            return;
        }
        doQuit(exitCode);
    }

    /**
     * Exit code call to quit.
     *
     * @param exitCode Exit code
     */
    public void doQuit(final int exitCode) {
        this.exitCode = exitCode;
        quitting = true;

        quitWorker.get().execute();
    }

    @Override
    public void configChanged(final String domain, final String key) {
        if ("ui".equals(domain)) {
            switch (key) {
                case "framemanager":
                case "framemanagerPosition":
                    UIUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            setVisible(false);
                            getContentPane().remove(mainSplitPane);
                            initFrameManagers();
                            getContentPane().removeAll();
                            layoutComponents();
                            setVisible(true);
                        }
                    });
                    break;
                default:
                    showVersion = globalConfig.getOptionBool("ui", "showversion");
                    break;
            }
        } else {
            imageIcon = new ImageIcon(iconManager.getImage("icon"));
            UIUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setIconImage(imageIcon.getImage());
                }
            });
        }
    }

    @Override
    public TextFrame getActiveFrame() {
        return activeFrame;
    }

    @Override
    public void setActiveFrame(final TextFrame activeFrame) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                focusOrder.offerAndMove(activeFrame);
                framePanel.setVisible(false);
                framePanel.removeAll();

                if (MainFrame.this.activeFrame != null) {
                    MainFrame.this.activeFrame.getContainer()
                            .removeNotificationListener(MainFrame.this);
                }
                MainFrame.this.activeFrame = activeFrame;

                if (activeFrame == null) {
                    framePanel.add(new JPanel(), "grow");
                    setTitle(null);
                } else {
                    framePanel.add(activeFrame.getDisplayFrame(), "grow");
                    setTitle(activeFrame.getContainer().getTitle());
                    activeFrame.getContainer().addNotificationListener(
                            MainFrame.this);
                }

                framePanel.setVisible(true);

                if (activeFrame != null) {
                    activeFrame.requestFocus();
                    activeFrame.requestFocusInWindow();
                    activeFrame.activateFrame();
                }

                for (final SelectionListener listener : listeners.get(
                        SelectionListener.class)) {
                    listener.selectionChanged(activeFrame);
                }
            }
        });
    }

    @Override
    public void addSelectionListener(final SelectionListener listener) {
        listeners.add(SelectionListener.class, listener);
    }

    @Override
    public void removeSelectionListener(final SelectionListener listener) {
        listeners.remove(SelectionListener.class, listener);
    }

    @Subscribe
    public void doWindowAdded(final SwingWindowAddedEvent event) {
        final TextFrame window = event.getChildWindow();
        if (activeFrame == null) {
            setActiveFrame(window);
        }
    }

    @Subscribe
    public void doWindowDeleted(final SwingWindowDeletedEvent event) {
        final TextFrame window = event.getChildWindow();
        if (window == null) {
            return; //Deleting a window that doesnt exist will just cause problems, stop
        }
        focusOrder.remove(window);
        if (activeFrame.equals(window)) {
            activeFrame = null;
            framePanel.setVisible(false);
            framePanel.removeAll();
            framePanel.setVisible(true);
            if (focusOrder.peek() == null) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        frameManager.scrollUp();
                    }
                });
            } else {
                setActiveFrame(focusOrder.peek());
            }
        }
    }

    @Subscribe
    public void titleChanged(final FrameTitleChangedEvent event) {
        if (activeFrame != null && activeFrame.getContainer().equals(event.getContainer())) {
            setTitle(event.getTitle());
        }
    }

    @Override
    public void notificationSet(final FrameContainer window,
            final Colour colour) {
        if (activeFrame != null && activeFrame.getContainer().equals(window)) {
            window.clearNotification();
        }
    }

    @Override
    public void notificationCleared(final FrameContainer window) {
        //Ignore
    }

    @Override
    public void dispose() {
        if (!quitting) {
            removeWindowListener(this);
        }

        globalConfig.removeListener(this);
        super.dispose();
    }

}

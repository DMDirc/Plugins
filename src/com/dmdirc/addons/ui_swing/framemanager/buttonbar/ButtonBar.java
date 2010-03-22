/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
package com.dmdirc.addons.ui_swing.framemanager.buttonbar;

import com.dmdirc.FrameContainer;
import com.dmdirc.FrameContainerComparator;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.SelectionListener;
import com.dmdirc.ui.IconManager;
import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
import com.dmdirc.addons.ui_swing.framemanager.FramemanagerPosition;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.UIController;
import com.dmdirc.ui.interfaces.Window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

/**
 * The button bar manager is a grid of buttons that presents a manager similar
 * to that used by mIRC.
 *
 * @author chris
 */
public final class ButtonBar implements FrameManager, ActionListener,
        ComponentListener, Serializable, NotificationListener,
        SelectionListener, FrameInfoListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 3;
    /** A map of containers to the buttons we're using for them. */
    private final Map<FrameContainer<?>, JToggleButton> buttons;
    /** The position of this frame manager. */
    private final FramemanagerPosition position;
    /** The parent for the manager. */
    private JComponent parent;
    /** The scrolling panel for our ButtonBar */
     private final JScrollPane scrollPane;
    /** The panel used for our buttons. */
    private final ButtonPanel buttonPanel;
    /** The currently selected window. */
    private transient FrameContainer<?> selected;
    /** Selected window. */
    private Window activeWindow;
    /** The number of buttons per row or column. */
    private int cells = 1;
    /** The number of buttons to render per {cell,row}. */
    private int maxButtons = Integer.MAX_VALUE;
    /** The width of buttons. */
    private int buttonWidth = 0;
    /** The height of buttons. */
    private int buttonHeight = 25;
    /** UI Controller. */
    private SwingWindowFactory controller;

    /** Creates a new instance of DummyFrameManager. */
    public ButtonBar() {
        scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants
                .HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants
                .VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setMinimumSize(new Dimension(0,buttonHeight));

        buttons = new TreeMap<FrameContainer<?>, JToggleButton>(
                new FrameContainerComparator());
        position = FramemanagerPosition.getPosition(
                IdentityManager.getGlobalConfig().getOption("ui",
                "framemanagerPosition"));

        if (position.isHorizontal()) {
            buttonPanel = new ButtonPanel(new MigLayout("ins rel, fill, flowx"),
                    this);
        } else {
            buttonPanel = new ButtonPanel(new MigLayout("ins rel, fill, flowy"),
                    this);
        }
        scrollPane.getViewport().add(buttonPanel);
    }

    /**
     * Retreives button height.
     *
     * @return Button height
     * @since 0.6.4
     */
    public int getButtonHeight() {
        return buttonHeight;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(final JComponent parent) {
        UIUtilities.invokeLater(new Runnable() {

            /** {inheritDoc} */
            @Override
            public void run() {
                ButtonBar.this.parent = parent;
                scrollPane.setSize(parent.getWidth(), parent.getHeight());

                parent.setLayout(new MigLayout("ins 0"));
                parent.add(scrollPane);
                parent.addComponentListener(ButtonBar.this);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void setController(final UIController controller) {
        if (!(controller instanceof SwingController)) {
            throw new IllegalArgumentException("Controller must be an instance "
                    + "of SwingController");
        }
        this.controller = ((SwingController) controller).getWindowFactory();
    }

    /**
     * Adds any children of parent to the buttonPanel.
     *
     * @param parent Parent window to look for children
     */
    private synchronized void addChildren(
            final FrameContainer<?> parent) {
        for (FrameContainer<?> window : parent.getChildren()) {
            if (buttons.get(window) != null) {
                buttons.get(window).setPreferredSize(new Dimension(
                        buttonWidth, buttonHeight));
                buttonPanel.add(buttons.get(window));
                if (window.getChildren().size() > 0) {
                    addChildren(window);
                }
            }
        }
    }

    /**
     * Removes all buttons from the bar and readds them.
     */
    private synchronized void relayout() {
        buttonPanel.removeAll();

        if (buttons.size() > 0) {
            for (FrameContainer<?> rootWindow : WindowManager.getRootWindows()) {
                if (buttons.get(rootWindow) != null) {
                    buttons.get(rootWindow).setPreferredSize(new Dimension(
                            buttonWidth, buttonHeight));
                    buttonPanel.add(buttons.get(rootWindow));
                    if (rootWindow.getChildren().size() > 0) {
                        addChildren(rootWindow);
                    }
                }
            }
        }

        /*for (Map.Entry<FrameContainer<?>, List<FrameContainer<?>>> entry : windows
                .entrySet()) {
            buttons.get(entry.getKey()).setPreferredSize(new Dimension(
                    buttonWidth, buttonHeight));
            buttonPanel.add(buttons.get(entry.getKey()));

            Collections.sort(entry.getValue(), new FrameContainerComparator());

            for (FrameContainer<?> child : entry.getValue()) {
                buttons.get(child).setPreferredSize(new Dimension(
                        buttonWidth, buttonHeight));
                buttonPanel.add(buttons.get(child));
            }
        }*/
        buttonPanel.validate();
    }

    /**
     * Adds a button to the button array with the details from the specified
     * container.
     *
     * @param source The Container to get title/icon info from
     */
    private void addButton(final FrameContainer<?> source) {
        final JToggleButton button = new JToggleButton(source.toString(),
                IconManager.getIconManager().getIcon(source.getIcon()));
        button.addActionListener(this);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMinimumSize(new Dimension(0,buttonHeight));
        button.setMargin(new Insets(0, 0, 0, 0));
        synchronized(buttons) {
            buttons.put(source, button);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionVertically() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canPositionHorizontally() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void windowAdded(final Window parent, final Window window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (parent == null) {
                    addButton(window.getContainer());
                } else {
                    addButton(window.getContainer());
                }

                relayout();
                window.getContainer().addNotificationListener(ButtonBar.this);
                window.getContainer().addSelectionListener(ButtonBar.this);
                window.getContainer().addFrameInfoListener(ButtonBar.this);
            }

        });
    }

    /** {@inheritDoc} */
    @Override
    public void windowDeleted(final Window parent, final Window window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                synchronized(buttons) {
                    if (parent == null) {
                        buttons.remove(window.getContainer());
                    } else {
                        buttons.remove(window.getContainer());
                    }
                }

                relayout();
                window.getContainer().removeNotificationListener(ButtonBar.this);
                window.getContainer().removeFrameInfoListener(ButtonBar.this);
                window.getContainer().removeSelectionListener(ButtonBar.this);
            }
        });
    }

    /**
     * Called when the user clicks on one of the buttons.
     *
     * @param e The action event associated with this action
     */
    @Override
    public synchronized void actionPerformed(final ActionEvent e) {
        for (Map.Entry<FrameContainer<?>, JToggleButton> entry : buttons.entrySet()) {
            if (entry.getValue().equals(e.getSource())) {
                final TextFrame frame = (TextFrame) controller.getSwingWindow(
                        entry.getKey());
                if (frame != null && frame.equals(activeWindow)) {
                    entry.getValue().setSelected(true);
                }
                entry.getKey().activateFrame();
            }
        }
    }

    /**
     * Called when the parent component is resized.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    @Override
    public void componentResized(final ComponentEvent e) {
        buttonWidth = position.isHorizontal() ? 150 : (parent.getWidth() / cells);
        relayout();
    }

    /**
     * Called when the parent component is moved.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    @Override
    public void componentMoved(final ComponentEvent e) {
        // Do nothing
    }

    /**
     * Called when the parent component is made visible.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    @Override
    public void componentShown(final ComponentEvent e) {
        // Do nothing
    }

    /**
     * Called when the parent component is made invisible.
     *
     * @param e A ComponentEvent corresponding to this event.
     */
    @Override
    public void componentHidden(final ComponentEvent e) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void notificationSet(final FrameContainer<?> window, final Color colour) {
        if (buttons.containsKey(window)) {
            buttons.get(window).setForeground(colour);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notificationCleared(final FrameContainer<?> window) {
        notificationSet(window, window.getNotification());
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void selectionChanged(final FrameContainer<?> window) {
        activeWindow = (TextFrame) controller.getSwingWindow(window);
        if (selected != null && buttons.containsKey(selected)) {
            buttons.get(selected).setSelected(false);
        }

        selected = window;

        if (buttons.containsKey(window)) {
            buttons.get(window).setSelected(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void iconChanged(final FrameContainer<?> window, final String icon) {
        buttons.get(window).setIcon(IconManager.getIconManager().getIcon(icon));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void nameChanged(final FrameContainer<?> window, final String name) {
        buttons.get(window).setText(name);
    }

    /** {@inheritDoc} */
    @Override
    public void titleChanged(final FrameContainer<?> window, final String title) {
        // Do nothing
    }
}

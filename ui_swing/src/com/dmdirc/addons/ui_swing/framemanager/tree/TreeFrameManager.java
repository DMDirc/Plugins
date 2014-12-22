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

package com.dmdirc.addons.ui_swing.framemanager.tree;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.injection.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingEventBus;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowSelectedEvent;
import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.events.FrameIconChangedEvent;
import com.dmdirc.events.NotificationClearedEvent;
import com.dmdirc.events.NotificationSetEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.IconManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.util.colours.Colour;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

/**
 * Manages open windows in the application in a tree style view.
 */
public class TreeFrameManager implements FrameManager, Serializable, ConfigChangeListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 5;
    /** node storage, used for adding and deleting nodes correctly. */
    private final Map<Window, TreeViewNode> nodes;
    /** Configuration manager. */
    private final AggregateConfigProvider config;
    /** Colour manager. */
    private final ColourManager colourManager;
    /** Factory to use to retrieve swing windows. */
    private final SwingWindowFactory windowFactory;
    /** Window manage. */
    private final WindowManager windowManager;
    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;
    /** Swing event bus. */
    private final SwingEventBus swingEventBus;
    /** Icon manager. */
    private final IconManager iconManager;
    /** display tree. */
    private Tree tree;
    /** data model. */
    private TreeViewModel model;
    /** Tree scroller. */
    private TreeScroller scroller;

    @Inject
    public TreeFrameManager(final WindowManager windowManager,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @GlobalConfig final ColourManager colourManager,
            final ActiveFrameManager activeFrameManager,
            final SwingWindowFactory windowFactory,
            @PluginDomain(SwingController.class) final String domain,
            final DMDircMBassador eventBus,
            final SwingEventBus swingEventBus,
            @GlobalConfig final IconManager iconManager) {
        this.windowFactory = windowFactory;
        this.windowManager = windowManager;
        this.nodes = new HashMap<>();
        this.config = globalConfig;
        this.colourManager = colourManager;
        this.activeFrameManager = activeFrameManager;
        this.eventBus = eventBus;
        this.swingEventBus = swingEventBus;
        this.iconManager = iconManager;

        UIUtilities.invokeLater(() -> {
            model = new TreeViewModel(config, new TreeViewNode(null, null));
            tree = new Tree(this, model, swingEventBus, globalConfig, domain);
            tree.setCellRenderer(
                    new TreeViewTreeCellRenderer(config, colourManager, this, eventBus));
            tree.setVisible(true);

            config.addChangeListener("treeview", this);
            config.addChangeListener("ui", "sortrootwindows", this);
            config.addChangeListener("ui", "sortchildwindows", this);
            config.addChangeListener("ui", "backgroundcolour", this);
            config.addChangeListener("ui", "foregroundcolour", this);
        });
    }

    @Override
    public boolean canPositionVertically() {
        return true;
    }

    @Override
    public boolean canPositionHorizontally() {
        return false;
    }

    @Override
    public void setParent(final JComponent parent) {
        SwingUtilities.invokeLater(() -> {
            final JScrollPane scrollPane = new JScrollPane(tree);
            scrollPane.setAutoscrolls(true);
            scrollPane.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            parent.setVisible(false);
            parent.setLayout(new MigLayout("ins 0, fill"));
            parent.add(scrollPane, "grow");
            parent.setFocusable(false);
            parent.setVisible(true);

            setColours();

            eventBus.subscribe(this);
            swingEventBus.subscribe(this);
            redoTreeView();
        });
    }

    @Handler
    public void doAddWindow(final SwingWindowAddedEvent event) {
        final TextFrame parent = event.getParentWindow().orElse(null);
        final TextFrame window = event.getChildWindow();
        if (nodes.containsKey(window)) {
            return;
        }
        if (parent == null) {
            addWindow(model.getRootNode(), window);
        } else {
            addWindow(nodes.get(parent), window);
        }
    }

    @Handler
    public void doDeleteWindow(final SwingWindowDeletedEvent event) {
        final TextFrame window = event.getChildWindow();
        UIUtilities.invokeAndWait(() -> {
            if (nodes.get(window) == null) {
                return;
            }
            final DefaultMutableTreeNode node = nodes.get(window);
            if (node.getLevel() == 0) {
                eventBus.publishAsync(
                        new UserErrorEvent(ErrorLevel.MEDIUM, new IllegalArgumentException(),
                                "delServer triggered for root node" + node, ""));
            } else {
                model.removeNodeFromParent(nodes.get(window));
            }
            synchronized (nodes) {
                eventBus.unsubscribe(nodes.get(window).getLabel());
                nodes.remove(window);
            }
        });
    }

    /**
     * Adds a window to the frame container.
     *
     * @param parent Parent node
     * @param window Window to add
     */
    public void addWindow(final MutableTreeNode parent, final TextFrame window) {
        UIUtilities.invokeAndWait(() -> {
            final NodeLabel label = new NodeLabel(window, iconManager);
            eventBus.subscribe(label);
            swingEventBus.subscribe(label);
            final TreeViewNode node = new TreeViewNode(label, window);
            synchronized (nodes) {
                nodes.put(window, node);
            }
            if (parent == null) {
                model.insertNodeInto(node, model.getRootNode());
            } else {
                model.insertNodeInto(node, parent);
            }
            tree.expandPath(new TreePath(node.getPath()).getParentPath());
            final Rectangle view = tree.getRowBounds(tree.getRowForPath(new TreePath(node.
                    getPath())));
            if (view != null) {
                tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(), 0, 0));
            }

            // TODO: Should this colour be configurable?
            node.getLabel().notificationSet(new NotificationSetEvent(window.getContainer(),
                    window.getContainer().getNotification().orElse(Colour.BLACK)));
            node.getLabel().iconChanged(new FrameIconChangedEvent(window.getContainer(),
                    window.getContainer().getIcon()));
        });
    }

    /**
     * Returns the tree for this frame manager.
     *
     * @return Tree for the manager
     */
    public JTree getTree() {
        return tree;
    }

    /**
     * Checks for and sets a rollover node.
     *
     * @param event event to check
     */
    protected void checkRollover(final MouseEvent event) {
        NodeLabel node = null;

        if (event != null && tree.getNodeForLocation(event.getX(), event.getY()) != null) {
            node = tree.getNodeForLocation(event.getX(), event.getY()).getLabel();
        }

        synchronized (nodes) {
            for (TreeViewNode treeNode : nodes.values()) {
                final NodeLabel label = treeNode.getLabel();
                label.setRollover(label == node);
            }
        }
        tree.repaint();
    }

    /** Sets treeview colours. */
    private void setColours() {
        tree.setBackground(UIUtilities.convertColour(colourManager.getColourFromString(
                        config.getOptionString("treeview", "backgroundcolour", "ui",
                                "backgroundcolour"), null)));
        tree.setForeground(UIUtilities.convertColour(colourManager.getColourFromString(
                        config.getOptionString("treeview", "foregroundcolour", "ui",
                                "foregroundcolour"), null)));

        tree.repaint();
    }

    @Override
    public void configChanged(final String domain, final String key) {
        if ("sortrootwindows".equals(key) || "sortchildwindows".equals(key)) {
            redoTreeView();
        } else {
            setColours();
        }
    }

    /**
     * Starts the tree from scratch taking into account new sort orders.
     */
    private void redoTreeView() {
        UIUtilities.invokeLater(() -> {
            ((DefaultTreeModel) tree.getModel()).setRoot(null);
            ((DefaultTreeModel) tree.getModel()).setRoot(new TreeViewNode(null, null));
            if (scroller != null) {
                scroller.unregister();
            }
            scroller = new TreeTreeScroller(swingEventBus, tree);

            for (FrameContainer window : windowManager.getRootWindows()) {
                addWindow(null, windowFactory.getSwingWindow(window));
                final Collection<FrameContainer> childWindows = window.getChildren();
                for (FrameContainer childWindow : childWindows) {
                    addWindow(nodes.get(windowFactory.getSwingWindow(window)),
                            windowFactory.getSwingWindow(childWindow));
                }
            }

            if (activeFrameManager.getActiveFrame() != null) {
                selectionChanged(new SwingWindowSelectedEvent(activeFrameManager.getActiveFrame()));
            }
        });
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void selectionChanged(final SwingWindowSelectedEvent event) {
        if (event.getWindow().isPresent()) {
            UIUtilities.invokeLater(() -> {
                final TreeNode[] treePath = ((DefaultTreeModel) tree.getModel())
                        .getPathToRoot(nodes.get(event.getWindow().get()));
                if (treePath != null && treePath.length > 0) {
                    final TreePath path = new TreePath(treePath);
                    tree.setTreePath(path);
                    tree.scrollPathToVisible(path);
                }
            });
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class, delivery = Invoke.Asynchronously)
    public void notificationSet(final NotificationSetEvent event) {
        synchronized (nodes) {
            final TreeViewNode node = nodes.get(windowFactory.getSwingWindow(event.getWindow()));
            if (event.getWindow() != null && node != null) {
                final NodeLabel label = node.getLabel();
                if (label != null) {
                    label.notificationSet(event);
                    tree.repaint();
                }
            }
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class, delivery = Invoke.Asynchronously)
    public void notificationCleared(final NotificationClearedEvent event) {
        synchronized (nodes) {
            final TreeViewNode node = nodes.get(windowFactory.getSwingWindow(event.getWindow()));
            if (event.getWindow() != null && node != null) {
                final NodeLabel label = node.getLabel();
                if (label != null) {
                    label.notificationCleared();
                    tree.repaint();
                }
            }
        }
    }

}

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
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
import com.dmdirc.interfaces.FrameInfoListener;
import com.dmdirc.interfaces.NotificationListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.Colour;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.messages.ColourManager;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

/**
 * Manages open windows in the application in a tree style view.
 */
public class TreeFrameManager implements FrameManager,
        Serializable, ConfigChangeListener, NotificationListener,
        FrameInfoListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 5;
    /** display tree. */
    private Tree tree;
    /** data model. */
    private TreeViewModel model;
    /** node storage, used for adding and deleting nodes correctly. */
    private final Map<FrameContainer, TreeViewNode> nodes;
    /** Tree scroller. */
    private TreeScroller scroller;
    /** Configuration manager. */
    private final AggregateConfigProvider config;
    /** Colour manager. */
    private final ColourManager colourManager;
    /** Factory to use to retrieve swing windows. */
    private final SwingWindowFactory windowFactory;
    /** Window manage. */
    private final WindowManager windowManager;
    /** Provider to use to retrieve the current main frame. */
    private final Provider<MainFrame> mainFrameProvider;

    /**
     * Creates a new instance of the TreeFrameManager.
     *
     * @param windowManager     The window manager to use to read window state.
     * @param globalConfig      The provider to read config settings from.
     * @param colourManager     The colour manager to use to retrieve colours.
     * @param mainFrameProvider The provider to use to retrieve the current main frame.
     * @param windowFactory     The factory to use to retrieve swing windows.
     * @param domain            The domain to read settings from.
     */
    @Inject
    public TreeFrameManager(
            final WindowManager windowManager,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final ColourManager colourManager,
            final Provider<MainFrame> mainFrameProvider,
            final SwingWindowFactory windowFactory,
            @PluginDomain(SwingController.class) final String domain) {
        this.windowFactory = windowFactory;
        this.windowManager = windowManager;
        this.nodes = new HashMap<>();
        this.config = globalConfig;
        this.colourManager = colourManager;

        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model = new TreeViewModel(config, new TreeViewNode(null, null));
                tree = new Tree(TreeFrameManager.this, model, mainFrameProvider, globalConfig,
                        windowFactory, domain);
                tree.setCellRenderer(new TreeViewTreeCellRenderer(config, colourManager,
                        TreeFrameManager.this));
                tree.setVisible(true);

                config.addChangeListener("treeview", TreeFrameManager.this);
                config.addChangeListener("ui", "sortrootwindows", TreeFrameManager.this);
                config.addChangeListener("ui", "sortchildwindows", TreeFrameManager.this);
                config.addChangeListener("ui", "backgroundcolour", TreeFrameManager.this);
                config.addChangeListener("ui", "foregroundcolour", TreeFrameManager.this);
            }
        });
        this.mainFrameProvider = mainFrameProvider;
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
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final JScrollPane scrollPane = new JScrollPane(tree);
                scrollPane.setAutoscrolls(true);
                scrollPane.setHorizontalScrollBarPolicy(
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

                parent.setVisible(false);
                parent.setLayout(new MigLayout("ins 0, fill"));
                parent.add(scrollPane, "grow");
                parent.setFocusable(false);
                parent.setVisible(true);

                setColours();

                redoTreeView();
            }
        });
    }

    @Override
    public void windowAdded(final TextFrame parent, final TextFrame window) {
        if (nodes.containsKey(window.getContainer())) {
            return;
        }
        if (parent == null) {
            addWindow(model.getRootNode(), window.getContainer());
        } else {
            addWindow(nodes.get(parent.getContainer()), window.getContainer());
        }
    }

    @Override
    public void windowDeleted(final TextFrame parent, final TextFrame window) {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                if (nodes == null || nodes.get(window.getContainer()) == null) {
                    return;
                }
                final DefaultMutableTreeNode node = nodes.get(window.getContainer());
                if (node.getLevel() == 0) {
                    Logger.appError(ErrorLevel.MEDIUM,
                            "delServer triggered for root node"
                            + node.toString(),
                            new IllegalArgumentException());
                } else {
                    model.removeNodeFromParent(nodes.get(window.getContainer()));
                }
                synchronized (nodes) {
                    nodes.remove(window.getContainer());
                }
                window.getContainer().removeFrameInfoListener(
                        TreeFrameManager.this);
                window.getContainer().removeNotificationListener(
                        TreeFrameManager.this);
            }
        });
    }

    /**
     * Adds a window to the frame container.
     *
     * @param parent Parent node
     * @param window Window to add
     */
    public void addWindow(final TreeViewNode parent,
            final FrameContainer window) {
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                final NodeLabel label = new NodeLabel(window);
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
                    tree.scrollRectToVisible(new Rectangle(0, (int) view.getY(),
                            0, 0));
                }
                window.addFrameInfoListener(TreeFrameManager.this);
                window.addNotificationListener(TreeFrameManager.this);

                node.getLabel().notificationSet(window, window.getNotification());
                node.getLabel().iconChanged(window, window.getIcon());
            }
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
        tree.setBackground(UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "treeview", "backgroundcolour",
                                "ui", "backgroundcolour"), null)));
        tree.setForeground(UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "treeview", "foregroundcolour",
                                "ui", "foregroundcolour"), null)));

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
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ((DefaultTreeModel) tree.getModel()).setRoot(null);
                ((DefaultTreeModel) tree.getModel()).setRoot(new TreeViewNode(
                        null, null));
                if (scroller != null) {
                    scroller.unregister();
                }
                scroller = new TreeTreeScroller(mainFrameProvider, windowFactory, tree);

                for (FrameContainer window : windowManager.getRootWindows()) {
                    addWindow(null, window);
                    final Collection<FrameContainer> childWindows = window
                            .getChildren();
                    for (FrameContainer childWindow : childWindows) {
                        addWindow(nodes.get(window), childWindow);
                    }
                }

                final MainFrame mainFrame = mainFrameProvider.get();
                if (mainFrame != null && mainFrame.getActiveFrame() != null) {
                    selectionChanged(mainFrame.getActiveFrame());
                }
            }
        });
    }

    @Override
    public void selectionChanged(final TextFrame window) {
        synchronized (nodes) {
            final Collection<TreeViewNode> collection = new ArrayList<>(nodes.values());
            for (TreeViewNode treeNode : collection) {
                final NodeLabel label = treeNode.getLabel();
                label.selectionChanged(window);
            }
        }

        if (window != null) {
            UIUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final TreeNode[] treePath = ((DefaultTreeModel) tree.getModel()).getPathToRoot(
                            nodes.get(window.getContainer()));
                    if (treePath != null && treePath.length > 0) {
                        final TreePath path = new TreePath(treePath);
                        tree.setTreePath(path);
                        tree.scrollPathToVisible(path);
                    }
                }
            });
        }
    }

    @Override
    public void notificationSet(final FrameContainer window, final Colour colour) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (nodes) {
                    final TreeViewNode node = nodes.get(window);
                    if (window != null && node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.notificationSet(window, colour);
                            tree.repaint();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void notificationCleared(final FrameContainer window) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (nodes) {
                    final TreeViewNode node = nodes.get(window);
                    if (window != null && node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.notificationCleared(window);
                            tree.repaint();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void iconChanged(final FrameContainer window, final String icon) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (nodes) {
                    final TreeViewNode node = nodes.get(window);
                    if (node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.iconChanged(window, icon);
                            tree.repaint();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void nameChanged(final FrameContainer window, final String name) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (nodes) {
                    final TreeViewNode node = nodes.get(window);
                    if (node != null) {
                        final NodeLabel label = node.getLabel();
                        if (label != null) {
                            label.nameChanged(window, name);
                            tree.repaint();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void titleChanged(final FrameContainer window, final String title) {
        // Do nothing
    }

}
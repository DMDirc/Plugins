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

import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.CloseFrameContainerAction;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.layout.PlatformDefaults;

/**
 * Specialised JTree for the frame manager.
 */
public class Tree extends JTree implements MouseMotionListener,
        ConfigChangeListener, MouseListener, ActionListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Tree frame manager. */
    private final TreeFrameManager manager;
    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** Config manager. */
    private final AggregateConfigProvider config;
    /** Drag selection enabled? */
    private boolean dragSelect;
    /** Drag button 1? */
    private boolean dragButton;
    /** Show handles. */
    private boolean showHandles;
    /** Factory to use to retrieve swing windows. */
    private final SwingWindowFactory windowFactory;

    /**
     * Specialised JTree for frame manager.
     *
     * @param manager           Frame manager
     * @param model             tree model.
     * @param activeFrameManager The active window manager
     * @param globalConfig      The config to read settings from.
     * @param domain            The domain to read settings from.
     * @param windowFactory     The factory to use to get swing windows.
     */
    public Tree(
            final TreeFrameManager manager,
            final TreeModel model,
            final ActiveFrameManager activeFrameManager,
            final AggregateConfigProvider globalConfig,
            final SwingWindowFactory windowFactory,
            final String domain) {
        super(model);

        this.manager = manager;
        this.config = globalConfig;
        this.windowFactory = windowFactory;
        this.activeFrameManager = activeFrameManager;

        putClientProperty("JTree.lineStyle", "Angled");
        getInputMap().setParent(null);
        getInputMap(JComponent.WHEN_FOCUSED).clear();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).clear();
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setRootVisible(false);
        setRowHeight(getFontMetrics(UIManager.getFont("Tree.font")).getHeight() + 2);
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue(),
                (int) PlatformDefaults.getUnitValueX("related").getValue()));
        setFocusable(false);

        dragSelect = config.getOptionBool("treeview", "dragSelection");
        showHandles = config.getOptionBool(domain, "showtreeexpands");
        config.addChangeListener(domain, "showtreeexpands", this);
        config.addChangeListener("treeview", this);

        setShowsRootHandles(showHandles);
        putClientProperty("showHandles", showHandles);

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void scrollRectToVisible(final Rectangle aRect) {
        final Rectangle rect = new Rectangle(0, aRect.y, aRect.width, aRect.height);
        super.scrollRectToVisible(rect);
    }

    /**
     * Set path.
     *
     * @param path Path
     */
    public void setTreePath(final TreePath path) {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setSelectionPath(path);
            }
        });
    }

    /**
     * Returns the node for the specified location, returning null if rollover is disabled or there
     * is no node at the specified location.
     *
     * @param x x coordiantes
     * @param y y coordiantes
     *
     * @return node or null
     */
    public TreeViewNode getNodeForLocation(final int x,
            final int y) {
        TreeViewNode node = null;
        final TreePath selectedPath = getPathForLocation(x, y);
        if (selectedPath != null) {
            node = (TreeViewNode) selectedPath.getLastPathComponent();
        }
        return node;
    }

    @Override
    public void configChanged(final String domain, final String key) {
        switch (key) {
            case "dragSelection":
                dragSelect = config.getOptionBool("treeview", "dragSelection");
                break;
            case "showtreeexpands":
                showHandles = config.getOptionBool(domain, "showtreeexpands");
                setShowsRootHandles(showHandles);
                putClientProperty("showHandles", showHandles);
                break;
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (dragSelect && dragButton) {
            final TreeViewNode node = getNodeForLocation(e.getX(), e.getY());
            if (node != null) {
                activeFrameManager.setActiveFrame(windowFactory.getSwingWindow(
                        ((TreeViewNode) new TreePath(node.getPath()).getLastPathComponent())
                        .getWindow()));
            }
        }
        manager.checkRollover(e);
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        manager.checkRollover(e);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        processMouseEvents(e);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            dragButton = true;
            final TreePath selectedPath = getPathForLocation(e.getX(),
                    e.getY());
            if (selectedPath != null) {
                activeFrameManager.setActiveFrame(windowFactory.getSwingWindow(
                        ((TreeViewNode) selectedPath.getLastPathComponent()).getWindow()));
            }
        }
        processMouseEvents(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        dragButton = false;
        processMouseEvents(e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        manager.checkRollover(null);
    }

    /**
     * Processes every mouse button event to check for a popup trigger.
     *
     * @param e mouse event
     */
    void processMouseEvents(final MouseEvent e) {
        final TreePath localPath = getPathForLocation(e.getX(), e.getY());
        if (localPath != null && e.isPopupTrigger()) {
            final TextFrame frame = windowFactory.getSwingWindow(
                    ((TreeViewNode) localPath.getLastPathComponent()).getWindow());

            if (frame == null) {
                return;
            }

            final JPopupMenu popupMenu = frame.getPopupMenu(null,
                    new Object[][]{new Object[]{""}});
            frame.addCustomPopupItems(popupMenu);
            if (popupMenu.getComponentCount() > 0) {
                popupMenu.addSeparator();
            }
            final TreeViewNodeMenuItem popoutMenuItem;
            if (frame.getPopoutFrame() == null) {
                popoutMenuItem = new TreeViewNodeMenuItem("Pop Out", "popout",
                        (TreeViewNode) localPath.getLastPathComponent());
            } else {
                popoutMenuItem = new TreeViewNodeMenuItem("Pop In", "popin",
                        (TreeViewNode) localPath.getLastPathComponent());
            }
            popupMenu.add(popoutMenuItem);
            popupMenu.addSeparator();
            popoutMenuItem.addActionListener(this);

            final TreeViewNodeMenuItem moveUp = new TreeViewNodeMenuItem("Move Up", "Up",
                    (TreeViewNode) localPath.getLastPathComponent());
            final TreeViewNodeMenuItem moveDown = new TreeViewNodeMenuItem("Move Down", "Down",
                    (TreeViewNode) localPath.getLastPathComponent());

            moveUp.addActionListener(this);
            moveDown.addActionListener(this);

            popupMenu.add(moveUp);
            popupMenu.add(moveDown);
            popupMenu.add(new JMenuItem(new CloseFrameContainerAction(frame.
                    getContainer())));
            popupMenu.show(this, e.getX(), e.getY());
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final TreeViewNode node = ((TreeViewNodeMenuItem) e.getSource()).
                getTreeNode();
        int index = getModel().getIndexOfChild(node.getParent(), node);
        switch (e.getActionCommand()) {
            case "Up":
                if (index == 0) {
                    index = node.getSiblingCount() - 1;
                } else {
                    index--;
                }
                break;
            case "Down":
                if (index == node.getSiblingCount() - 1) {
                    index = 0;
                } else {
                    index++;
                }
                break;
            case "popout":
                windowFactory.getSwingWindow(node.getWindow()).setPopout(true);
                break;
            case "popin":
                windowFactory.getSwingWindow(node.getWindow()).setPopout(false);
                break;
        }
        final TreeViewNode parentNode = (TreeViewNode) node.getParent();
        final TreePath nodePath = new TreePath(node.getPath());
        final boolean isExpanded = isExpanded(nodePath);
        ((DefaultTreeModel) getModel()).removeNodeFromParent(node);
        ((TreeViewModel) getModel()).insertNodeInto(node, parentNode, index);
        setExpandedState(nodePath, isExpanded);
    }

}

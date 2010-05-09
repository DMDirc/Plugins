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

package com.dmdirc.addons.ui_swing.dialogs.serverlist;

import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.renderers.ServerGroupTreeRenderer;
import com.dmdirc.serverlists.ServerGroup;
import com.dmdirc.serverlists.ServerGroupItem;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

/**
 * Tree of server groups and items.
 */
public class Tree extends JPanel implements TreeSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Tree. */
    private final JTree items;
    /** Tree model. */
    private final DefaultTreeModel treeModel;
    /** Server list model. */
    private final ServerListModel model;

    /**
     * Instantiates a new tree of server groups.
     *
     * @param model Model backing this tree
     */
    public Tree(final ServerListModel model) {
        super();

        this.model = model;
        items = new JTree(model.getTreeModel()) {

            /**
             * A version number for this class. It should be changed whenever
             * the class structure is changed (or anything else that would
             * prevent serialized objects being unserialized with the new
             * class).
             */
            private static final long serialVersionUID = 2;

            /** {@inheritDoc} */
            @Override
            public void scrollRectToVisible(final Rectangle aRect) {
                final Rectangle rect = new Rectangle(0, aRect.y,
                        aRect.width, aRect.height);
                super.scrollRectToVisible(rect);
            }
        };

        items.putClientProperty("JTree.lineStyle", "Angled");
        items.getInputMap().setParent(null);
        items.getInputMap(JComponent.WHEN_FOCUSED).clear();
        items.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .clear();
        items.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        items.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        items.setRootVisible(false);
        items.setRowHeight(0);
        items.setShowsRootHandles(false);
        items.setOpaque(true);
        items.setFocusable(false);
        items.setRootVisible(false);
        items.setShowsRootHandles(true);
        new TreeScroller(items);
        items.setCellRenderer(new ServerGroupTreeRenderer());
        treeModel = (DefaultTreeModel) items.getModel();
        items.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
        items.setSelectionRow(0);
        valueChanged(null);
        addListeners();

        setLayout(new MigLayout("fill, ins 0"));

        add(new JScrollPane(items), "grow, push");
    }

    /**
     * Adds required listeners.
     */
    private void addListeners() {
        items.addTreeSelectionListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        final DefaultMutableTreeNode itemNode = (DefaultMutableTreeNode) items.
                getSelectionPath().getLastPathComponent();
        DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) items.
                getSelectionPath().getLastPathComponent();
        while (!((groupNode.getUserObject()) instanceof ServerGroup)) {
            groupNode = (DefaultMutableTreeNode) groupNode.getParent();
        }
        model.setSelectedItem((ServerGroupItem) itemNode.getUserObject());
    }
}

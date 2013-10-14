/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.framemanager.ctrltab;

import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.SwingWindowListener;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.NextFrameAction;
import com.dmdirc.addons.ui_swing.actions.PreviousFrameAction;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewModel;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewNode;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.interfaces.ui.Window;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A Window manager to handle ctrl[+shift]+tab switching between windows.
 */
public class CtrlTabWindowManager implements SwingWindowListener,
        SelectionListener {

    /** Node storage, used for adding and deleting nodes correctly. */
    private final Map<Window, TreeViewNode> nodes;
    /** Data model. */
    private final TreeViewModel model;
    /** Tree Scroller. */
    private final TreeScroller treeScroller;
    /** Selection model for the tree scroller. */
    private final TreeSelectionModel selectionModel;

    /**
     * Creates a new ctrl tab window manager.
     *
     * @param controller Parent controller
     * @param windowFactory The window factory to use to create and listen for windows.
     * @param mainFrame The main frame that owns this window manager
     * @param component Component to add listen to events on
     */
    public CtrlTabWindowManager(
            final SwingController controller,
            final SwingWindowFactory windowFactory,
            final MainFrame mainFrame,
            final JComponent component) {
        nodes = new HashMap<>();
        model = new TreeViewModel(controller, new TreeViewNode(null, null));
        selectionModel = new DefaultTreeSelectionModel();
        treeScroller = new TreeScroller(model, selectionModel, false) {

            /** {@inheritDoc} */
            @Override
            protected void setPath(final TreePath path) {
                super.setPath(path);
                mainFrame.setActiveFrame(windowFactory.getSwingWindow(
                        ((TreeViewNode) path.getLastPathComponent()).getWindow()));
            }
        };

        windowFactory.addWindowListener(this);
        mainFrame.addSelectionListener(this);

        component.getActionMap().put("prevFrameAction",
                new PreviousFrameAction(treeScroller));
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                "prevFrameAction");
        component.getActionMap().put(
                "nextFrameAction", new NextFrameAction(treeScroller));
        component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                KeyEvent.CTRL_DOWN_MASK), "nextFrameAction");
    }

    /* {@inheritDoc} */
    @Override
    public void windowAdded(final TextFrame parent, final TextFrame window) {
        final TreeViewNode parentNode;
        if (parent == null) {
            parentNode = model.getRootNode();
        } else {
            parentNode = nodes.get(parent);
        }

        UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final TreeViewNode node = new TreeViewNode(null, window.
                        getContainer());
                synchronized (nodes) {
                    nodes.put(window, node);
                }
                node.setUserObject(window);
                model.insertNodeInto(node, parentNode);
            }
        });
    }

    /* {@inheritDoc} */
    @Override
    public void windowDeleted(final TextFrame parent, final TextFrame window) {
         UIUtilities.invokeAndWait(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                if (nodes == null || nodes.get(window) == null) {
                    return;
                }
                final TreeViewNode node = nodes.get(window);
                if (node.getLevel() == 0) {
                    Logger.appError(ErrorLevel.MEDIUM,
                            "delServer triggered for root node"
                            + node.toString(),
                            new IllegalArgumentException());
                } else {
                    model.removeNodeFromParent(nodes.get(window));
                }
                nodes.remove(window);
            }
        });
    }

    /** Scrolls up. */
    public void scrollUp() {
        treeScroller.changeFocus(true);
    }

    /** Scrolls down. */
    public void scrollDown() {
        treeScroller.changeFocus(false);
    }

    /* {@inheritDoc} */
    @Override
    public void selectionChanged(final TextFrame window) {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                final TreeNode[] path = model.getPathToRoot(nodes.get(
                        window));
                if (path != null && path.length > 0) {
                    selectionModel.setSelectionPath(new TreePath(path));
                }
            }
        });
    }
}

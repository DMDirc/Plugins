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

package com.dmdirc.addons.ui_swing.framemanager.ctrltab;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SelectionListener;
import com.dmdirc.addons.ui_swing.SwingWindowFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.NextFrameAction;
import com.dmdirc.addons.ui_swing.actions.PreviousFrameAction;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewModel;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewNode;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.logger.ErrorLevel;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dmdirc.DMDircMBassador;
import net.engio.mbassy.listener.Handler;

/**
 * A Window manager to handle ctrl[+shift]+tab switching between windows.
 */
@Singleton
public class CtrlTabWindowManager implements SelectionListener {

    /** Node storage, used for adding and deleting nodes correctly. */
    private final Map<Window, TreeViewNode> nodes;
    /** Data model. */
    private final TreeViewModel model;
    /** Tree Scroller. */
    private final TreeScroller treeScroller;
    /** Selection model for the tree scroller. */
    private final TreeSelectionModel selectionModel;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new ctrl tab window manager.
     *
     * @param globalConfig       The configuration to read settings from.
     * @param windowFactory      The window factory to use to create and listen for windows.
     * @param mainFrame          The main frame that owns this window manager
     * @param activeFrameManager Active frame manager.
     * @param eventBus           The eventBus to post errors to
     */
    @Inject
    public CtrlTabWindowManager(
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final SwingWindowFactory windowFactory,
            final ActiveFrameManager activeFrameManager,
            final MainFrame mainFrame,
            final DMDircMBassador eventBus) {
        this.eventBus = eventBus;
        nodes = new HashMap<>();
        model = new TreeViewModel(globalConfig, new TreeViewNode(null, null));
        selectionModel = new DefaultTreeSelectionModel();
        treeScroller = new TreeScroller(model, selectionModel, false) {

            @Override
            protected void setPath(final TreePath path) {
                super.setPath(path);
                activeFrameManager.setActiveFrame(windowFactory.getSwingWindow(
                        ((TreeViewNode) path.getLastPathComponent()).getWindow()));
            }
        };

        activeFrameManager.addSelectionListener(this);

        mainFrame.getRootPane().getActionMap().put("prevFrameAction",
                new PreviousFrameAction(treeScroller));
        mainFrame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                        "prevFrameAction");
        mainFrame.getRootPane().getActionMap().put(
                "nextFrameAction", new NextFrameAction(treeScroller));
        mainFrame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                KeyEvent.CTRL_DOWN_MASK), "nextFrameAction");
    }

    @Handler
    public void windowAdded(final SwingWindowAddedEvent event) {
        final TextFrame parent = event.getParentWindow().orNull();
        final TextFrame window = event.getChildWindow();
        final TreeViewNode parentNode;
        if (parent == null) {
            parentNode = model.getRootNode();
        } else {
            parentNode = nodes.get(parent);
        }

        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                final TreeViewNode node = new TreeViewNode(null, window.getContainer());
                synchronized (nodes) {
                    nodes.put(window, node);
                }
                node.setUserObject(window);
                model.insertNodeInto(node, parentNode);
            }
        });
    }

    @Handler
    public void windowDeleted(final SwingWindowDeletedEvent event) {
        final TextFrame parent = event.getParentWindow().orNull();
        final TextFrame window = event.getChildWindow();
        UIUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                if (nodes.get(window) == null) {
                    return;
                }
                final TreeViewNode node = nodes.get(window);
                if (node.getLevel() == 0) {
                    eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM,
                            new IllegalArgumentException(),
                            "delServer triggered for root node" + node.toString(), ""));
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

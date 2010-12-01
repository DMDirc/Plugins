package com.dmdirc.addons.ui_swing.framemanager.ctrltab;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.SwingWindowListener;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.NextFrameAction;
import com.dmdirc.addons.ui_swing.actions.PreviousFrameAction;
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewModel;
import com.dmdirc.addons.ui_swing.framemanager.tree.TreeViewNode;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.ui.interfaces.Window;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

/**
 *
 */
public class CtrlTabWindowManager implements SwingWindowListener {

    /** Node storage, used for adding and deleting nodes correctly. */
    private final Map<Window, TreeViewNode> nodes;
    /** Data model. */
    private final TreeViewModel model;
    /** Tree Scroller. */
    private final TreeScroller treeScroller;

    public CtrlTabWindowManager(final SwingController controller) {
        nodes = new HashMap<Window, TreeViewNode>();
        model = new TreeViewModel(new TreeViewNode(null, null));
        treeScroller = new TreeScroller(model, new DefaultTreeSelectionModel(),
                false) {

            /** {@inheritDoc} */
            @Override
            protected void setPath(final TreePath path) {
                super.setPath(path);
                ((TreeViewNode) path.getLastPathComponent()).getWindow().
                        activateFrame();
            }
        };

        controller.getWindowFactory().addWindowListener(this);
        controller.getMainFrame().getRootPane().getActionMap().put(
                "prevFrameAction", new PreviousFrameAction(this));
        controller.getMainFrame().getRootPane().getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke
                .getKeyStroke(KeyEvent.VK_TAB,
                KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                "prevFrameAction");
        controller.getMainFrame().getRootPane().getActionMap().put(
                "nextFrameAction", new NextFrameAction(this));
        controller.getMainFrame().getRootPane().getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke
                .getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK),
                "nextFrameAction");
    }

    @Override
    public void windowAdded(final Window parent, final Window window) {
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

    @Override
    public void windowDeleted(final Window parent, final Window window) {
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
}

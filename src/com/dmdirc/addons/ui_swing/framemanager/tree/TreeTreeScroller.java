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
import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;

import javax.swing.tree.TreePath;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extension of TreeScroller to scroll the tree view.
 */
public class TreeTreeScroller extends TreeScroller {

    /** Active frame manager. */
    private final ActiveFrameManager activeFrameManager;
    /** Factory to use to retrieve swing windows. */
    private final SwingWindowFactory windowFactory;

    /**
     * Creates a new Tree scroller for the tree view.
     *
     * @param activeFrameManager The active window manager
     * @param windowFactory      Factory to use to retrieve swing windows.
     * @param tree               Tree view tree
     */
    public TreeTreeScroller(
            final ActiveFrameManager activeFrameManager,
            final SwingWindowFactory windowFactory,
            final Tree tree) {
        super(tree);

        this.activeFrameManager = activeFrameManager;
        this.windowFactory = windowFactory;
    }

    @Override
    protected void setPath(final TreePath path) {
        checkNotNull(path);
        checkNotNull(path.getLastPathComponent());
        checkNotNull(((TreeViewNode) path.getLastPathComponent()).getWindow());
        super.setPath(path);
        activeFrameManager.setActiveFrame(windowFactory.getSwingWindow(
                ((TreeViewNode) path.getLastPathComponent()).getWindow()));
    }

}

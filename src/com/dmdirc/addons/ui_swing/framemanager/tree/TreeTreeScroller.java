/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.addons.ui_swing.components.TreeScroller;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import javax.swing.tree.TreePath;

/**
 * Extention of TreeScroller to scroll the tree view.
 */
public class TreeTreeScroller extends TreeScroller {

    /**
     * Creates a new Tree scroller for the tree view.
     *
     * @param tree Tree view tree
     */
    public TreeTreeScroller(final Tree tree) {
        super(tree);
    }

    /** {@inheritDoc} */
    @Override
    protected void setPath(final TreePath path) {
        if (path == null) {
            Logger.appError(ErrorLevel.HIGH,
                    "Unable to change focus",
                    new IllegalArgumentException("path == null"));
            return;
        }
        if (path.getLastPathComponent() == null) {
            Logger.appError(ErrorLevel.HIGH,
                    "Unable to change focus",
                    new IllegalArgumentException(
                    "Last component == null"));
            return;
        }
        if (((TreeViewNode) path.getLastPathComponent()).getWindow()
                == null) {
            Logger.appError(ErrorLevel.HIGH,
                    "Unable to change focus",
                    new IllegalArgumentException("Frame is null"));
            return;
        }
        super.setPath(path);
        ((TreeViewNode) path.getLastPathComponent()).getWindow().
                activateFrame();
    }
}

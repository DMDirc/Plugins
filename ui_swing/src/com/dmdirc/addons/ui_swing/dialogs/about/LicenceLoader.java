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

package com.dmdirc.addons.ui_swing.dialogs.about;

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.interfaces.ui.AboutDialogModel;
import com.dmdirc.ui.core.about.LicensedComponent;

import java.io.IOException;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Background loader of licences into a list.
 */
public class LicenceLoader extends LoggingSwingWorker<Void, Void> {

    /** Tree to add licenses to. */
    private final JTree tree;
    private final AboutDialogModel model;
    /** Model to load licences into. */
    private final DefaultTreeModel treeModel;

    /**
     * Instantiates a new licence loader.
     *
     * @param tree     Tree to add licenses to
     * @param model    Model to load licences into
     * @param eventBus The event bus to post errors to
     */
    public LicenceLoader(final AboutDialogModel model, final JTree tree,
            final DefaultTreeModel treeModel, final DMDircMBassador eventBus) {
        super(eventBus);
        this.tree = tree;
        this.model = model;
        this.treeModel = treeModel;
    }

    @Override
    protected Void doInBackground() throws IOException {
        model.getLicensedComponents().forEach(this::addLicensedComponent);
        return null;
    }

    private void addLicensedComponent(final LicensedComponent component) {
        final MutableTreeNode componentNode = new DefaultMutableTreeNode(component);
        treeModel.insertNodeInto(componentNode, (MutableTreeNode) treeModel.getRoot(),
                treeModel.getChildCount(treeModel.getRoot()));
        component.getLicences().forEach(l -> treeModel.insertNodeInto(
                new DefaultMutableTreeNode(l), componentNode,
                treeModel.getChildCount(componentNode)));
    }

    @Override
    protected void done() {
        treeModel.nodeStructureChanged((TreeNode) treeModel.getRoot());
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        tree.setSelectionRow(0);
        super.done();
    }
}

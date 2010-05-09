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

import com.dmdirc.serverlists.ServerGroup;
import com.dmdirc.serverlists.ServerGroupItem;
import com.dmdirc.serverlists.ServerList;
import com.dmdirc.util.ListenerList;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Model proxying requests from the core server list model to the swing ui.
 */
public class ServerListModel {

    /** Server list. */
    private final ServerList list = new ServerList();
    /** Listener list. */
    private final ListenerList listeners;
    /** Active server group. */
    private ServerGroup activeGroup;
    /** Active server item. */
    private ServerGroupItem activeItem;

    /**
     * Creates a new server list model.
     */
    public ServerListModel() {
        listeners = new ListenerList();
    }

    /**
     * Returns a populated tree model for this server list model.
     *
     * @return Populated tree model
     */
    public DefaultTreeModel getTreeModel() {
        return populateModel(new DefaultTreeModel(
                new DefaultMutableTreeNode()));
    }

    /**
     * Populates a tree model for this server list model.
     *
     * @param model Un-populated tree model to populate
     *
     * @return Populated tree model
     */
    public DefaultTreeModel populateModel(final DefaultTreeModel model) {
        for (ServerGroup group : list.getServerGroups()) {
            final DefaultMutableTreeNode child = new DefaultMutableTreeNode(
                    group);
            model.insertNodeInto(child, (DefaultMutableTreeNode) model
                    .getRoot(), model.getChildCount(model.getRoot()));
            model.nodeStructureChanged((DefaultMutableTreeNode) model
                    .getRoot());
            addGroups(model, child, group.getItems());
        }
        return model;
    }

    /**
     * Recursively adds groups to the specified tree model.
     *
     * @param model Tree model
     * @param parent Parent node to populate
     * @param items Items to add to parent node
     */
    private void addGroups(final DefaultTreeModel model,
            final DefaultMutableTreeNode parent,
            final List<ServerGroupItem> items) {
        for (ServerGroupItem group : items) {
            final DefaultMutableTreeNode child = new DefaultMutableTreeNode(
                    group);
            model.insertNodeInto(child, parent, model.getChildCount(parent));
            if (group instanceof ServerGroup) {
                addGroups(model, child, ((ServerGroup) group).getItems());
            }
        }
    }

    /**
     * Adds a server list listener to be notified of changes.
     *
     * @param listener Listener to add
     */
    public void addServerListListener(final ServerListListener listener) {
        listeners.add(ServerListListener.class, listener);
    }

    /**
     * Sets the selected item in this model.
     *
     * @param item Newly selected item
     */
    public void setSelectedItem(final ServerGroupItem item) {
        activeGroup = item.getGroup();
        activeItem = item;
        for (ServerListListener listener : listeners.get(
                ServerListListener.class)) {
            listener.serverGroupChanged(item);
        }
    }

    /**
     * Gets the currently selected group.
     *
     * @return Currently selected group
     */
    public ServerGroup getSelectedGroup() {
        return activeGroup;
    }

    /**
     * Gets the currently selected item.
     *
     * @return Currently selected item
     */
    public ServerGroupItem getSelectedItem() {
        return activeItem;
    }

    /**
     * Saves the changes.
     */
    public void saveChanges() {
        //TODO
    }
}

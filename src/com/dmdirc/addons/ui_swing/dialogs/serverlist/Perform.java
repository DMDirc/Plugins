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

import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.serverlists.ServerGroupItem;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Panel to show and edit performs for a server group.
 */
public class Perform extends JPanel implements ServerListListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 2;
    /** Perform list. */
    private final Map<ServerGroupItem, JTextArea> performs =
            new HashMap<ServerGroupItem, JTextArea>();
    /** Perform scroll pane. */
    private final JScrollPane scrollPane;
    /** Server list model. */
    private final ServerListModel model;

    /**
     * Creates a new perform panel backed by the specified model.
     *
     * @param model Backing model
     */
    public Perform(final ServerListModel model) {
        super();

        this.model = model;

        scrollPane = new JScrollPane();
        addListeners();

        scrollPane.setViewportView(getPerfom(model.getSelectedItem()));
        setBorder(BorderFactory.createTitledBorder(UIManager.
                        getBorder("TitledBorder.border"), "Network perform"));
        setLayout(new MigLayout("fill"));
        add(scrollPane, "grow");
    }

    /**
     * Adds required listeners.
     */
    private void addListeners() {
        model.addServerListListener(this);
    }

    /**
     * Gets (and creates if required) the perform for a specified server group
     * item.
     *
     * @param item Server group item to get perform for
     *
     * @return Perform text area for specified server group item
     */
    private JTextArea getPerfom(final ServerGroupItem item) {
        if (!performs.containsKey(item)) {
            final JTextArea text = new JTextArea();
            text.setRows(5);
            PerformWrapper.getPerformWrapper();
            performs.put(item, text);
        }
        return performs.get(item);
    }

    /** {@inheritDoc} */
    @Override
    public void serverGroupChanged(final ServerGroupItem item) {
        scrollPane.setViewportView(getPerfom(item));
    }
}

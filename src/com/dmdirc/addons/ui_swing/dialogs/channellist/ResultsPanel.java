/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.dialogs.channellist;

import com.dmdirc.addons.ui_swing.components.PackingTable;
import com.dmdirc.lists.GroupListManager;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

/**
 * Displays the results of a group list search in a table.
 */
public class ResultsPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Group list manager to perform searches on. */
    private GroupListManager manager;

    /**
     * Creates a new panel to show group list results.
     *
     * @param manager Group manager to show results
     */
    public ResultsPanel(final GroupListManager manager) {
        this.manager = manager;
        layoutComponents();
    }

    /** Lays out the components in the panel. */
    private void layoutComponents() {
        final JScrollPane sp = new JScrollPane();
        final PackingTable table = new PackingTable(
                new ChannelListTableModel(manager), sp);
        sp.setViewportView(table);
        setLayout(new MigLayout("fill, hidemode 3, ins 0"));
        add(sp, "grow, push");
    }
}

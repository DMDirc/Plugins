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

package com.dmdirc.addons.ui_swing.dialogs.channellist;

import com.dmdirc.interfaces.Connection;
import com.dmdirc.lists.GroupListManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Panel to perform and display a group list search.
 */
public class ChannelListPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Search terms. */
    private SearchTermsPanel searchTerms;
    /** Search results. */
    private ResultsPanel results;
    /** Group list manager to show results/perform searching. */
    private GroupListManager manager;

    /**
     * Creates a new panel to perform a group list search on a server.
     *
     * @param connection Server on which to perform search
     * @param total  Label to update with total
     */
    public ChannelListPanel(final Connection connection, final JLabel total) {
        manager = new GroupListManager(connection);
        searchTerms = new SearchTermsPanel(manager);
        results = new ResultsPanel(manager, total);
        layoutComponents();
    }

    /** Lays out the components in the panel. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, hidemode 3, ins 0"));
        add(searchTerms, "growx, wrap");
        add(results, "grow, push");
    }

}

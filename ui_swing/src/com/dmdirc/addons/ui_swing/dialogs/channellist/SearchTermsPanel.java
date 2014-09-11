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

import com.dmdirc.lists.GroupListManager;
import com.dmdirc.lists.GroupListObserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/** Gathers input for a group list search and begins the search. */
public class SearchTermsPanel extends JPanel implements ActionListener,
        GroupListObserver {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Group list manager to perform searches on. */
    private final GroupListManager manager;
    /** Search terms input field. */
    private final JTextField searchTerms;
    /** Search button. */
    private JButton search;

    /**
     * Creates a new panel to gather input for a group list search.
     *
     * @param manager Group list manager to perform search with
     */
    public SearchTermsPanel(final GroupListManager manager) {
        this.manager = manager;
        searchTerms = new JTextField();
        layoutComponents();
        manager.addGroupListObserver(this);
    }

    /** Lays out the components in the panel. */
    private void layoutComponents() {
        search = new JButton("Search");
        search.addActionListener(this);
        searchTerms.addActionListener(this);
        setLayout(new MigLayout("fill, hidemode 3, ins 0"));
        add(new JLabel("Search terms: "), "align label");
        add(searchTerms, "growx, pushx");
        add(search, "");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        manager.startSearch(searchTerms.getText());
        search.setEnabled(false);
    }

    @Override
    public void onGroupListFinished() {
        if (search != null) {
            search.setEnabled(true);
        }
    }

    @Override
    public void onGroupListStarted() {
        // Do nothing
    }

}

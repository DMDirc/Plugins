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

package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.Server;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper.PerformDescription;
import com.dmdirc.actions.wrappers.PerformWrapper.PerformType;
import com.dmdirc.addons.ui_swing.UIUtilities;

import com.dmdirc.addons.ui_swing.components.performpanel.PerformPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

/**
 * Perform panel.
 */
public final class GregsMotherLikesHorses extends JPanel implements ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;

    /** Parent server. */
    private final Server server;

    /** Network/server combo box. */
    private JComboBox target;

    private PerformPanel performPanel;


    /**
     * Creates a new instance of IgnoreList.
     *
     * @param server Parent server
     */
    public GregsMotherLikesHorses(final Server server) {
        super();
        this.server = server;

        this.setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents();
        addListeners();
    }

    /** Initialises the components. */
    private void initComponents() {
        setLayout(new MigLayout("fill"));

        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        target = new JComboBox(model);

        model.addElement("Network perform (" + server.getNetwork()
                + ") Any profile");
        model.addElement("Network perform (" + server.getNetwork()
                + ") This profile (" + server.getProfile().getName() + ")");
        model.addElement("Server perform (" + server.getAddress()
                + ") Any profile");
        model.addElement("Server perform (" + server.getAddress()
                + ") This profile (" + server.getProfile().getName() + ")");

        add(target, "growx, pushx, wrap");

        Collection<PerformDescription> performList = new ArrayList<PerformDescription>();
        PerformDescription defaultPerform = new PerformDescription(PerformType.NETWORK, server.getNetwork());
        performList.add(defaultPerform);
        performList.add(new PerformDescription(PerformType.NETWORK, server.getProfile().getName()));
        performList.add(new PerformDescription(PerformType.SERVER, server.getAddress()));
        performList.add(new PerformDescription(PerformType.SERVER, server.getProfile().getName()));

        performPanel = new PerformPanel(performList);
        performPanel.switchPerform(defaultPerform);
        add(performPanel, "grow, push");

    }

    /** Adds listeners to the components. */
    private void addListeners() {
        target.addActionListener(this);
    }

    /** Saves the performs. */
    public void savePerforms() {
        performPanel.savePerform();
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        //activePerform = target.getSelectedIndex();
    }

}

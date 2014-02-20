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

package com.dmdirc.addons.ui_swing.dialogs.serversetting;

import com.dmdirc.actions.wrappers.PerformType;
import com.dmdirc.actions.wrappers.PerformWrapper;
import com.dmdirc.actions.wrappers.PerformWrapper.PerformDescription;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.performpanel.PerformPanel;
import com.dmdirc.addons.ui_swing.components.renderers.PerformRenderer;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Perform panel.
 */
public class PerformTab extends JPanel implements ActionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Parent connection. */
    private final Connection connection;
    /** Perform wrapper to read/write performs to. */
    private final PerformWrapper wrapper;
    /** Network/server combo box. */
    private JComboBox<PerformDescription> target;
    /** Perform panel. */
    private PerformPanel performPanel;

    /**
     * Creates a new instance of IgnoreList.
     *
     * @param iconManager Icon manager
     * @param config      Config to read settings from
     * @param wrapper     Perform wrapper to read/write performs to.
     * @param connection  Connection whose perform should be displayed.
     */
    public PerformTab(
            final IconManager iconManager,
            final AggregateConfigProvider config,
            final PerformWrapper wrapper,
            final Connection connection) {
        super();

        this.wrapper = wrapper;
        this.connection = connection;

        setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents(iconManager, config);
        addListeners();
    }

    /**
     * Initialises the components.
     *
     * @param iconManager Icon manager
     * @param config      Config to read settings from
     */
    private void initComponents(final IconManager iconManager,
            final AggregateConfigProvider config) {
        setLayout(new MigLayout("fill"));

        final DefaultComboBoxModel<PerformDescription> model = new DefaultComboBoxModel<>();
        target = new JComboBox<>(model);

        add(target, "growx, pushx, wrap");

        final Collection<PerformDescription> performList = new ArrayList<>();

        final PerformDescription networkPerform = new PerformDescription(
                PerformType.NETWORK, connection.getNetwork());
        final PerformDescription networkProfilePerform = new PerformDescription(
                PerformType.NETWORK, connection.getNetwork(), connection.getProfile().getName());
        final PerformDescription serverPerform = new PerformDescription(
                PerformType.SERVER, connection.getAddress());
        final PerformDescription serverProfilePerform = new PerformDescription(
                PerformType.SERVER, connection.getAddress(), connection.getProfile().getName());

        model.addElement(networkPerform);
        model.addElement(networkProfilePerform);
        model.addElement(serverPerform);
        model.addElement(serverProfilePerform);

        target.setRenderer(new PerformRenderer(target.getRenderer()));

        performList.add(networkPerform);
        performList.add(networkProfilePerform);
        performList.add(serverPerform);
        performList.add(serverProfilePerform);

        performPanel = new PerformPanel(iconManager, config, wrapper, performList);
        performPanel.switchPerform(networkPerform);
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
        final PerformDescription perform = (PerformDescription) ((JComboBox) e.getSource()).
                getSelectedItem();
        performPanel.switchPerform(perform);
    }

}

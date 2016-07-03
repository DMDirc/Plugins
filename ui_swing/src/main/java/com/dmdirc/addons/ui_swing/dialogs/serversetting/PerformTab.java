/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.performpanel.PerformPanel;
import com.dmdirc.addons.ui_swing.components.renderers.PerformRenderer;
import com.dmdirc.commandparser.auto.AutoCommand;
import com.dmdirc.commandparser.auto.AutoCommandManager;
import com.dmdirc.interfaces.Connection;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.addons.ui_swing.components.IconManager;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

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
    /** Command manager wrapper to read/write performs to. */
    private final AutoCommandManager autoCommandManager;
    /** Network/server combo box. */
    private JComboBox<AutoCommand> target;
    /** Perform panel. */
    private PerformPanel performPanel;

    public PerformTab(
            final IconManager iconManager,
            final ColourManagerFactory colourManagerFactory,
            final AggregateConfigProvider config,
            final AutoCommandManager autoCommandManager,
            final Connection connection) {
        this.autoCommandManager = autoCommandManager;
        this.connection = connection;

        setOpaque(UIUtilities.getTabbedPaneOpaque());
        initComponents(iconManager, colourManagerFactory, config);
        addListeners();
    }

    /**
     * Initialises the components.
     *
     * @param iconManager Icon manager
     * @param config      Config to read settings from
     */
    private void initComponents(final IconManager iconManager,
            final ColourManagerFactory colourManagerFactory,
            final AggregateConfigProvider config) {
        setLayout(new MigLayout("fill"));

        final DefaultComboBoxModel<AutoCommand> model = new DefaultComboBoxModel<>();
        target = new JComboBox<>(model);

        add(target, "growx, pushx, wrap");

        final Collection<AutoCommand> performList = new ArrayList<>();

        final AutoCommand networkPerform = autoCommandManager.getOrCreateAutoCommand(
                Optional.of(connection.getNetwork()), Optional.empty(), Optional.empty());
        final AutoCommand networkProfilePerform = autoCommandManager.getOrCreateAutoCommand(
                Optional.of(connection.getNetwork()), Optional.empty(),
                Optional.of(connection.getProfile().getName()));
        final AutoCommand serverPerform = autoCommandManager.getOrCreateAutoCommand(
                Optional.empty(), Optional.of(connection.getAddress()), Optional.empty());
        final AutoCommand serverProfilePerform = autoCommandManager.getOrCreateAutoCommand(
                Optional.empty(), Optional.of(connection.getAddress()),
                Optional.of(connection.getProfile().getName()));

        model.addElement(networkPerform);
        model.addElement(networkProfilePerform);
        model.addElement(serverPerform);
        model.addElement(serverProfilePerform);

        target.setRenderer(new PerformRenderer(target.getRenderer()));

        performList.add(networkPerform);
        performList.add(networkProfilePerform);
        performList.add(serverPerform);
        performList.add(serverProfilePerform);

        performPanel = new PerformPanel(iconManager, colourManagerFactory, config,
                autoCommandManager, performList);
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

    @Override
    public void actionPerformed(final ActionEvent e) {
        final AutoCommand perform = (AutoCommand) ((JComboBox<?>) e.getSource()).
                getSelectedItem();
        performPanel.switchPerform(perform);
    }

}

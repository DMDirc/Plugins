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

package com.dmdirc.addons.serverlistdialog;

import com.dmdirc.addons.serverlists.ServerGroupItem;
import com.dmdirc.addons.ui_swing.components.vetoable.VetoableComboBoxModel;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Profile selection field for an associated server group item.
 */
public class Profiles extends JPanel implements ServerListListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** Server list model. */
    private final ServerListModel model;
    /** Combo boxes. */
    private final Map<ServerGroupItem, JComboBox<ConfigProvider>> combos = new HashMap<>();
    /** Info label. */
    private final JLabel label;
    /** Controller to use to get profiles. */
    private final IdentityController identityController;

    /**
     * Creates a new profile panel backed by the specified model.
     *
     * @param model              Backing server list model
     * @param identityController Controller to use to get profiles.
     */
    public Profiles(final ServerListModel model, final IdentityController identityController) {

        this.model = model;
        this.identityController = identityController;

        label = new JLabel("Use this profile on this network: ");
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Default profile"));
        addListeners();

        setLayout(new MigLayout("fill"));
        add(label);
        add(getComboBox(model.getSelectedItem()), "grow, push");
    }

    /**
     * Adds required listeners.
     */
    private void addListeners() {
        model.addServerListListener(this);
    }

    @Override
    public void serverGroupChanged(final ServerGroupItem item) {
        setVisible(false);
        removeAll();
        add(label);
        add(getComboBox(item), "grow, push");
        setVisible(true);
    }

    /**
     * Gets or creates a combo box for selecting performs for the specified server group item.
     *
     * @param item Server group item requiring the profile selection
     *
     * @return The server group item's associated profile selection box
     */
    private JComboBox<ConfigProvider> getComboBox(final ServerGroupItem item) {
        if (!combos.containsKey(item)) {
            final DefaultComboBoxModel<ConfigProvider> comboModel = new VetoableComboBoxModel<>();

            ConfigProvider selectedItem = null;
            comboModel.addElement(null);
            for (ConfigProvider profile : identityController.getProvidersByType("profile")) {
                comboModel.addElement(profile);
                if (item != null && profile.getName().equals(
                        item.getProfile())) {
                    selectedItem = profile;
                }
            }
            comboModel.setSelectedItem(selectedItem);
            combos.put(item, new JComboBox<>(comboModel));
        }
        return combos.get(item);
    }

    @Override
    public void dialogClosed(final boolean save) {
        if (save) {
            for (Entry<ServerGroupItem, JComboBox<ConfigProvider>> entry : combos.entrySet()) {
                if (entry.getKey() != null) {
                    if (entry.getValue().getSelectedItem() == null) {
                        entry.getKey().setProfile(null);
                    } else {
                        entry.getKey().setProfile(((ConfigProvider) entry.getValue().
                                getSelectedItem()).getName());
                    }
                }
            }
        }
    }

    @Override
    public void serverGroupAdded(final ServerGroupItem parent,
            final ServerGroupItem group) {
        //Ignore
    }

    @Override
    public void serverGroupRemoved(final ServerGroupItem parent,
            final ServerGroupItem group) {
        //Ignore
    }

}
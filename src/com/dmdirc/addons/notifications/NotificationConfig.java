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

package com.dmdirc.addons.notifications;

import com.dmdirc.addons.ui_swing.components.reorderablelist.ListReorderButtonPanel;
import com.dmdirc.addons.ui_swing.components.reorderablelist.ReorderableJList;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.interfaces.config.ConfigProvider;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Notification method configuration panel.
 */
public class NotificationConfig extends JPanel implements PreferencesInterface {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Notification method order list. */
    private ReorderableJList<String> list;
    /** Notification methods. */
    private final List<String> methods;
    /** User settings config to save to. */
    private final ConfigProvider userSettings;
    /** This plugin's settings domain. */
    private final String domain;

    /**
     * Creates a new instance of NotificationConfig panel.
     *
     * @param userSettings Config to save settings to
     * @param domain       This plugin's settings domain
     * @param methods      A list of methods to be used in the panel
     */
    public NotificationConfig(
            final ConfigProvider userSettings,
            final String domain,
            final List<String> methods) {
        super();

        if (methods == null) {
            this.methods = new LinkedList<>();
        } else {
            this.methods = new LinkedList<>(methods);
        }
        this.userSettings = userSettings;
        this.domain = domain;

        initComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        list = new ReorderableJList<>();

        for (String method : methods) {
            list.getModel().addElement(method);
        }

        setLayout(new MigLayout("fillx, ins 0"));

        JPanel panel = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Source order"));
        panel.setLayout(new MigLayout("fillx, ins 5"));

        panel.add(new JLabel("Drag and drop items to reorder"), "wrap");
        panel.add(new JScrollPane(list), "growx, pushx");
        panel.add(new ListReorderButtonPanel<>(list), "");

        add(panel, "growx, wrap");

        panel = new JPanel();

        panel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Output format"));
        panel.setLayout(new MigLayout("fillx, ins 5"));

        add(panel, "growx, wrap");
    }

    /**
     * Retrieves the (new) notification method order from this config panel.
     *
     * @return An ordered list of methods
     */
    public List<String> getMethods() {
        final List<String> newMethods = new LinkedList<>();

        final Enumeration<?> values = list.getModel().elements();

        while (values.hasMoreElements()) {
            newMethods.add((String) values.nextElement());
        }

        return newMethods;
    }

    @Override
    public void save() {
        userSettings.setOption(domain, "methodOrder", getMethods());
    }

}

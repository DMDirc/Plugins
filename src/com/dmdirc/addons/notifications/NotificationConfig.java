/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Notification method order list. */
    private ReorderableJList list;
    /** Notification methods. */
    private final List<String> methods;
    /** The plugin that owns this panel. */
    private final NotificationsPlugin plugin;

    /**
     * Creates a new instance of NotificationConfig panel.
     *
     * @param plugin The plugin that owns this panel
     * @param methods A list of methods to be used in the panel
     */
    public NotificationConfig(final NotificationsPlugin plugin,
            final List<String> methods) {
        super();

        if (methods == null) {
            this.methods = new LinkedList<String>();
        } else {
            this.methods = new LinkedList<String>(methods);
        }
        this.plugin = plugin;

        initComponents();
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        list = new ReorderableJList();

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
        panel.add(new ListReorderButtonPanel(list), "");

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
        final List<String> newMethods = new LinkedList<String>();

        final Enumeration<?> values = list.getModel().elements();

        while (values.hasMoreElements()) {
            newMethods.add((String) values.nextElement());
        }

        return newMethods;
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        plugin.saveSettings(getMethods());
    }
}

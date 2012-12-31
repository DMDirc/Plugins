/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.components.addonpanel;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.addonbrowser.BrowserWindow;
import com.dmdirc.addons.ui_swing.components.renderers.AddonCellRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.ui.IconManager;

import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

/**
 * Addon panel, base class for displaying and managing addons.
 */
public abstract class AddonPanel extends JPanel implements AddonToggleListener,
        ListSelectionListener, PreferencesInterface, HyperlinkListener {

    /** List of addons. */
    protected JTable addonList;
    /** Swing Controller. */
    protected final SwingController controller;
    /** Serial version UID. */
    private static final long serialVersionUID = 3;
    /** Parent Window. */
    private final Window parentWindow;
    /** The icon manager used to retrieve icons. */
    private final IconManager iconManager;
    /** Addon list scroll pane. */
    private JScrollPane scrollPane;
    /** Blurb label. */
    private TextLabel blurbLabel;
    /** Get more info link. */
    private TextLabel getMoreLabel;
    /** Addon info panel. */
    private AddonInfoPanel addonInfo;
    /** Selected addon. */
    private int selectedAddon = -1;

    /**
     * Creates a new instance of AddonPanel
     *
     * @param parentWindow Parent window
     * @param controller Swing Controller
     */
    public AddonPanel(final Window parentWindow,
            final SwingController controller) {
        super();

        this.parentWindow = parentWindow;
        this.controller = controller;
        iconManager = controller.getIconManager();

        initComponents();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        addonList = new JTable(new DefaultTableModel(
                new Object[]{"Addon", }, 0)) {

            /** Serial Version UID. */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }

        };
        addonList.setDefaultRenderer(Object.class,
                new AddonCellRenderer());
        addonList.setTableHeader(null);
        addonList.setShowGrid(false);
        addonList.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        scrollPane = new JScrollPane(new JLabel("Loading " + getTypeName()
                + "..."));
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        blurbLabel = new TextLabel(getTypeName().substring(0, 1).toUpperCase()
                + getTypeName().substring(1) + " allow you to extend the "
                + "functionality of DMDirc.");
        getMoreLabel = new TextLabel(
                "<a href=\"http://addons.dmdirc.com\">Get more addons</a>");
        getMoreLabel.addHyperlinkListener(this);
        addonInfo = new AddonInfoPanel();
        addonInfo.addListener(this);

        /** {@inheritDoc}. */
        new LoggingSwingWorker<Object, Object>() {

            /** {@inheritDoc}. */
            @Override
            protected Object doInBackground() {
                return populateList(addonList);
            }

            /** {@inheritDoc}. */
            @Override
            protected void done() {
                super.done();
                scrollPane.setViewportView(addonList);
                UIUtilities.invokeLater(new Runnable() {

                    /** {@inheritDoc}. */
                    @Override
                    public void run() {
                        addonList.getSelectionModel()
                                .addListSelectionListener(AddonPanel.this);
                        addonList.getSelectionModel()
                                .setSelectionInterval(0, 0);
                    }
                });
            }
        }.executeInExecutor();
    }

    /** Lays out the dialog. */
    private void layoutComponents() {
        if (controller == null) {
            setLayout(new MigLayout("ins 0, fill, hmax " + 500));
        } else {
            setLayout(new MigLayout("ins 0, fill, hmax "
                    + controller.getPrefsDialog().getPanelHeight()));
        }

        add(blurbLabel, "wrap 5, growx, pushx");
        add(getMoreLabel, "wrap 5, right");
        add(scrollPane, "wrap 5, grow, push");
        add(addonInfo, "grow, push");
    }

    /**
     * Populates the addon list returning it when complete.
     *
     * @param table Table to be populated
     *
     * @return Populated table
     */
    protected abstract JTable populateList(final JTable table);

    /**
     * Returns the name of the type of addon being handled.
     *
     * @return Addon type name
     */
    protected abstract String getTypeName();

    /** {@inheritDoc}. */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        final int newSelection = addonList.getSelectedRow();
        if (newSelection == -1) {
            addonList.getSelectionModel().setSelectionInterval(0, selectedAddon);
        } else if (addonList.getModel().getRowCount() > newSelection) {
            addonInfo.setAddonToggle((AddonToggle) ((AddonCell) addonList
                    .getModel().getValueAt(newSelection, 0)).getObject());
        }
        selectedAddon = addonList.getSelectedRow();
    }

    /** {@inheritDoc}. */
    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
            new BrowserWindow(controller, parentWindow);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        if (addonList.getRowCount() == 0) {
            return;
        }
        for (int i = 0; i < addonList.getRowCount(); i++) {
            addonList.getModel().getColumnCount();
            ((AddonToggle) ((AddonCell) addonList.getModel()
                    .getValueAt(i, 0)).getObject()).apply();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addonToggled() {
        addonList.repaint();
    }

    /**
     * Returns the icon manager for this panel.
     *
     * @return Icon manager instance
     */
    public IconManager getIconManager() {
        return iconManager;
    }
}

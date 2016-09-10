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

package com.dmdirc.addons.ui_swing.components.addonpanel;

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.addonbrowser.BrowserWindow;
import com.dmdirc.addons.ui_swing.components.addonbrowser.DataLoaderWorkerFactory;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.config.prefs.PreferencesInterface;
import com.dmdirc.interfaces.EventBus;

import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
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

    /** Serial version UID. */
    private static final long serialVersionUID = 3;
    /** List of addons. */
    protected JTable addonList;
    /** Parent Window. */
    private final Window parentWindow;
    /** The factory to use to produce data loader workers. */
    private final DataLoaderWorkerFactory workerFactory;
    /** The event bus to post errors to. */
    private final EventBus eventBus;
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
     * @param parentWindow  Parent window
     * @param workerFactory The factory to use to produce data loader workers.
     * @param eventBus      The event bus to post errors to.
     */
    public AddonPanel(final Window parentWindow, final DataLoaderWorkerFactory workerFactory,
            final EventBus eventBus) {
        this.parentWindow = parentWindow;
        this.workerFactory = workerFactory;
        this.eventBus = eventBus;

        initComponents();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        addonList = new JTable(new DefaultTableModel(
                new Object[]{"Addon",}, 0)) {
                    /** Serial Version UID. */
                    private static final long serialVersionUID = 1;

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
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        blurbLabel = new TextLabel(getTypeName().substring(0, 1).toUpperCase()
                + getTypeName().substring(1) + " allow you to extend the "
                + "functionality of DMDirc.");
        getMoreLabel = new TextLabel(
                "<a href=\"https://addons.dmdirc.com\">Get more addons</a>");
        getMoreLabel.addHyperlinkListener(this);
        addonInfo = new AddonInfoPanel();
        addonInfo.addListener(this);
    }

    /**
     * Populates the list in a background thread.
     */
    protected void load() {
        UIUtilities.invokeOffEDT(() -> populateList(addonList),
                value -> {
                    scrollPane.setViewportView(addonList);
                    addonList.getSelectionModel().addListSelectionListener(this);
                    addonList.getSelectionModel().setSelectionInterval(0, 0);
                });
    }

    /** Lays out the dialog. */
    private void layoutComponents() {
        setLayout(new MigLayout("ins 0, fill, hmax 500"));
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

    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() == EventType.ACTIVATED) {
            new BrowserWindow(workerFactory, parentWindow);
        }
    }

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

    @Override
    public void addonToggled() {
        addonList.repaint();
    }

}

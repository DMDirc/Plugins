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

package com.dmdirc.addons.ui_swing.dialogs.prefs;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.ListScroller;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.addonpanel.PluginPanel;
import com.dmdirc.addons.ui_swing.components.addonpanel.ThemePanel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to modify global client preferences.
 */
public final class SwingPreferencesDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 9;
    /** Preferences tab list, used to switch option types. */
    private JList tabList;
    /** Main panel. */
    private CategoryPanel mainPanel;
    /** Previously selected category. */
    private PreferencesCategory selected;
    /** Preferences Manager. */
    private PreferencesDialogModel manager;
    /** Manager loading swing worker. */
    private LoggingSwingWorker<PreferencesDialogModel, Void> worker;
    /** Panel size. */
    private int panelSize = 500;
    /** Swing controller. */
    @Deprecated
    private final SwingController controller;

    /**
     * Creates a new instance of SwingPreferencesDialog.
     *
     * @param controller The controller which owns this preferences window.
     */
    public SwingPreferencesDialog(final SwingController controller) {
        super(controller, ModalityType.MODELESS);

        this.controller = controller;

        initComponents();

        worker = new LoggingSwingWorker<PreferencesDialogModel, Void>() {

            /** {@inheritDoc} */
            @Override
            protected PreferencesDialogModel doInBackground() {
                mainPanel.setWaiting(true);
                PreferencesDialogModel prefsManager = null;
                try {
                    prefsManager = new PreferencesDialogModel(
                            new PluginPanel(controller.getMainFrame(), controller),
                            new ThemePanel(controller.getMainFrame(), controller, controller.getThemeManager()),
                            new UpdateConfigPanel(controller),
                            new URLConfigPanel(controller, controller.getMainFrame()),
                            controller.getGlobalConfig(),
                            controller.getGlobalIdentity(),
                            controller.getActionManager(),
                            controller.getPluginManager());
                } catch (IllegalArgumentException ex) {
                    mainPanel.setError(ex.getMessage());
                    Logger.appError(ErrorLevel.HIGH, "Unable to load the" +
                            "preferences dialog.", ex);
                }
                return prefsManager;
            }

            /** {@inheritDoc} */
            @Override
            protected void done() {
                if (!isCancelled()) {
                    try {
                        final PreferencesDialogModel prefsManager = get();
                        if (prefsManager != null) {
                            setPrefsManager(prefsManager);
                        }
                    } catch (InterruptedException ex) {
                        //Ignore
                    } catch (ExecutionException ex) {
                        Logger.appError(ErrorLevel.MEDIUM, ex.getMessage(), ex);
                    }
                }
            }
        };
        worker.executeInExecutor();
    }

    private void setPrefsManager(final PreferencesDialogModel manager) {
        this.manager = manager;

        ((DefaultListModel) tabList.getModel()).clear();
        mainPanel.setCategory(null);

        final int count = countCategories(manager.getCategories());
        tabList.setCellRenderer(new PreferencesListCellRenderer(
                controller.getIconManager(), count));

        addCategories(manager.getCategories());
    }

    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        mainPanel = new CategoryPanel(controller.getPrefsComponentFactory(),
                controller.getIconManager(), this);

        tabList = new JList(new DefaultListModel());
        tabList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabList.addListSelectionListener(this);
        ListScroller.register(tabList);
        final JScrollPane tabListScrollPane = new JScrollPane(tabList);
        tabListScrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Preferences");
        setResizable(false);

        tabList.setBorder(BorderFactory.createEtchedBorder());

        orderButtons(new JButton(), new JButton());

        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        final MigLayout layout = new MigLayout("pack, hmin min(80sp, 700), " +
                "hmax min(700, 80sp)");
        setLayout(layout);
        add(tabListScrollPane, "w 150!, growy, pushy");
        add(mainPanel, "wrap, w 480!, pushy, growy, pushy");
        add(getLeftButton(), "span, split, right");
        add(getRightButton(), "right");
    }

    /**
     * Adds the categories from the preferences manager, clearing existing
     * categories first.
     */
    private void addCategories(final List<PreferencesCategory> categories) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                tabList.removeListSelectionListener(SwingPreferencesDialog.this);
                for (PreferencesCategory category : categories) {
                    if (!category.isInline()) {
                        ((DefaultListModel) tabList.getModel()).addElement(
                                category);
                    }
                    addCategories(category.getSubcats());
                }
                tabList.addListSelectionListener(SwingPreferencesDialog.this);
                restoreActiveCategory();
            }
        });
        mainPanel.setWaiting(false);
    }

    /**
     * Counts the number of categories that will be displayed in the list panel.
     *
     * @param categories The collection of categories to inspect
     * @return The number of those categories (including children) that will be displayed
     * @since 0.6.3m1rc3
     */
    protected int countCategories(
            final Collection<PreferencesCategory> categories) {
        int count = 0;

        for (PreferencesCategory cat : categories) {
            if (!cat.isInline()) {
                count += 1 + countCategories(cat.getSubcats());
            }
        }

        return count;
    }

    /**
     * Handles the actions for the dialog.
     *
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (selected != null) {
            selected.fireCategoryDeselected();
            selected = null;
        }
        mainPanel.setCategory(null);

        if (actionEvent != null && getOkButton().equals(actionEvent.getSource())) {
            if (tabList.getSelectedIndex() > -1) {
                final PreferencesCategory node = (PreferencesCategory) tabList.
                        getSelectedValue();
                controller.getGlobalIdentity().setOption("dialogstate",
                        "preferences", node.getPath());
            }
            saveOptions();
        }

        new LoggingSwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                if (manager != null) {
                    manager.dismiss();
                }
                return null;
            }
        }.executeInExecutor();
        dispose();
    }

    /**
     * {@inheritDoc}
     *
     * @param e List selection event
     * @since 0.6.3m1
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            PreferencesCategory node = null;
            try {
                node = (PreferencesCategory) tabList.getSelectedValue();
            } catch (ArrayIndexOutOfBoundsException ex) {
                //I hate the JVM
            }
            if (node == null) {
                tabList.setSelectedValue(selected, true);
                return;
            }

            if (node == selected) {
                return;
            }

            if (selected != null) {
                selected.fireCategoryDeselected();
            }
            final int index = tabList.getSelectedIndex();
            tabList.scrollRectToVisible(tabList.getCellBounds(index, index));
            selected = node;
            if (selected != null) {
                selected.fireCategorySelected();
            }
            mainPanel.setCategory(selected);
        }
    }

    /**
     * Returns the selected category.
     *
     * @return Selected category
     */
    protected PreferencesCategory getSelectedCategory() {
        return selected;
    }

    /** {@inheritDoc} */
    public void saveOptions() {
        if (manager != null && manager.save()) {
            dispose();
            controller.showDialog(SwingRestartDialog.class,
                    ModalityType.APPLICATION_MODAL, "apply settings");
        }
    }

    private void restoreActiveCategory() {
        final String oldCategoryPath = controller.getGlobalConfig().
                getOption("dialogstate", "preferences");
        final DefaultListModel model = (DefaultListModel) tabList.getModel();
        int indexToSelect = 0;
        for (int i = 0; i < model.getSize(); i++) {
            final PreferencesCategory category =
                    (PreferencesCategory) model.get(i);
            if (oldCategoryPath.equals(category.getPath())) {
                indexToSelect = i;
                break;
            }
        }
        tabList.setSelectedIndex(indexToSelect);
    }

    /**
     * Gets the maximum panel size.
     *
     * @return Max panel size
     */
    public int getPanelHeight() {
        return panelSize;
    }

    /**
     * Sets the panel size to the specified value.
     *
     * @param panelSize New panel size
     */
    protected void setPanelHeight(final int panelSize) {
        this.panelSize = panelSize;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        synchronized (SwingPreferencesDialog.this) {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
            if (manager != null) {
                manager.close();
            }
            super.dispose();
        }
    }
}

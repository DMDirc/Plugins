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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.ListScroller;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.addons.ui_swing.injection.DialogModule.ForSettings;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.config.prefs.PreferencesDialogModel;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.ui.IconManager;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Provider;
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

import com.dmdirc.DMDircMBassador;

/**
 * Allows the user to modify global client preferences.
 */
public final class SwingPreferencesDialog extends StandardDialog implements
        ActionListener, ListSelectionListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 9;
    /** Preferences tab list, used to switch option types. */
    private JList<PreferencesCategory> tabList;
    /** Main panel. */
    private CategoryPanel mainPanel;
    /** Previously selected category. */
    private PreferencesCategory selected;
    /** Preferences Manager. */
    private PreferencesDialogModel manager;
    /** Manager loading swing worker. */
    private final LoggingSwingWorker<PreferencesDialogModel, Void> worker;
    /** The provider to use for restart dialogs. */
    private final DialogProvider<SwingRestartDialog> restartDialogProvider;
    /** The provider to use to produce a category panel. */
    private final Provider<CategoryPanel> categoryPanelProvider;
    /** Icon manager to retrieve icons from. */
    private final IconManager iconManager;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of SwingPreferencesDialog.
     *
     * @param parentWindow          Main window to parent dialogs on.
     * @param iconManager           Icon manager used to retrieve images
     * @param restartDialogProvider The provider to use for restart dialogs.
     * @param dialogModelProvider   The provider to use to get a dialog model.
     * @param categoryPanelProvider The provider to use to produce a category panel.
     * @param eventBus              The event bus to post errors to.
     */
    @Inject
    public SwingPreferencesDialog(
            @MainWindow final Window parentWindow,
            @GlobalConfig final IconManager iconManager,
            @ForSettings final DialogProvider<SwingRestartDialog> restartDialogProvider,
            final Provider<PreferencesDialogModel> dialogModelProvider,
            final Provider<CategoryPanel> categoryPanelProvider,
            final DMDircMBassador eventBus) {
        super(parentWindow, ModalityType.MODELESS);

        this.iconManager = iconManager;
        this.restartDialogProvider = restartDialogProvider;
        this.categoryPanelProvider = categoryPanelProvider;
        this.eventBus = eventBus;

        initComponents();

        worker = new LoggingSwingWorker<PreferencesDialogModel, Void>(eventBus) {

            @Override
            protected PreferencesDialogModel doInBackground() {
                mainPanel.setWaiting(true);
                PreferencesDialogModel prefsManager = null;
                try {
                    prefsManager = dialogModelProvider.get();
                } catch (IllegalArgumentException ex) {
                    mainPanel.setError(ex.getMessage());
                    eventBus.publishAsync(new UserErrorEvent(ErrorLevel.HIGH, ex,
                            "Unable to load the preferences dialog", ""));
                }
                return prefsManager;
            }

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
                        eventBus.publishAsync(new UserErrorEvent(ErrorLevel.MEDIUM, ex, ex.getMessage(), ""));
                    }
                }
            }
        };
        worker.execute();
    }

    private void setPrefsManager(final PreferencesDialogModel manager) {
        this.manager = manager;

        ((DefaultListModel) tabList.getModel()).clear();
        mainPanel.setCategory(null);

        final int count = countCategories(manager.getCategories());
        tabList.setCellRenderer(new PreferencesListCellRenderer(iconManager, eventBus, count));

        addCategories(manager.getCategories());
    }

    /**
     * Initialises GUI components.
     */
    private void initComponents() {
        mainPanel = categoryPanelProvider.get();

        tabList = new JList<>(new DefaultListModel<PreferencesCategory>());
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

        final MigLayout layout
                = new MigLayout("pack, hmin min(80sp, 700), " + "hmax min(700, 80sp)");
        setLayout(layout);
        add(tabListScrollPane, "w 150!, growy, pushy");
        add(mainPanel, "wrap, w 480!, pushy, growy, pushy");
        add(getLeftButton(), "span, split, right");
        add(getRightButton(), "right");
    }

    /**
     * Adds the categories from the preferences manager, clearing existing categories first.
     */
    private void addCategories(final List<PreferencesCategory> categories) {
        UIUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tabList.removeListSelectionListener(SwingPreferencesDialog.this);
                for (PreferencesCategory category : categories) {
                    if (!category.isInline()) {
                        ((DefaultListModel<PreferencesCategory>) tabList.getModel()).addElement(
                                category);
                    }
                    addCategories(category.getSubcats());
                }
                tabList.addListSelectionListener(SwingPreferencesDialog.this);
                tabList.setSelectedIndex(0);
            }
        });
        mainPanel.setWaiting(false);
    }

    /**
     * Counts the number of categories that will be displayed in the list panel.
     *
     * @param categories The collection of categories to inspect
     *
     * @return The number of those categories (including children) that will be displayed
     *
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

    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (selected != null) {
            selected.fireCategoryDeselected();
            selected = null;
        }
        mainPanel.setCategory(null);

        if (actionEvent != null && getOkButton().equals(actionEvent.getSource())) {
            saveOptions();
        }

        new LoggingSwingWorker<Void, Void>(eventBus) {
            @Override
            protected Void doInBackground() {
                if (manager != null) {
                    manager.dismiss();
                }
                return null;
            }
        }.execute();
        dispose();
    }

    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            PreferencesCategory node = null;
            try {
                node = tabList.getSelectedValue();
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
            selected.fireCategorySelected();
            mainPanel.setCategory(selected);
        }
    }

    public void saveOptions() {
        if (manager != null && manager.save()) {
            dispose();
            restartDialogProvider.displayOrRequestFocus();
        }
    }

    @Override
    public void dispose() {
        synchronized (SwingPreferencesDialog.this) {
            if (!worker.isDone()) {
                worker.cancel(true);
            }
            if (manager != null) {
                manager.close();
            }
            super.dispose();
        }
    }

}

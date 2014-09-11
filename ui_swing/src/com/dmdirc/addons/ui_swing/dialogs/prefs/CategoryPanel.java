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
import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
import com.dmdirc.addons.ui_swing.components.TitlePanel;
import com.dmdirc.addons.ui_swing.components.ToolTipPanel;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.config.prefs.PreferencesCategory;
import com.dmdirc.ui.IconManager;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Panel representing a preferences category.
 */
public class CategoryPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = -3268284364607758509L;
    /** Active preferences category. */
    private PreferencesCategory category;
    /** Title label. */
    private final TitlePanel title;
    /** Tooltip display area. */
    private final ToolTipPanel tooltip;
    /** Contents Panel. */
    private final JScrollPane scrollPane;
    /** Loading panel. */
    private final JPanel loading;
    /** Loading panel. */
    private final JPanel nullCategory;
    /** Loading panel. */
    private final JPanel waitingCategory;
    /** Category panel map. */
    private final Map<PreferencesCategory, JPanel> panels;
    /** Category loading swing worker. */
    private LoggingSwingWorker<JPanel, Object> worker;
    /** Prefs component factory. */
    private final PrefsComponentFactory factory;
    /** The event bus to post errors to. */
    private final DMDircMBassador eventBus;

    /**
     * Instantiates a new category panel.
     *
     * @param eventBus    The event bus to post errors to
     * @param factory     Prefs component factory instance
     * @param iconManager Icon manager
     */
    @Inject
    public CategoryPanel(
            final DMDircMBassador eventBus,
            final PrefsComponentFactory factory,
            @GlobalConfig final IconManager iconManager) {
        this(eventBus, factory, iconManager, null);
    }

    /**
     * Instantiates a new category panel.
     *
     * @param eventBus    The event bus to post errors to
     * @param factory     Prefs component factory instance
     * @param iconManager Icon manager
     * @param category    Initial category
     */
    public CategoryPanel(
            final DMDircMBassador eventBus, final PrefsComponentFactory factory,
            final IconManager iconManager,
            final PreferencesCategory category) {
        super(new MigLayout("fillx, wrap, ins 0"));
        this.factory = factory;
        this.eventBus = eventBus;

        panels = Collections.synchronizedMap(new HashMap<PreferencesCategory, JPanel>());

        loading = new JPanel(new MigLayout("fillx"));
        loading.add(new TextLabel("Loading..."));

        nullCategory = new JPanel(new MigLayout("fillx"));
        nullCategory.add(new TextLabel("Please select a category."));

        waitingCategory = new JPanel(new MigLayout("fillx"));
        waitingCategory.add(new TextLabel("Please wait, loading..."));

        scrollPane = new JScrollPane(loading);
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);

        title = new TitlePanel(BorderFactory.createEtchedBorder(),
                "Preferences");
        tooltip = new ToolTipPanel(iconManager,
                "Hover over a setting to see a description, if available.");

        add(title, "pushx, growx, h 45!");
        add(scrollPane, "grow, push");
        add(tooltip, "pushx, growx, h 70!");

        panels.put(null, loading);
        setCategory(category);
    }

    /**
     * Returns the tooltip panel for this category panel.
     *
     * @return Tooltip panel
     */
    protected ToolTipPanel getToolTipPanel() {
        return tooltip;
    }

    /**
     * Informs the category panel a category has been loaded.
     *
     * @param loader   Category loader
     * @param category Loaded category
     */
    protected void categoryLoaded(final PrefsCategoryLoader loader,
            final PreferencesCategory category) {
        panels.put(category, loader.getPanel());
        categoryLoaded(category);
    }

    private void categoryLoaded(final PreferencesCategory category) {
        if (this.category == category) {
            UIUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    final JPanel panel = panels.get(category);
                    scrollPane.setViewportView(panel);
                    //Hack around mig bug
                    panel.invalidate();
                    panel.validate();
                    for (final Component component : panel.getComponents()) {
                        if (component instanceof JPanel) {
                            component.invalidate();
                            component.validate();
                        }
                    }
                    //And for good measure, hack the crap out of it some more :(
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            panel.invalidate();
                            panel.validate();
                            for (final Component component : panel.getComponents()) {
                                if (component instanceof JPanel) {
                                    component.invalidate();
                                    component.validate();
                                }
                            }
                        }
                    });
                    if (category == null) {
                        title.setText("Preferences");
                    } else {
                        title.setText(category.getPath());
                    }
                }
            });
        }
    }

    /**
     * Sets the new active category for this panel and relays out.
     *
     * @param category New Category
     */
    public void setCategory(final PreferencesCategory category) {
        this.category = category;

        if (category == null) {
            tooltip.setWarning(null);
        } else {
            tooltip.setWarning(category.getWarning());
        }

        if (panels.containsKey(category)) {
            categoryLoaded(category);
        } else {
            UIUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    scrollPane.setViewportView(loading);
                }
            });

            worker = new PrefsCategoryLoader(factory, eventBus, this, category);
            worker.execute();
        }
    }

    /**
     * Sets this panel to a waiting to load state.
     *
     * @param b Loading state
     */
    public void setWaiting(final boolean b) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                scrollPane.setViewportView(waitingCategory);
            }
        });
    }

    /**
     * Displays an error panel to the end user.
     *
     * @param message Message to display
     */
    public void setError(final String message) {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final JPanel panel = new JPanel(new MigLayout("fillx"));
                panel.add(new TextLabel("An error has occurred loading the "
                        + "preferences dialog, an error has been raised: "),
                        "wrap");
                panel.add(new TextLabel(message));
                scrollPane.setViewportView(panel);
            }
        });
    }

}

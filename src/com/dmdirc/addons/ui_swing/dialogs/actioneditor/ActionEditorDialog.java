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

package com.dmdirc.addons.ui_swing.dialogs.actioneditor;

import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionFactory;
import com.dmdirc.actions.ActionStatus;
import com.dmdirc.actions.ActionSubstitutorFactory;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.actionsmanager.ActionsManagerDialog;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.IconManager;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.slf4j.LoggerFactory;

/**
 * Action editor dialog.
 */
public class ActionEditorDialog extends StandardDialog implements ActionListener,
        PropertyChangeListener {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ActionEditorDialog.class);
    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Name panel. */
    private ActionNamePanel name;
    /** Triggers panel. */
    private ActionTriggersPanel triggers;
    /** Response panel. */
    private ActionResponsePanel response;
    /** Conditions panel. */
    private ActionConditionsPanel conditions;
    /** Substitutions panel. */
    private ActionSubstitutionsPanel substitutions;
    /** Advanced panel. */
    private ActionAdvancedPanel advanced;
    /** Show substitutions button. */
    private JButton showSubstitutions;
    /** Show advanced button. */
    private JButton showAdvanced;
    /** Is the name valid? */
    private boolean nameValid = false;
    /** Are the triggers valid? */
    private boolean triggersValid = false;
    /** Are the conditions valid? */
    private boolean conditionsValid = false;
    /** Action to be edited. */
    private final Action action;
    /** Action group. */
    private final String group;
    /** Factory to use to create new actions. */
    private final ActionFactory actionFactory;

    /**
     * Instantiates the panel.
     *
     * @param iconManager   Icon manager
     * @param config        Config
     * @param subsFactory   Actions substitution factory
     * @param actionFactory Actions factory
     * @param parentWindow  Parent window
     * @param group         Action's group
     */
    public ActionEditorDialog(
            final IconManager iconManager,
            final AggregateConfigProvider config,
            final ActionSubstitutorFactory subsFactory,
            final ActionFactory actionFactory,
            final ActionsManagerDialog parentWindow,
            final String group) {
        super(parentWindow, ModalityType.DOCUMENT_MODAL);
        LOG.debug("loading with group: " + group);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("Action Editor");

        this.group = group;
        this.action = null;
        this.actionFactory = actionFactory;

        initComponents(iconManager, config, subsFactory);
        addListeners();
        doComponents();
        layoutComponents();

        setResizable(false);
    }

    /**
     * Instantiates the panel.
     *
     * @param iconManager   Icon manager
     * @param config        Config
     * @param subsFactory   Actions substitution factory
     * @param actionFactory Actions factory
     * @param parentWindow  Parent window
     * @param action        Action to be edited
     */
    public ActionEditorDialog(
            final IconManager iconManager,
            final AggregateConfigProvider config,
            final ActionSubstitutorFactory subsFactory,
            final ActionFactory actionFactory,
            final ActionsManagerDialog parentWindow,
            final Action action) {
        super(parentWindow, ModalityType.DOCUMENT_MODAL);
        LOG.debug("loading with action: " + action);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("Action Editor");

        this.group = action.getGroup();
        this.action = action;
        this.actionFactory = actionFactory;

        initComponents(iconManager, config, subsFactory);
        addListeners();
        doComponents();
        layoutComponents();

        setResizable(false);
    }

    /** Sets components initial states and stuff. */
    private void doComponents() {
        triggers.setEnabled(action != null);
        response.setEnabled(action != null);
        conditions.setEnabled(action != null);
        substitutions.setVisible(false);
        advanced.setVisible(false);

        triggersValid = action != null;
        conditionsValid = true;
        nameValid = action != null;
        getOkButton().setEnabled(action != null);

        if (action != null) {
            name.setActionName(action.getName());
            triggers.setTriggers(action.getTriggers());
            response.setResponse(action.getResponse());
            response.setFormatter(action.getNewFormat());
            conditions.setActionTrigger(action.getTriggers()[0]);
            conditions.setConditions(action.getConditions());
            conditions.setConditionTree(action.getRealConditionTree());
            advanced.setActionEnabled(action.getStatus() != ActionStatus.DISABLED);
            advanced.setConcurrencyGroup(action.getConcurrencyGroup());
            advanced.setActionStopped(action.isStopping());
        }
    }

    /** Initialises the components. */
    private void initComponents(final IconManager iconManager,
            final AggregateConfigProvider config, final ActionSubstitutorFactory subsFactory) {
        orderButtons(new JButton(), new JButton());
        name = new ActionNamePanel(iconManager, "", group);
        triggers = new ActionTriggersPanel(iconManager);
        response = new ActionResponsePanel(iconManager, config);
        conditions = new ActionConditionsPanel(iconManager);
        substitutions = new ActionSubstitutionsPanel(subsFactory);
        advanced = new ActionAdvancedPanel();
        showSubstitutions = new JButton("Show Substitutions");
        showAdvanced = new JButton("Show Advanced Options");
    }

    /** Adds the listeners. */
    private void addListeners() {
        showSubstitutions.addActionListener(this);
        showAdvanced.addActionListener(this);
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);

        name.addPropertyChangeListener("validationResult", this);
        triggers.addPropertyChangeListener("validationResult", this);
        conditions.addPropertyChangeListener("validationResult", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setMinimumSize(new Dimension(800, 600));
        setLayout(new MigLayout("fill, hidemode 3, wrap 2, pack, hmax 80sp," + "wmin 800, wmax 800"));

        add(name, "grow, w 50%");
        add(conditions, "spany 3, grow, pushx, w 50%");
        add(triggers, "grow, w 50%");
        add(response, "grow, pushy, w 50%");
        add(substitutions, "spanx, grow, push");
        add(advanced, "spanx, grow, push");
        add(showSubstitutions, "left, sgx button, split 2");
        add(showAdvanced, "left, sgx button");
        add(getLeftButton(), "right, sgx button, gapleft push, split");
        add(getRightButton(), "right, sgx button");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource().equals(showSubstitutions)) {
            substitutions.setVisible(!substitutions.isVisible());
            showSubstitutions
                    .setText(substitutions.isVisible() ? "Hide Substitutions"
                            : "Show Substitutions");
            pack();
        } else if (e.getSource().equals(showAdvanced)) {
            advanced.setVisible(!advanced.isVisible());
            showAdvanced.setText(advanced.isVisible() ? "Hide Advanced Options"
                    : "Show Advanced Options");
        } else if (e.getSource().equals(getOkButton())) {
            save();
            dispose();
        } else if (e.getSource().equals(getCancelButton())) {
            dispose();
        }
    }

    @Override
    public void validate() {
        super.validate();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                centreOnOwner();
            }
        });
    }

    /** Saves the action being edited. */
    private void save() {
        name.getActionName();
        triggers.getTriggers();
        response.getResponse();
        response.getFormatter();
        conditions.getConditions();
        conditions.getConditionTree();
        if (action == null) {
            final Action newAction = actionFactory.getAction(group, name.getActionName(),
                    triggers.getTriggers(), response.getResponse(),
                    conditions.getConditions(), conditions.getConditionTree(),
                    response.getFormatter());
            newAction.setConcurrencyGroup(advanced.getConcurrencyGroup());
            newAction.setEnabled(advanced.isActionEnabled());
            newAction.setStopping(advanced.isActionStopped());
        } else {
            action.setName(name.getActionName());
            action.setConditionTree(conditions.getConditionTree());
            action.setConditions(conditions.getConditions());
            action.setNewFormat(response.getFormatter());
            action.setResponse(response.getResponse());
            action.setTriggers(triggers.getTriggers());
            action.setConcurrencyGroup(advanced.getConcurrencyGroup());
            action.setEnabled(advanced.isActionEnabled());
            action.setStopping(advanced.isActionStopped());
            action.save();
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getSource().equals(name)) {
            nameValid = (Boolean) evt.getNewValue();
            triggers.setEnabled((Boolean) evt.getNewValue());
            conditions.setEnabled((Boolean) evt.getNewValue());
            response.setEnabled((Boolean) evt.getNewValue());
        } else if (evt.getSource().equals(triggers)) {
            triggersValid = (Boolean) evt.getNewValue();

            response.setEnabled((Boolean) evt.getNewValue());
            conditions.setEnabled((Boolean) evt.getNewValue());
            substitutions.setEnabled((Boolean) evt.getNewValue());
            advanced.setEnabled((Boolean) evt.getNewValue());

            substitutions.setType(triggers.getPrimaryTrigger());
            conditions.setActionTrigger(triggers.getPrimaryTrigger());
        } else if (evt.getSource().equals(conditions)) {
            conditionsValid = (Boolean) evt.getNewValue();
        }

        getOkButton().setEnabled(triggersValid && conditionsValid && nameValid);
    }

}

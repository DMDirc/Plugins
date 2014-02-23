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

import com.dmdirc.actions.ActionCondition;
import com.dmdirc.actions.ConditionTree;
import com.dmdirc.actions.ConditionTreeFactory.ConditionTreeFactoryType;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.ui.IconManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Action conditions panel.
 */
public class ActionConditionsPanel extends JPanel implements ActionListener,
        PropertyChangeListener {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Tree panel. */
    private ActionConditionsTreePanel tree;
    /** List Panel. */
    private ActionConditionsListPanel list;
    /** Add button. */
    private JButton add;
    /** tree validates? */
    private boolean treeValidates = true;
    /** list validates? */
    private boolean listValidates = true;
    /** Icon manager. */
    private final IconManager iconManager;

    /**
     * Instantiates the panel.
     *
     * @param iconManager Icon manager
     */
    public ActionConditionsPanel(final IconManager iconManager) {
        this(iconManager, null);
    }

    /**
     * Instantiates the panel.
     *
     * @param iconManager Icon manager
     * @param trigger     Action trigger
     */
    public ActionConditionsPanel(final IconManager iconManager,
            final ActionType trigger) {
        super();

        this.iconManager = iconManager;
        initComponents();
        addListeners();
        layoutComponents();

        if (trigger == null) {
            setEnabled(false);
            add.setEnabled(false);
        }
    }

    /** Validates the conditions. */
    public void validateConditions() {
        tree.validateConditions();
    }

    /** Initialises the components. */
    private void initComponents() {
        tree = new ActionConditionsTreePanel(iconManager);
        list = new ActionConditionsListPanel(iconManager, tree);
        add = new JButton("Add");
    }

    /** Adds the listeners. */
    private void addListeners() {
        add.addActionListener(this);
        tree.addPropertyChangeListener("validationResult", this);
        list.addPropertyChangeListener("validationResult", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, wrap 1, pack"));

        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Conditions"));

        final JScrollPane sp = new JScrollPane(list);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(10);

        add(tree, "growx, pushx");
        add(new JSeparator(JSeparator.HORIZONTAL), "growx, pushx");
        add(sp, "grow, push");
        add(add, "right, gaptop push");
    }

    /**
     * Sets the trigger for this conditions panel.
     *
     * @param trigger Action trigger.
     */
    public void setActionTrigger(final ActionType trigger) {
        list.setTrigger(trigger);
        add.setEnabled(trigger != null);
        add.setEnabled(trigger != null && trigger.getType().getArgNames().length != 0);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        tree.setEnabled(enabled);
        list.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        list.addCondition(new ActionCondition(-1, null, null, ""));
    }

    /**
     * Sets the conditions.
     *
     * @param conditions conditions list
     */
    public void setConditions(final List<ActionCondition> conditions) {
        list.clearConditions();

        for (final ActionCondition condition : conditions) {
            list.addCondition(condition);
        }
    }

    /**
     * Sets the condition tree.
     *
     * @param conditionTree new condition tree
     */
    public void setConditionTree(final ConditionTree conditionTree) {
        tree.setRule(list.getConditions().size(), conditionTree);
    }

    /**
     * Gets the condition type.
     *
     * @return condition type
     */
    public ConditionTreeFactoryType getConditionTreeType() {
        return tree.getRuleType(list.getConditions().size());
    }

    /**
     * Gets the condition tree.
     *
     * @return condition tree
     */
    public ConditionTree getConditionTree() {
        return tree.getRule(list.getConditions().size());
    }

    /**
     * Returns the condition list.
     *
     * @return condition list
     */
    public List<ActionCondition> getConditions() {
        return list.getConditions();
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final boolean currentlyValidates = listValidates && treeValidates;
        if (evt.getSource() == list) {
            listValidates = (Boolean) evt.getNewValue();
        } else {
            treeValidates = (Boolean) evt.getNewValue();
        }

        firePropertyChange("validationResult", currentlyValidates,
                listValidates && treeValidates);
    }

}
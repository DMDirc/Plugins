/*
 * Copyright (c) 2006-2011 DMDirc Developers
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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionTypeComparator;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.addons.ui_swing.ComboBoxWidthModifier;
import com.dmdirc.addons.ui_swing.components.renderers.ActionTypeRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.util.MapList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Action triggers panel.
 */
public class ActionTriggersPanel extends JPanel implements ActionListener,
        ActionTriggerRemovalListener, PropertyChangeListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Trigger combo box. */
    private JComboBox triggerGroup;
    /** Trigger combo box. */
    private JComboBox triggerItem;
    /** Add button. */
    private JButton add;
    /** Triggers list. */
    private ActionTriggersListPanel triggerList;
    /** Are we internally changing the combo boxes? */
    private boolean comboChange;

    /** Instantiates the panel. */
    public ActionTriggersPanel() {
        super();

        initComponents();
        addListeners();
        layoutComponents();
    }

    /** Initialises the components. */
    private void initComponents() {
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Triggers"));

        triggerGroup = new JComboBox(new DefaultComboBoxModel());
        //Only fire events on selection not on highlight
        triggerGroup.putClientProperty("JComboBox.isTableCellEditor",
                Boolean.TRUE);
        triggerGroup.setRenderer(new ActionTypeRenderer());
        triggerGroup.addPopupMenuListener(new ComboBoxWidthModifier());

        triggerItem = new JComboBox(new DefaultComboBoxModel());
        //Only fire events on selection not on highlight
        triggerItem.putClientProperty("JComboBox.isTableCellEditor",
                Boolean.TRUE);
        triggerItem.setRenderer(new ActionTypeRenderer());
        triggerItem.addPopupMenuListener(new ComboBoxWidthModifier());

        add = new JButton("Add");
        add.setEnabled(triggerGroup.getSelectedIndex() != -1
                && triggerItem.getSelectedIndex() != 1);

        triggerList = new ActionTriggersListPanel();
        addAll(ActionManager.getActionManager().getGroupedTypes());
    }

    /** Adds the listeners. */
    private void addListeners() {
        add.addActionListener(this);
        triggerGroup.addActionListener(this);
        triggerItem.addActionListener(this);
        triggerList.addTriggerListener(this);

        triggerList.addPropertyChangeListener("triggerCount", this);
    }

    /** Lays out the components. */
    private void layoutComponents() {
        setLayout(new MigLayout("fill, pack, wmax 50%"));

        add(new TextLabel("This action will be triggered when any of these "
                + "events occurs: "), "growx, pushx, wrap, spanx");
        add(triggerList, "grow, push, wrap, spanx");
        add(triggerGroup, "growx, wmax 39%, wmin 39%");
        add(triggerItem, "growx, wmax 39%, wmin 39%");
        add(add, "right");
    }

    /**
     * Returns the primary trigger for this panel.
     *
     * @return Primary trigger or null
     */
    public ActionType getPrimaryTrigger() {
        if (triggerList.getTriggerCount() == 0) {
            return null;
        }
        return triggerList.getTrigger(0);
    }

    /**
     * Returns the list of triggers.
     *
     * @return Trigger list
     */
    public ActionType[] getTriggers() {
        final List<ActionType> triggers = triggerList.getTriggers();
        return triggers.toArray(new ActionType[triggers.size()]);
    }

    /**
     * Sets the triggers.
     *
     * @param triggers Sets the triggers.
     */
    public void setTriggers(final ActionType[] triggers) {
        triggerList.clearTriggers();

        for (ActionType localTrigger : triggers) {
            triggerList.addTrigger(localTrigger);
        }
        addCompatible(triggerList.getTrigger(0));
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (comboChange) {
            return;
        }
        if (e.getSource() == triggerGroup || e.getSource() == triggerItem) {
            if (e.getSource() == triggerGroup) {
                addList(ActionManager.getActionManager().getGroupedTypes()
                        .get((String) triggerGroup.getSelectedItem()));
            } else {
                add.setEnabled(triggerGroup.getSelectedIndex() != -1
                    && triggerItem.getSelectedIndex() != -1);
            }
        } else {
            triggerList.addTrigger((ActionType) triggerItem.getSelectedItem());
            addCompatible((ActionType) triggerItem.getSelectedItem());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void triggerRemoved(final ActionType trigger) {
        if (triggerList.getTriggerCount() == 0) {
            addAll(ActionManager.getActionManager().getGroupedTypes());
        } else {
            addCompatible(triggerList.getTrigger(0));
        }
    }

    private void addAll(final MapList<String, ActionType> mapList) {
        comboChange = true;
        ((DefaultComboBoxModel) triggerGroup.getModel()).removeAllElements();
        ((DefaultComboBoxModel) triggerItem.getModel()).removeAllElements();
        for (Map.Entry<String, List<ActionType>> entry : mapList.entrySet()) {
            ((DefaultComboBoxModel) triggerGroup.getModel())
                    .addElement(entry.getKey());
        }
        addList(ActionManager.getActionManager().getGroupedTypes()
                        .get((String) triggerGroup.getSelectedItem()));
        triggerGroup.setEnabled(triggerGroup.getModel().getSize() > 0);
        triggerItem.setEnabled(triggerItem.getModel().getSize() > 0);
        add.setEnabled(triggerGroup.getSelectedIndex() != -1
                    && triggerItem.getSelectedIndex() != -1);
        comboChange = false;
    }

    private void addCompatible(final ActionType primaryType) {
        comboChange = true;
        ((DefaultComboBoxModel) triggerGroup.getModel()).removeAllElements();
        ((DefaultComboBoxModel) triggerItem.getModel()).removeAllElements();
        for (ActionType thisType : ActionManager.getActionManager()
                .findCompatibleTypes(primaryType)) {
            final List<ActionType> types = triggerList.getTriggers();
            if (!types.contains(thisType)) {
                ((DefaultComboBoxModel) triggerItem.getModel())
                        .addElement(thisType);
                if (((DefaultComboBoxModel) triggerGroup.getModel())
                        .getIndexOf(thisType) == -1) {
                    ((DefaultComboBoxModel) triggerGroup.getModel())
                            .addElement(thisType.getType().getGroup());
                }
            }
        }
        if (triggerItem.getModel().getSize() == 0) {
            ((DefaultComboBoxModel) triggerGroup.getModel()).removeAllElements();
        }
        triggerGroup.setEnabled(triggerGroup.getModel().getSize() > 0);
        triggerItem.setEnabled(triggerItem.getModel().getSize() > 0);
        add.setEnabled(triggerGroup.getSelectedIndex() != -1
                    && triggerItem.getSelectedIndex() != -1);
        comboChange = false;
    }

    private void addList(final List<ActionType> list) {
        comboChange = true;
        ((DefaultComboBoxModel) triggerItem.getModel()).removeAllElements();
        Collections.sort(list, new ActionTypeComparator());
        for (ActionType entry : list) {
            ((DefaultComboBoxModel) triggerItem.getModel()).addElement(entry);
        }
        triggerGroup.setEnabled(triggerGroup.getModel().getSize() > 0);
        triggerItem.setEnabled(triggerItem.getModel().getSize() > 0);
        add.setEnabled(triggerGroup.getSelectedIndex() != -1
                    && triggerItem.getSelectedIndex() != -1);
        comboChange = false;
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(final boolean enabled) {
        triggerList.setEnabled(enabled);
        if (enabled) {
            add.setEnabled(triggerGroup.getSelectedIndex() != -1
                && triggerItem.getSelectedIndex() != -1);
            if (triggerGroup.getModel().getSize() > 0) {
                triggerGroup.setEnabled(enabled);
            }
            if (triggerItem.getModel().getSize() > 0) {
                triggerItem.setEnabled(enabled);
            }
        } else {
            add.setEnabled(false);
            triggerGroup.setEnabled(false);
            triggerItem.setEnabled(false);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        firePropertyChange("validationResult", (Integer) evt.getOldValue() > 0,
                (Integer) evt.getNewValue() > 0);
    }

    /** Validates the triggers. */
    public void validateTriggers() {
        triggerList.validateTriggers();
    }
}

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

import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.ActionTypeComparator;
import com.dmdirc.addons.ui_swing.ComboBoxWidthModifier;
import com.dmdirc.addons.ui_swing.components.renderers.ActionTypeRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.collections.MapList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.MutableComboBoxModel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

/**
 * Action triggers panel.
 */
public class ActionTriggersPanel extends JPanel implements ActionListener,
        ActionTriggerRemovalListener, PropertyChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Trigger combo box. */
    private JComboBox<Object> triggerGroup;
    /** Trigger combo box. */
    private JComboBox<Object> triggerItem;
    /** Triggers list. */
    private ActionTriggersListPanel triggerList;
    /** Are we internally changing the combo boxes? */
    private boolean comboChange;
    /** Triggers compatible with the currently added triggers. */
    private final List<ActionType> compatibleTriggers;

    /**
     * Instantiates the panel.
     *
     * @param iconManager Icon manager
     * */
    public ActionTriggersPanel(final IconManager iconManager) {

        compatibleTriggers = new ArrayList<>();

        initComponents(iconManager);
        addListeners();
        layoutComponents();
    }

    /**
     * Initialises the components.
     *
     * @param iconManager Icon manager
     * */
    private void initComponents(final IconManager iconManager) {
        setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Triggers"));

        triggerGroup = new JComboBox<>(new DefaultComboBoxModel<>());
        // Only fire events on selection not on highlight
        triggerGroup.putClientProperty("JComboBox.isTableCellEditor",
                Boolean.TRUE);
        triggerGroup.setRenderer(new ActionTypeRenderer(triggerGroup.getRenderer()));
        triggerGroup.addPopupMenuListener(new ComboBoxWidthModifier());

        triggerItem = new JComboBox<>(new DefaultComboBoxModel<>());
        // Only fire events on selection not on highlight
        triggerItem.putClientProperty("JComboBox.isTableCellEditor",
                Boolean.TRUE);
        triggerItem.setRenderer(new ActionTypeRenderer(triggerItem.getRenderer()));
        triggerItem.addPopupMenuListener(new ComboBoxWidthModifier());

        triggerList = new ActionTriggersListPanel(iconManager);
        addAll(ActionManager.getActionManager().getGroupedTypes());
    }

    /** Adds the listeners. */
    private void addListeners() {
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
        add(triggerGroup, "growx, wmax 50%-(4*rel), wmin 50%-(4*rel)");
        add(triggerItem, "growx, wmax 50%-(4*rel), wmin 50%-(4*rel)");
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
    public void setTriggers(final ActionType... triggers) {
        triggerList.clearTriggers();

        for (final ActionType localTrigger : triggers) {
            triggerList.addTrigger(localTrigger);
        }

        if (triggers.length > 0) {
            addCompatible(triggers[0]);
            setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (comboChange) {
            return;
        }
        if (e.getSource() == triggerGroup) {
            if (triggerList.getTriggerCount() == 0) {
                addList(ActionManager.getActionManager().getGroupedTypes()
                        .get((String) triggerGroup.getSelectedItem()));
            } else {
                addList(compatibleTriggers);
            }
        } else {
            triggerList.addTrigger((ActionType) triggerItem.getSelectedItem());
            addCompatible((ActionType) triggerItem.getSelectedItem());
        }
    }

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
        compatibleTriggers.clear();
        final DefaultComboBoxModel<Object> triggerModel
                = (DefaultComboBoxModel<Object>) triggerGroup.getModel();
        triggerModel.removeAllElements();
        ((DefaultComboBoxModel<Object>) triggerItem.getModel()).removeAllElements();
        for (final Map.Entry<String, List<ActionType>> entry : mapList
                .entrySet()) {
            triggerModel.addElement(entry.getKey());
        }
        triggerGroup.setSelectedIndex(-1);
        triggerGroup.setEnabled(triggerGroup.getModel().getSize() > 0);
        triggerItem.setEnabled(triggerItem.getModel().getSize() > 0);
        comboChange = false;
    }

    /**
     * Populates the combo boxes with triggers compatible with the specified type.
     *
     * @param primaryType Primary type
     */
    private void addCompatible(final ActionType primaryType) {
        final DefaultComboBoxModel<Object> groupModel = (DefaultComboBoxModel<Object>) triggerGroup.
                getModel();
        final DefaultComboBoxModel<Object> itemModel = (DefaultComboBoxModel<Object>) triggerItem.
                getModel();

        comboChange = true;
        compatibleTriggers.clear();
        groupModel.removeAllElements();
        itemModel.removeAllElements();
        final List<ActionType> types = triggerList.getTriggers();

        ActionManager.getActionManager().findCompatibleTypes(primaryType).stream()
                .filter(thisType -> !types.contains(thisType))
                .forEach(thisType -> {
                    compatibleTriggers.add(thisType);
                    itemModel.addElement(thisType);
                    if (groupModel.getIndexOf(thisType.getType().getGroup()) == -1) {
                        groupModel.addElement(thisType.getType().getGroup());
                    }
                });
        triggerGroup.setSelectedIndex(-1);
        triggerItem.setSelectedIndex(-1);
        if (itemModel.getSize() == 0) {
            groupModel.removeAllElements();
        }
        triggerGroup.setEnabled(groupModel.getSize() > 0);
        if (groupModel.getSize() == 1) {
            triggerGroup.setSelectedIndex(0);
        }

        triggerItem.setEnabled(itemModel.getSize() > 0);
        triggerItem.setEnabled(triggerGroup.getSelectedIndex() != -1);

        comboChange = false;
    }

    /**
     * Adds the specified list of triggers to the items list.
     *
     * @param list List of triggers to add
     */
    private void addList(final List<ActionType> list) {
        comboChange = true;
        ((DefaultComboBoxModel) triggerItem.getModel()).removeAllElements();
        Collections.sort(list, new ActionTypeComparator());
        list.stream()
                .filter(entry -> compatibleTriggers.isEmpty() || compatibleTriggers.contains(entry))
                .forEach(entry -> ((MutableComboBoxModel<Object>) triggerItem.getModel()).addElement(entry));
        triggerItem.setSelectedIndex(-1);
        triggerGroup.setEnabled(triggerGroup.getModel().getSize() > 0);
        triggerItem.setEnabled(triggerItem.getModel().getSize() > 0);
        comboChange = false;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        triggerList.setEnabled(enabled);
        if (enabled) {
            if (triggerGroup.getModel().getSize() > 0) {
                triggerGroup.setEnabled(true);
            }
            if (triggerItem.getModel().getSize() > 0) {
                triggerItem.setEnabled(true);
            }
        } else {
            triggerGroup.setEnabled(false);
            triggerItem.setEnabled(false);
        }
    }

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

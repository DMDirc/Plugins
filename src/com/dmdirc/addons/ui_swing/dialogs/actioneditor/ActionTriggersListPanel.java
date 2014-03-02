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

import com.dmdirc.addons.ui_swing.components.ImageButton;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.collections.ListenerList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Action triggers list panel.
 */
public class ActionTriggersListPanel extends JPanel {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** Trigger list. */
    private final List<ActionType> triggers;
    /** Listeners. */
    private final ListenerList listeners = new ListenerList();
    /** Icon manager. */
    private final IconManager iconManager;

    /**
     * Instantiates the panel.
     *
     * @param iconManager Icon Manager
     */
    public ActionTriggersListPanel(final IconManager iconManager) {
        this(iconManager, new ArrayList<ActionType>());
    }

    /**
     * Instantiates the panel.
     *
     * @param iconManager Icon Manager
     * @param triggers    Trigger list
     */
    public ActionTriggersListPanel(final IconManager iconManager,
            final List<ActionType> triggers) {
        super();

        this.iconManager = iconManager;
        this.triggers = new ArrayList<>(triggers);

        initComponents();
        layoutComponents();
    }

    /**
     * Initialises the components.
     *
     * @param iconManager Icon Manager
     */
    private void initComponents() {
        setOpaque(false);
        setLayout(new MigLayout("fillx, wrap 2"));
    }

    /** Lays out the components. */
    private void layoutComponents() {
        synchronized (triggers) {
            setVisible(false);

            removeAll();

            for (final ActionType trigger : triggers) {
                final ImageButton<?> button = new ImageButton<>(
                        "delete", iconManager.getIcon("close-inactive"),
                        iconManager.getIcon("close-active"));
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        delTrigger(trigger);
                    }
                });

                button.setEnabled(isEnabled());

                add(new JLabel(trigger.getName()), "growx, pushx");
                add(button, "right");
            }

            if (triggers.isEmpty()) {
                if (isEnabled()) {
                    add(new TextLabel("<b>Select a trigger and click Add.</b>"));
                } else {
                    add(new TextLabel("No Triggers."));
                }
            }
            setVisible(true);
        }
    }

    /**
     * Adds a trigger to the list.
     *
     * @param trigger Trigger to add
     */
    public void addTrigger(final ActionType trigger) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (triggers) {
                    triggers.add(trigger);
                    firePropertyChange("triggerCount", triggers.size() - 1,
                            triggers.size());

                    layoutComponents();
                }
            }
        });
    }

    /**
     * Deletes a trigger from the list.
     *
     * @param trigger Trigger to delete
     */
    public void delTrigger(final ActionType trigger) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (triggers) {
                    triggers.remove(trigger);
                    fireTriggerRemoved(trigger);
                    firePropertyChange("triggerCount", triggers.size() + 1,
                            triggers.size());

                    layoutComponents();
                }
            }
        });
    }

    /**
     * Clears the trigger list.
     */
    public void clearTriggers() {
        for (final ActionType trigger : triggers) {
            delTrigger(trigger);
        }
    }

    /**
     * Returns the current list of triggers.
     *
     * @return Trigger list
     */
    public List<ActionType> getTriggers() {
        synchronized (triggers) {
            return triggers;
        }
    }

    /**
     * Gets the trigger at the specified index.
     *
     * @param index Index to retrieve
     *
     * @return Requested action trigger
     */
    public ActionType getTrigger(final int index) {
        return triggers.get(index);
    }

    /**
     * Returns the number of triggers.
     *
     * @return Trigger count
     */
    public int getTriggerCount() {
        synchronized (triggers) {
            return triggers.size();
        }
    }

    /**
     * Adds an ActionTriggerRemovalListener to the listener list.
     *
     * @param listener Listener to add
     */
    public void addTriggerListener(
            final ActionTriggerRemovalListener listener) {
        if (listener == null) {
            return;
        }

        listeners.add(ActionTriggerRemovalListener.class, listener);
    }

    /**
     * Removes an ActionTriggerRemovalListener from the listener list.
     *
     * @param listener Listener to remove
     */
    public void removeTriggerListener(
            final ActionTriggerRemovalListener listener) {
        listeners.remove(ActionTriggerRemovalListener.class, listener);
    }

    /**
     * Fired when the an action trigger is removed.
     *
     * @param type Removed trigger
     */
    protected void fireTriggerRemoved(final ActionType type) {
        for (final ActionTriggerRemovalListener listener : listeners.get(
                ActionTriggerRemovalListener.class)) {
            listener.triggerRemoved(type);
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                layoutComponents();
            }
        });
    }

    /** Validates the triggers. */
    public void validateTriggers() {
        firePropertyChange("triggerCount", triggers.size(), triggers.size());
    }

}

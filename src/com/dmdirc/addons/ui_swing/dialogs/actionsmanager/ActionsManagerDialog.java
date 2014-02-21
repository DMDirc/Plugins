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

package com.dmdirc.addons.ui_swing.dialogs.actionsmanager;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.actions.Action;
import com.dmdirc.actions.ActionGroup;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.ListScroller;
import com.dmdirc.addons.ui_swing.components.SortedListModel;
import com.dmdirc.addons.ui_swing.components.frames.AppleJFrame;
import com.dmdirc.addons.ui_swing.components.renderers.ActionGroupListCellRenderer;
import com.dmdirc.addons.ui_swing.components.text.TextLabel;
import com.dmdirc.addons.ui_swing.dialogs.StandardDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.validators.FileNameValidator;
import com.dmdirc.util.validators.ValidatorChain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

/**
 * Allows the user to manage actions.
 */
public class ActionsManagerDialog extends StandardDialog implements
        ActionListener, com.dmdirc.interfaces.ActionListener,
        ListSelectionListener {

    /**
     * A version number for this class. It should be changed whenever the class structure is changed
     * (or anything else that would prevent serialized objects being unserialized with the new
     * class).
     */
    private static final long serialVersionUID = 1;
    /** Config instance. */
    private final ConfigProvider config;
    /** Preferences setting component factory. */
    private final PrefsComponentFactory compFactory;
    /** Are we saving? */
    private final AtomicBoolean saving = new AtomicBoolean(false);
    /** Duplicate action group validator. */
    private final ValidatorChain<String> validator;
    /** Info label. */
    private TextLabel infoLabel;
    /** Group list. */
    private JList groups;
    /** Add button. */
    private JButton add;
    /** Edit button. */
    private JButton edit;
    /** Delete button. */
    private JButton delete;
    /** Info panel. */
    private ActionGroupInformationPanel info;
    /** Actions panel. */
    private ActionsGroupPanel actions;
    /** Settings panels. */
    private Map<ActionGroup, ActionGroupSettingsPanel> settings;
    /** Active s panel. */
    private ActionGroupSettingsPanel activeSettings;
    /** Group panel. */
    private JPanel groupPanel;
    /** The icon manager to use for validating text fields. */
    private final IconManager iconManager;
    /** Factory to use to create group panels. */
    private final ActionsGroupPanelFactory groupPanelFactory;

    /**
     * Creates a new instance of ActionsManagerDialog.
     *
     * @param parentWindow      Parent window
     * @param controller        Parent controller
     * @param config            Config to save dialog state to
     * @param compFactory       Prefs setting component factory
     * @param iconManager       The icon manager to use for validating text fields.
     * @param groupPanelFactory Factory to use to create group panels.
     */
    @Inject
    public ActionsManagerDialog(
            final MainFrame parentWindow,
            final SwingController controller,
            @UserConfig final ConfigProvider config,
            final PrefsComponentFactory compFactory,
            @GlobalConfig final IconManager iconManager,
            final ActionsGroupPanelFactory groupPanelFactory) {
        super(Apple.isAppleUI()
                ? new AppleJFrame(parentWindow, controller)
                : parentWindow, ModalityType.MODELESS);
        this.config = config;
        this.compFactory = compFactory;
        this.iconManager = iconManager;
        this.groupPanelFactory = groupPanelFactory;

        initComponents();
        validator = new ValidatorChain<>(
                new ActionGroupNoDuplicatesInListValidator(groups,
                (DefaultListModel) groups.getModel()), new FileNameValidator());
        addListeners();
        layoutGroupPanel();
        layoutComponents();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("Actions Manager");
        setResizable(false);
    }

    /**
     * Initialises the components.
     */
    private void initComponents() {
        orderButtons(new JButton(), new JButton());
        infoLabel = new TextLabel("Actions allow you to make DMDirc"
                + " intelligently respond to various events.  Action groups are"
                + " there for you to organise groups, add or remove them"
                + " to suit your needs.");
        groups = new JList(new SortedListModel<>(new ActionGroupNameComparator()));
        actions = groupPanelFactory.getActionsGroupPanel(this, null);
        info = new ActionGroupInformationPanel(null);
        settings = new HashMap<>();
        activeSettings = new ActionGroupSettingsPanel(compFactory, null, this);
        settings.put(null, activeSettings);
        add = new JButton("Add");
        edit = new JButton("Edit");
        delete = new JButton("Delete");
        groupPanel = new JPanel();
        groupPanel.setName("Groups");

        groupPanel.setBorder(BorderFactory.createTitledBorder(UIManager.
                getBorder("TitledBorder.border"), "Groups"));
        info.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Information"));
        actions.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder(
                "TitledBorder.border"), "Actions"));

        groups.setCellRenderer(new ActionGroupListCellRenderer(groups.getCellRenderer()));
        groups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        edit.setEnabled(false);
        delete.setEnabled(false);

        info.setVisible(false);
        activeSettings.setVisible(false);

        ListScroller.register(groups);

        reloadGroups();
    }

    /**
     * Adds listeners.
     */
    private void addListeners() {
        getOkButton().addActionListener(this);
        getCancelButton().addActionListener(this);
        add.addActionListener(this);
        edit.addActionListener(this);
        delete.addActionListener(this);
        groups.getSelectionModel().addListSelectionListener(this);
        ActionManager.getActionManager().registerListener(this, CoreActionType.ACTION_CREATED);
        ActionManager.getActionManager().registerListener(this, CoreActionType.ACTION_UPDATED);
        ActionManager.getActionManager().registerListener(this, CoreActionType.ACTION_DELETED);
    }

    /**
     * Lays out the group panel.
     */
    private void layoutGroupPanel() {
        groupPanel.setLayout(new MigLayout("fill, wrap 1"));

        groupPanel.add(new JScrollPane(groups), "growy, pushy, w 150!");
        groupPanel.add(add, "sgx button, w 150!");
        groupPanel.add(edit, "sgx button, w 150!");
        groupPanel.add(delete, "sgx button, w 150!");
    }

    /**
     * Lays out the components.
     */
    private void layoutComponents() {

        getContentPane().setLayout(new MigLayout(
                "fill, wrap 2, hidemode 3, wmax 800"));

        getContentPane().add(infoLabel, "spanx 2, growx");
        if (info.isVisible() && activeSettings.isVisible()) {
            getContentPane().add(groupPanel, "growy, pushy, spany 3");
        } else if (info.isVisible() || activeSettings.isVisible()) {
            getContentPane().add(groupPanel, "growy, pushy, spany 2");
        } else {
            getContentPane().add(groupPanel, "growy, pushy");
        }
        getContentPane().add(info, "growx, pushx");
        getContentPane().add(actions, "grow, push");
        getContentPane().add(activeSettings, "growx, pushx");
        getContentPane().add(getOkButton(), "skip, right, sgx button");
    }

    /**
     * Reloads the action groups.
     */
    private void reloadGroups() {
        reloadGroups(null);
    }

    /**
     * Reloads the action groups.
     *
     * @param selectedGroup Newly selected group
     */
    private void reloadGroups(final ActionGroup selectedGroup) {
        ((DefaultListModel) groups.getModel()).clear();
        for (ActionGroup group : ActionManager.getActionManager().getGroupsMap().values()) {
            ((DefaultListModel) groups.getModel()).addElement(group);
        }
        groups.setSelectedValue(selectedGroup, true);
    }

    /**
     * Changes the active group.
     *
     * @param group New group
     */
    private void changeActiveGroup(final ActionGroup group) {
        info.setActionGroup(group);
        actions.setActionGroup(group);
        if (!settings.containsKey(group)) {
            final ActionGroupSettingsPanel currentSettings = new ActionGroupSettingsPanel(
                    compFactory, group, this);
            settings.put(group, currentSettings);
            currentSettings.setBorder(BorderFactory.createTitledBorder(
                    UIManager.getBorder("TitledBorder.border"), "Settings"));
        }
        activeSettings = settings.get(group);

        info.setVisible(info.shouldDisplay());
        activeSettings.setVisible(activeSettings.shouldDisplay());

        getContentPane().setVisible(false);
        getContentPane().removeAll();
        layoutComponents();
        validate();
        layoutComponents();
        getContentPane().setVisible(true);
    }

    /**
     * {@inheritDoc}
     *
     * @param e Action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == add) {
            addGroup();
        } else if (e.getSource() == edit) {
            editGroup();
        } else if (e.getSource() == delete) {
            delGroup();
        } else if ((e.getSource() == getOkButton() || e.getSource()
                == getCancelButton()) && !saving.getAndSet(true)) {
            for (ActionGroupSettingsPanel loopSettings : settings.values()) {
                loopSettings.save();
            }
            config.setOption("dialogstate", "actionsmanagerdialog", groups.getSelectedIndex());
            dispose();
        }
    }

    /**
     * Prompts then adds an action group.
     */
    private void addGroup() {
        final int index = groups.getSelectedIndex();
        groups.getSelectionModel().clearSelection();
        new StandardInputDialog(this, ModalityType.DOCUMENT_MODAL, iconManager, "New action group",
                "Please enter the name of the new action group", validator) {
            /** Java Serialisation version ID. */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public boolean save() {
                if (!saving.getAndSet(true)) {
                    groups.setSelectedIndex(index);
                    if (getText() == null || getText().isEmpty()
                            && !ActionManager.getActionManager().getGroupsMap()
                            .containsKey(getText())) {
                        return false;
                    } else {
                        final ActionGroup group = ActionManager
                                .getActionManager().createGroup(getText());
                        reloadGroups(group);
                        return true;
                    }
                }
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public void cancelled() {
                groups.setSelectedIndex(index);
            }
        }.display(this);
    }

    /**
     * Prompts then edits an action group.
     */
    private void editGroup() {
        final String oldName =
                ((ActionGroup) groups.getSelectedValue()).getName();
        final StandardInputDialog inputDialog = new StandardInputDialog(
                this, ModalityType.DOCUMENT_MODAL, iconManager, "Edit action group",
                "Please enter the new name of the action group", validator) {
            /** Java Serialisation version ID. */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public boolean save() {
                if (!saving.getAndSet(true)) {
                    if (getText() == null || getText().isEmpty()) {
                        return false;
                    } else {
                        ActionManager.getActionManager().changeGroupName(
                                oldName, getText());
                        reloadGroups();
                        return true;
                    }
                }
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public void cancelled() {
                //Ignore
            }
        };
        inputDialog.setText(oldName);
        inputDialog.display(this);
    }

    /**
     * Prompts then deletes an action group.
     */
    private void delGroup() {
        final String group =
                ((ActionGroup) groups.getSelectedValue()).getName();
        new StandardQuestionDialog(this,
                ModalityType.APPLICATION_MODAL,
                "Confirm deletion",
                "Are you sure you wish to delete the '" + group
                + "' group and all actions within it?") {
            /** Java Serialisation version ID. */
            private static final long serialVersionUID = 1;

            /** {@inheritDoc} */
            @Override
            public boolean save() {
                int location =
                        ((DefaultListModel) groups.getModel()).indexOf(
                        ActionManager.getActionManager().getOrCreateGroup(group));
                ActionManager.getActionManager().deleteGroup(group);
                reloadGroups();
                if (groups.getModel().getSize() == 0) {
                    location = -1;
                } else if (location >= groups.getModel().getSize()) {
                    location = groups.getModel().getSize();
                } else if (location <= 0) {
                    location = 0;
                }
                groups.setSelectedIndex(location);
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public void cancelled() {
                //Ignore
            }
        }.display();
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        changeActiveGroup((ActionGroup) groups.getSelectedValue());
        if (groups.getSelectedIndex() == -1 || !((ActionGroup) groups.
                getSelectedValue()).isDelible()) {
            edit.setEnabled(false);
            delete.setEnabled(false);
        } else {
            edit.setEnabled(true);
            delete.setEnabled(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        if (groups.getSelectedValue() == null) {
            return;
        }
        if (type.equals(CoreActionType.ACTION_CREATED) || type.equals(
                CoreActionType.ACTION_UPDATED)) {
            final Action action = (Action) arguments[0];
            if (action.getGroup().equals(((ActionGroup) groups.getSelectedValue()).
                    getName())) {
                actions.actionChanged(action);
            }
        } else {
            if (arguments[0].equals(((ActionGroup) groups.getSelectedValue()).
                    getName())) {
                actions.actionDeleted((String) arguments[1]);
            }
        }
    }

}
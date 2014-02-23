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

package com.dmdirc.addons.ui_swing.dialogs.channelsetting;

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.renderers.ExtendedListModeCellRenderer;
import com.dmdirc.addons.ui_swing.components.renderers.ListModeCellRenderer;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.parser.common.ChannelListModeItem;
import com.dmdirc.ui.IconManager;
import com.dmdirc.util.collections.MapList;
import com.dmdirc.util.validators.NotEmptyValidator;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import static com.google.common.base.Preconditions.checkNotNull;

/** List modes panel. */
public final class ChannelListModesPane extends JPanel implements ActionListener,
        ListSelectionListener, ConfigChangeListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 5;
    /** Channel. */
    private final Channel channel;
    /** Combox box used to switch between list modes. */
    private final JComboBox<String> listModesMenu;
    /** Arraylist of jpanels containing the listmodes. */
    private final List<JList<ChannelListModeItem>> listModesPanels;
    /** JPanel used to show listmodespanels in. */
    private final JScrollPane listModesPanel;
    /** Add list mode button. */
    private final JButton addListModeButton;
    /** Remove list mode button. */
    private final JButton removeListModeButton;
    /** list modes available on this server. */
    private final char[] listModesArray;
    /** Modes on creation. */
    private final MapList<Character, ChannelListModeItem> existingListItems;
    /** Mode count label. */
    private final JLabel modeCount;
    /** Extended info toggle. */
    private final JCheckBox toggle;
    /** Parent window. */
    private final Window parentWindow;
    /** Native cell renderer. */
    private final ListCellRenderer<? super ChannelListModeItem> nativeRenderer;
    /** Cell renderer. */
    private ListCellRenderer<? super ChannelListModeItem> renderer;
    /** Mode list. */
    private JList<ChannelListModeItem> list;
    /** The config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** The config to write settings to. */
    private final ConfigProvider userConfig;
    /** The manager to use to retrieve icons for dialogs and validation. */
    private final IconManager iconManager;

    /**
     * Creates a new instance of ChannelListModePane.
     *
     * @param globalConfig The config to read settings from.
     * @param userConfig   The config to write settings to.
     * @param iconManager  The manager to use to retrieve icons for dialogs and validation.
     * @param channel      Parent channel
     * @param parentWindow Parent window
     */
    public ChannelListModesPane(
            final AggregateConfigProvider globalConfig,
            final ConfigProvider userConfig,
            final IconManager iconManager,
            final Channel channel,
            final Window parentWindow) {
        super();

        this.globalConfig = checkNotNull(globalConfig);
        this.userConfig = checkNotNull(userConfig);
        this.iconManager = checkNotNull(iconManager);
        this.channel = checkNotNull(channel);
        this.parentWindow = checkNotNull(parentWindow);
        this.setOpaque(UIUtilities.getTabbedPaneOpaque());

        list = new JList<>();
        nativeRenderer = list.getCellRenderer();
        if (channel.getConfigManager().getOptionBool("general", "extendedListModes")) {
            renderer = new ExtendedListModeCellRenderer();
        } else {
            renderer = new ListModeCellRenderer(nativeRenderer);
        }
        listModesPanel = new JScrollPane();
        listModesPanels = new ArrayList<>();
        listModesArray = channel.getConnection().getParser().getListChannelModes().
                toCharArray();
        existingListItems = new MapList<>();
        listModesMenu = new JComboBox<>(new DefaultComboBoxModel<String>());
        addListModeButton = new JButton("Add");
        removeListModeButton = new JButton("Remove");
        removeListModeButton.setEnabled(false);
        modeCount = new JLabel();
        toggle = new JCheckBox("Show extended information",
                channel.getConfigManager().getOptionBool("general", "extendedListModes"));
        toggle.setOpaque(UIUtilities.getTabbedPaneOpaque());

        initListModesPanel();
        initListeners();
    }

    /** Updates the panel. */
    public void update() {
        existingListItems.clear();

        if (channel.getChannelInfo() == null) {
            return;
        }
        for (int i = 0; i < listModesArray.length; i++) {
            final char mode = listModesArray[i];
            final Collection<ChannelListModeItem> listItems = channel
                    .getChannelInfo().getListMode(mode);
            if (listItems == null) {
                continue;
            }
            existingListItems.add(mode, new ArrayList<>(listItems));
            final DefaultListModel<ChannelListModeItem> model
                    = (DefaultListModel<ChannelListModeItem>) listModesPanels.get(i).getModel();

            model.removeAllElements();
            for (ChannelListModeItem listItem : listItems) {
                model.addElement(listItem);
            }
        }
    }

    /** Updates the list mode menu. */
    private void updateMenu() {
        if (listModesArray.length == 0) {
            listModesMenu.setEnabled(false);
            addListModeButton.setEnabled(false);
            return;
        } else {
            listModesMenu.setEnabled(true);
            addListModeButton.setEnabled(true);
        }

        final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) listModesMenu.
                getModel();
        for (char mode : listModesArray) {
            String modeText = mode + " list";
            if (channel.getConfigManager().hasOptionString("server", "mode" + mode)) {
                modeText = channel.getConfigManager().getOption("server",
                        "mode" + mode) + " list [+" + mode + "]";
            }
            model.addElement(modeText);

            list = new JList<>(new DefaultListModel<ChannelListModeItem>());
            list.setCellRenderer(renderer);
            list.setVisibleRowCount(8);
            list.addListSelectionListener(this);

            listModesPanels.add(list);
        }
        if (listModesPanels.isEmpty()) {
            listModesPanel.setViewportView(new JPanel());
        } else {
            listModesPanel.setViewportView(listModesPanels.get(0));
        }
        updateModeCount();
        listModesPanel.setVisible(true);
    }

    /** Initialises the list modes panel. */
    private void initListModesPanel() {
        updateMenu();

        setLayout(new MigLayout("fill, wrap 1"));

        add(listModesMenu, "growx, pushx");
        add(listModesPanel, "grow, push");
        add(modeCount, "split 2, growx, pushx");
        add(toggle, "alignx center");
        add(addListModeButton, "split 2, growx, pushx");
        add(removeListModeButton, "growx, pushx");

        update();
        updateModeCount();
    }

    /** Initialises listeners for this dialog. */
    private void initListeners() {
        addListModeButton.addActionListener(this);
        removeListModeButton.addActionListener(this);
        listModesMenu.addActionListener(this);
        toggle.addActionListener(this);
        channel.getConfigManager().addChangeListener("general", "extendedListModes", this);

    }

    /** Sends the list modes to the server. */
    public void save() {
        final Map<ChannelListModeItem, Character> currentModes = new HashMap<>();
        final Map<ChannelListModeItem, Character> newModes = new HashMap<>();

        for (int i = 0; i < listModesArray.length;
                i++) {
            final char mode = listModesArray[i];
            final Enumeration<?> values = ((DefaultListModel) listModesPanels.get(i).getModel()).
                    elements();
            final List<ChannelListModeItem> listItems = existingListItems.get(mode);

            for (ChannelListModeItem listItem : listItems) {
                currentModes.put(listItem, mode);
            }

            while (values.hasMoreElements()) {
                final ChannelListModeItem value = (ChannelListModeItem) values.nextElement();
                newModes.put(value, mode);
            }
        }

        for (Entry<ChannelListModeItem, Character> entry : newModes.entrySet()) {
            if (currentModes.containsKey(entry.getKey())) {
                currentModes.remove(entry.getKey());
            } else {
                channel.getChannelInfo().alterMode(true, entry.getValue(), entry.getKey().getItem());
            }
        }

        for (Entry<ChannelListModeItem, Character> entry : currentModes.entrySet()) {
            channel.getChannelInfo().alterMode(false, entry.getValue(), entry.getKey().getItem());
        }

        channel.getChannelInfo().flushModes();
        userConfig.setOption("general", "extendedListModes", toggle.isSelected());
    }

    /** Adds a list mode. */
    private void addListMode() {
        final int selectedIndex = listModesMenu.getSelectedIndex();
        String modeText = String.valueOf(listModesArray[selectedIndex]);
        if (channel.getConfigManager().hasOptionString("server", "mode"
                + listModesArray[selectedIndex])) {
            modeText = channel.getConfigManager().
                    getOption("server", "mode" + listModesArray[selectedIndex]);
        }
        new StandardInputDialog(parentWindow, ModalityType.DOCUMENT_MODAL,
                iconManager, "Add new " + modeText,
                "Please enter the hostmask for the new " + modeText,
                new NotEmptyValidator()) {
                    /**
                     * A version number for this class. It should be changed whenever the class
                     * structure is changed (or anything else that would prevent serialized objects
                     * being unserialized with the new class).
                     */
                    private static final long serialVersionUID = 1;

                    /** {@inheritDoc} */
                    @Override
                    public boolean save() {
                        final DefaultListModel<ChannelListModeItem> model
                        = (DefaultListModel<ChannelListModeItem>) listModesPanels.get(selectedIndex).
                        getModel();
                        model.addElement(new ChannelListModeItem(getText(), "",
                                        System.currentTimeMillis() / 1000));
                        updateModeCount();
                        return true;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public void cancelled() {
                        //Ignore
                    }
                }.display();
    }

    /** Removes a list mode. */
    private void removeListMode() {
        final int selectedIndex = listModesMenu.getSelectedIndex();
        final JList<ChannelListModeItem> removeList = listModesPanels.get(selectedIndex);
        for (ChannelListModeItem mode : removeList.getSelectedValuesList()) {
            ((DefaultListModel) removeList.getModel()).removeElement(mode);
        }
        updateModeCount();
    }

    /**
     * {@inheritDoc}
     *
     * @param event Action event
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        if (listModesMenu.equals(event.getSource())) {
            final int selectedIndex = listModesMenu.getSelectedIndex();
            listModesPanel.setVisible(false);
            listModesPanel.setViewportView(listModesPanels.get(selectedIndex));
            listModesPanel.setVisible(true);
            updateModeCount();
        } else if (addListModeButton.equals(event.getSource())) {
            addListMode();
        } else if (removeListModeButton.equals(event.getSource())) {
            removeListMode();
        } else if (toggle.equals(event.getSource())) {
            if (toggle.isSelected()) {
                renderer = new ExtendedListModeCellRenderer();
            } else {
                renderer = new ListModeCellRenderer(nativeRenderer);
            }
            for (JList<ChannelListModeItem> renderList : listModesPanels) {
                renderList.setCellRenderer(renderer);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param event List selection event
     */
    @Override
    public void valueChanged(final ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            final int selected = ((JList) event.getSource()).getSelectedIndex();
            if (selected == -1) {
                removeListModeButton.setEnabled(false);
            } else {
                removeListModeButton.setEnabled(true);
            }
        }
    }

    /** Updates the mode count label. */
    private void updateModeCount() {
        if (listModesPanels.isEmpty()) {
            modeCount.setText(null);
            return;
        }

        final int selected = listModesMenu.getSelectedIndex();
        final int current = listModesPanels.get(selected).getModel().getSize();
        final int maxModes = channel.getConnection().getParser().
                getMaxListModes(listModesArray[selected]);

        if (maxModes == -1) {
            modeCount.setText(current + " mode" + (current == 1 ? "" : "s")
                    + " set");
        } else {
            modeCount.setText(current + " mode" + (current == 1 ? "" : "s")
                    + " set (maximum of " + maxModes + ")");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (globalConfig.getOptionBool("general", "extendedListModes")) {
            renderer = new ListModeCellRenderer(nativeRenderer);
        } else {
            renderer = new ExtendedListModeCellRenderer();
        }
        for (JList<ChannelListModeItem> renderList : listModesPanels) {
            renderList.setCellRenderer(renderer);
        }
    }

}

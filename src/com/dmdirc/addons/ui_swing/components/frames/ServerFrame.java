/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.Server;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.interfaces.ServerWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * The ServerFrame is the MDI window that shows server messages to the user.
 */
public final class ServerFrame extends InputTextFrame implements ServerWindow,
        ActionListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 9;
    /** popup menu item. */
    private JMenuItem settingsMI;

    /**
     * Creates a new ServerFrame.
     *
     * @param owner Parent Frame container
     * @param controller Swing controller
     */
    public ServerFrame(final SwingController controller, final Server owner) {
        super(controller, owner);

        initComponents();

        setInputHandler(new SwingInputHandler(getInputField(),
                owner.getCommandParser(), this));
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public CommandParser getCommandParser() {
        return getContainer().getCommandParser();
    }

    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        settingsMI = new JMenuItem("Settings");
        settingsMI.addActionListener(this);

        setLayout(new MigLayout("ins 0, fill, hidemode 3, wrap 1"));
        add(getTextPane(), "grow, push");
        add(getSearchBar(), "growx, pushx");
        add(inputPanel, "growx, pushx");
    }

    /**
     * {@inheritDoc}.
     *
     * @param actionEvent Action event
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() == settingsMI) {
            ServerSettingsDialog.showServerSettingsDialog(getContainer().
                    getServer(), getController().getMainFrame());
        }
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.CHAN_NICK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getChannelPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.CHAN_HYPERLINK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNormalPopupType() {
        return PopupType.CHAN_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        if (getContainer().getServer().getState()
                .equals(ServerState.CONNECTED)) {
            settingsMI.setEnabled(true);
        } else {
            settingsMI.setEnabled(false);
        }

        if (popupMenu.getComponentCount() > 0) {
            popupMenu.addSeparator();
        }

        popupMenu.add(settingsMI);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        if (ServerSettingsDialog.hasServerSettingsDialog()) {
            ServerSettingsDialog.getServerSettingsDialog(getContainer()
                    .getServer(), getController().getMainFrame()).dispose();
        }
        super.close();
    }
}

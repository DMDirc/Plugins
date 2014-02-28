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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.ServerState;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
import com.dmdirc.addons.ui_swing.dialogs.InputDialogCloseListener;
import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialogFactory;
import com.dmdirc.addons.ui_swing.dialogs.channellist.ChannelListDialog;
import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
import com.dmdirc.addons.ui_swing.injection.KeyedDialogProvider;
import com.dmdirc.parser.common.ChannelJoinRequest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * A menu to provide channel related commands in the menu bar.
 */
@Singleton
public class ChannelMenu extends JMenu implements ActionListener, MenuListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** Dialog provider. */
    private final KeyedDialogProvider<Channel, ChannelSettingsDialog> dialogProvider;
    /** Input dialog factory. */
    private final StandardInputDialogFactory inputDialogFactory;
    /** Swing controller. */
    private final SwingController controller;
    /** Main frame. */
    private final MainFrame mainFrame;
    /** Menu items to be disabled/enabled. */
    private JMenuItem csd;
    private JMenuItem join;
    private JMenuItem list;

    /**
     * Creates a new channel menu.
     *
     * @param controller         Parent swing controller.
     * @param mainFrame          Parent mainframe
     * @param dialogProvider     Channel settings dialog provider
     * @param inputDialogFactory Input dialog factory
     */
    @Inject
    public ChannelMenu(
            final SwingController controller,
            final MainFrame mainFrame,
            final KeyedDialogProvider<Channel, ChannelSettingsDialog> dialogProvider,
            final StandardInputDialogFactory inputDialogFactory) {
        super("Channel");
        this.controller = controller;
        this.mainFrame = mainFrame;
        this.dialogProvider = dialogProvider;
        this.inputDialogFactory = inputDialogFactory;
        setMnemonic('c');
        addMenuListener(this);
        initChannelMenu();
        menuSelected(null);
    }

    /**
     * Initialises the channel menu.
     */
    private void initChannelMenu() {
        join = new JMenuItem();
        join.setText("Join Channel...");
        join.setMnemonic('j');
        join.setActionCommand("JoinChannel");
        join.addActionListener(this);
        add(join);

        csd = new JMenuItem();
        csd.setMnemonic('c');
        csd.setText("Channel Settings");
        csd.setActionCommand("ChannelSettings");
        csd.addActionListener(this);
        add(csd);

        list = new JMenuItem();
        list.setText("List channels...");
        list.setMnemonic('l');
        list.setActionCommand("ListChannels");
        list.addActionListener(this);
        add(list);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        switch (e.getActionCommand()) {
            case "JoinChannel":
                inputDialogFactory.getStandardInputDialog(mainFrame, "Join channel",
                        "Enter the name of the channel to join.", new ChannelJoinDialogListener());
                break;
            case "ChannelSettings":
                final FrameContainer activeWindow = mainFrame.getActiveFrame().getContainer();
                if (activeWindow instanceof Channel) {
                    dialogProvider.displayOrRequestFocus(((Channel) activeWindow));
                }
                break;
            case "ListChannels":
                new ChannelListDialog(controller).display();
                break;
        }
    }

    @Override
    public final void menuSelected(final MenuEvent e) {
        final TextFrame activeFrame = mainFrame.getActiveFrame();
        final FrameContainer activeWindow = activeFrame == null ? null
                : activeFrame.getContainer();

        final boolean connected = activeWindow != null
                && activeWindow.getConnection() != null
                && activeWindow.getConnection().getState() == ServerState.CONNECTED;

        join.setEnabled(connected);
        csd.setEnabled(connected);
        list.setEnabled(connected);
    }

    @Override
    public final void menuDeselected(final MenuEvent e) {
        //Ignore
    }

    @Override
    public final void menuCanceled(final MenuEvent e) {
        //Ignore
    }

    private class ChannelJoinDialogListener implements InputDialogCloseListener {

        @Override
        public boolean save(final String text) {
            mainFrame.getActiveFrame().getContainer().getConnection().join(new ChannelJoinRequest(
                    text));
            return true;
        }

        @Override
        public void cancelled() {
            //Ignore
        }

    }

}

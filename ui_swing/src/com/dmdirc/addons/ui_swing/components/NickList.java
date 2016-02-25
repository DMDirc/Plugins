/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.addons.ui_swing.EDTInvocation;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.components.renderers.NicklistRenderer;
import com.dmdirc.addons.ui_swing.textpane.ClickType;
import com.dmdirc.addons.ui_swing.textpane.ClickTypeValue;
import com.dmdirc.config.ConfigBinding;
import com.dmdirc.events.NickListClientAddedEvent;
import com.dmdirc.events.NickListClientRemovedEvent;
import com.dmdirc.events.NickListClientsChangedEvent;
import com.dmdirc.events.NickListUpdatedEvent;
import com.dmdirc.interfaces.GroupChat;
import com.dmdirc.interfaces.GroupChatUser;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.engio.mbassy.listener.Handler;

/**
 * Nicklist class.
 */
public class NickList extends JScrollPane implements MouseListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 10;
    /** Nick list. */
    private final JList<GroupChatUser> nickList;
    /** Parent frame. */
    private final ChannelFrame frame;
    /** The colour manager to use for this nicklist. */
    private final ColourManager colourManager;
    /** Nick list model. */
    private final NicklistListModel nicklistModel;

    /**
     * Creates a nicklist.
     *
     * @param frame  Frame
     * @param config Config
     */
    public NickList(final ChannelFrame frame, final AggregateConfigProvider config,
            final ColourManagerFactory colourManagerFactory) {
        this.frame = frame;
        this.colourManager = colourManagerFactory.getColourManager(config);

        nickList = new JList<>();
        nickList.setCellRenderer(new NicklistRenderer(nickList.getCellRenderer(), config,
                nickList, colourManager));
        nickList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        nickList.addMouseListener(this);

        nicklistModel = new NicklistListModel(config);

        nickList.setModel(nicklistModel);
        setViewportView(nickList);

        final int splitPanePosition = config.getOptionInt("ui", "channelSplitPanePosition");
        setPreferredSize(new Dimension(splitPanePosition, 0));
        setMinimumSize(new Dimension(75, 0));

        nicklistModel.replace(((GroupChat) frame.getContainer()).getUsers());
        frame.getContainer().getEventBus().subscribe(this);
        config.getBinder().bind(this, NickList.class);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        processMouseEvent(e);
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        processMouseEvent(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        processMouseEvent(e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        //Ignore
    }

    @Override
    public void processMouseEvent(final MouseEvent e) {
        if (!e.isPopupTrigger()
                || e.getSource() != nickList
                || nickList.getMousePosition() == null) {
            return;
        }
        if (checkCursorInSelectedCell() || selectNickUnderCursor()) {
            final List<GroupChatUser> values = nickList.getSelectedValuesList();
            final StringBuilder builder = new StringBuilder();

            for (GroupChatUser value : values) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }

                builder.append(value.getNickname());
            }

            frame.showPopupMenu(new ClickTypeValue(ClickType.NICKNAME,
                    builder.toString()), new Point(e.getXOnScreen(),
                            e.getYOnScreen()));
        } else {
            nickList.clearSelection();
        }

        super.processMouseEvent(e);
    }

    /**
     * Checks whether the mouse cursor is currently over a cell in the nicklist which has been
     * previously selected.
     *
     * @return True if the cursor is over a selected cell, false otherwise
     */
    private boolean checkCursorInSelectedCell() {
        boolean showMenu = false;
        final Point mousePos = nickList.getMousePosition();
        if (mousePos != null) {
            for (int i = 0; i < nickList.getModel().getSize(); i++) {
                if (nickList.getCellBounds(i, i) != null && nickList.
                        getCellBounds(i, i).
                        contains(mousePos) && nickList.isSelectedIndex(i)) {
                    showMenu = true;
                    break;
                }
            }
        }
        return showMenu;
    }

    /**
     * If the mouse cursor is over a nick list cell, sets that cell to be selected and returns true.
     * If the mouse is not over any cell, the selection is unchanged and the method returns false.
     *
     * @return True if an item was selected
     */
    private boolean selectNickUnderCursor() {
        boolean suceeded = false;
        final Point mousePos = nickList.getMousePosition();
        if (mousePos != null) {
            for (int i = 0; i < nickList.getModel().getSize(); i++) {
                if (nickList.getCellBounds(i, i) != null && nickList.
                        getCellBounds(i, i).
                        contains(mousePos)) {
                    nickList.setSelectedIndex(i);
                    suceeded = true;
                    break;
                }
            }
        }
        return suceeded;
    }

    @ConfigBinding(domain = "ui", key = "textPaneFontName", invocation = EDTInvocation.class)
    public void handleFontName(final String value) {
        nickList.setFont(new Font(value, Font.PLAIN, getFont().getSize()));
        nickList.repaint();
    }

    @ConfigBinding(domain = "ui", key = "nicklistbackgroundcolour",
            fallbacks = {"ui", "backgroundcolour"}, invocation = EDTInvocation.class)
    public void handleBackgroundColour(final String value) {
        nickList.setBackground(UIUtilities.convertColour(
                colourManager.getColourFromString(value, null)));
        nickList.repaint();
    }

    @ConfigBinding(domain = "ui", key = "nicklistforegroundcolour",
            fallbacks = {"ui", "foregroundcolour"}, invocation = EDTInvocation.class)
    public void handleForegroundColour(final String value) {
        nickList.setForeground(UIUtilities.convertColour(
                colourManager.getColourFromString(value, null)));
        nickList.repaint();
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleClientsChanged(final NickListClientsChangedEvent event) {
        if (event.getChannel().getWindowModel().equals(frame.getContainer())) {
            nicklistModel.replace(event.getUsers());
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleNickListUpdated(final NickListUpdatedEvent event) {
        if (event.getChannel().getWindowModel().equals(frame.getContainer())) {
            nicklistModel.sort();
            repaint();
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleClientAdded(final NickListClientAddedEvent event) {
        if (event.getChannel().getWindowModel().equals(frame.getContainer())) {
            nicklistModel.add(event.getUser());
        }
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void handleClientRemoved(final NickListClientRemovedEvent event) {
        if (event.getChannel().getWindowModel().equals(frame.getContainer())) {
            nicklistModel.remove(event.getUser());
        }
    }

}

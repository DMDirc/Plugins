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

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.Channel;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
import com.dmdirc.addons.ui_swing.components.renderers.NicklistRenderer;
import com.dmdirc.addons.ui_swing.textpane.ClickType;
import com.dmdirc.addons.ui_swing.textpane.ClickTypeValue;
import com.dmdirc.interfaces.NicklistListener;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigChangeListener;
import com.dmdirc.parser.interfaces.ChannelClientInfo;
import com.dmdirc.ui.messages.ColourManager;
import com.dmdirc.ui.messages.ColourManagerFactory;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * Nicklist class.
 */
public class NickList extends JScrollPane implements ConfigChangeListener,
        MouseListener, NicklistListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 10;
    /** Nick list. */
    private final JList<ChannelClientInfo> nickList;
    /** Parent frame. */
    private final ChannelFrame frame;
    /** Config. */
    private final AggregateConfigProvider config;
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
        this.config = config;
        this.colourManager = colourManagerFactory.getColourManager(config);

        nickList = new JList<>();

        nickList.setBackground(UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "nicklistbackgroundcolour",
                                "ui", "backgroundcolour"), null)));
        nickList.setForeground(UIUtilities.convertColour(
                colourManager.getColourFromString(
                        config.getOptionString(
                                "ui", "nicklistforegroundcolour",
                                "ui", "foregroundcolour"), null)));
        nickList.setFont(new Font(config.getOption("ui", "textPaneFontName"),
                Font.PLAIN, getFont().getSize()));
        config.addChangeListener("ui", "nicklistforegroundcolour", this);
        config.addChangeListener("ui", "foregroundcolour", this);
        config.addChangeListener("ui", "nicklistbackgroundcolour", this);
        config.addChangeListener("ui", "backgroundcolour", this);
        config.addChangeListener("ui", "nickListAltBackgroundColour", this);
        config.addChangeListener("ui", "textPaneFontName", this);

        nickList.setCellRenderer(new NicklistRenderer(config, nickList, colourManager));
        nickList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        nickList.addMouseListener(this);

        nicklistModel = new NicklistListModel(config);

        nickList.setModel(nicklistModel);
        setViewportView(nickList);

        final int splitPanePosition = config.getOptionInt("ui",
                "channelSplitPanePosition");
        setPreferredSize(new Dimension(splitPanePosition, 0));
        setMinimumSize(new Dimension(75, 0));

        ((Channel) frame.getContainer()).addNicklistListener(this);
        clientListUpdated(((Channel) frame.getContainer()).getChannelInfo()
                .getChannelClients());
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

    /**
     * Processes every mouse button event to check for a popup trigger.
     *
     * @param e mouse event
     */
    @Override
    public void processMouseEvent(final MouseEvent e) {
        if (!e.isPopupTrigger()
                || e.getSource() != nickList
                || nickList.getMousePosition() == null) {
            return;
        }
        if (checkCursorInSelectedCell() || selectNickUnderCursor()) {
            final List<ChannelClientInfo> values = nickList.getSelectedValuesList();
            final StringBuilder builder = new StringBuilder();

            for (ChannelClientInfo value : values) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }

                builder.append(value.getClient().getNickname());
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

    @Override
    public void configChanged(final String domain, final String key) {
        if ("nickListAltBackgroundColour".equals(key)
                || "nicklistbackgroundcolour".equals(key)
                || "backgroundcolour".equals(key)
                || "nicklistforegroundcolour".equals(key)
                || "foregroundcolour".equals(key)
                || "textPaneFontName".equals(key)) {
            nickList.setBackground(UIUtilities.convertColour(
                    colourManager.getColourFromString(
                            config.getOptionString(
                                    "ui", "nicklistbackgroundcolour",
                                    "ui", "backgroundcolour"), null)));
            nickList.setForeground(UIUtilities.convertColour(
                    colourManager.getColourFromString(
                            config.getOptionString(
                                    "ui", "nicklistforegroundcolour",
                                    "ui", "foregroundcolour"), null)));
            nickList.setFont(new Font(config.getOption("ui", "textPaneFontName"),
                    Font.PLAIN, getFont().getSize()));
            nickList.repaint();
        }
        nickList.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    @Override
    public void clientListUpdated(final Collection<ChannelClientInfo> clients) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                nicklistModel.replace(clients);
            }
        });
    }

    @Override
    public void clientListUpdated() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                nicklistModel.sort();
                repaint();
            }
        });
    }

    @Override
    public void clientAdded(final ChannelClientInfo client) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                nicklistModel.add(client);
            }
        });
    }

    @Override
    public void clientRemoved(final ChannelClientInfo client) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                nicklistModel.remove(client);
            }
        });
    }

}

/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.FrameContainer;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.actions.ChannelCopyAction;
import com.dmdirc.addons.ui_swing.actions.CommandAction;
import com.dmdirc.addons.ui_swing.actions.HyperlinkCopyAction;
import com.dmdirc.addons.ui_swing.actions.InputFieldCopyAction;
import com.dmdirc.addons.ui_swing.actions.NicknameCopyAction;
import com.dmdirc.addons.ui_swing.actions.SearchAction;
import com.dmdirc.addons.ui_swing.components.SwingSearchBar;
import com.dmdirc.addons.ui_swing.textpane.ClickTypeValue;
import com.dmdirc.addons.ui_swing.textpane.MouseEventType;
import com.dmdirc.addons.ui_swing.textpane.TextPane;
import com.dmdirc.addons.ui_swing.textpane.TextPaneControlCodeCopyAction;
import com.dmdirc.addons.ui_swing.textpane.TextPaneCopyAction;
import com.dmdirc.addons.ui_swing.textpane.TextPaneEndAction;
import com.dmdirc.addons.ui_swing.textpane.TextPaneHomeAction;
import com.dmdirc.addons.ui_swing.textpane.TextPaneListener;
import com.dmdirc.addons.ui_swing.textpane.TextPanePageDownAction;
import com.dmdirc.addons.ui_swing.textpane.TextPanePageUpAction;
import com.dmdirc.commandparser.PopupManager;
import com.dmdirc.commandparser.PopupMenu;
import com.dmdirc.commandparser.PopupMenuItem;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.commandparser.parsers.GlobalCommandParser;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.interfaces.ConfigChangeListener;
import com.dmdirc.interfaces.FrameCloseListener;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.ui.interfaces.Window;
import com.dmdirc.ui.messages.IRCDocument;
import com.dmdirc.util.StringTranscoder;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

/**
 * Implements a generic (internal) frame.
 */
public abstract class TextFrame extends JPanel implements Window,
        ConfigChangeListener, TextPaneListener, FrameCloseListener {
    
    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 5;
    /** The channel object that owns this frame. */
    protected final FrameContainer<?> frameParent;
    /** Frame output pane. */
    private TextPane textPane;
    /** search bar. */
    private SwingSearchBar searchBar;
    /** String transcoder. */
    private StringTranscoder transcoder;
    /** Command parser for popup commands. */
    private final CommandParser commandParser;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Creates a new instance of Frame.
     *
     * @param owner FrameContainer owning this frame.
     * @param controller Swing controller
     */
    public TextFrame(final FrameContainer<?> owner,
            final SwingController controller) {
        super();
        this.controller = controller;
        this.frameParent = owner;

        final ConfigManager config = owner.getConfigManager();

        owner.addCloseListener(this);
        owner.setTitle(frameParent.getTitle());

        try {
            transcoder = new StringTranscoder(Charset.forName(
                    config.getOption("channel", "encoding")));
        } catch (UnsupportedCharsetException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        } catch (IllegalCharsetNameException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        } catch (IllegalArgumentException ex) {
            transcoder = new StringTranscoder(Charset.forName("UTF-8"));
        }

        commandParser = findCommandParser();

        initComponents();
        setFocusable(true);

        getTextPane().addTextPaneListener(this);

        config.addChangeListener("ui", "foregroundcolour", this);
        config.addChangeListener("ui", "backgroundcolour", this);
        config.addChangeListener("ui", "frameBufferSize", this);
        updateColours();

        setLayout(new MigLayout("fill"));
    }

    /**
     * Locate the appropriate command parser in the window heirarchy.
     *
     * @return Closest command parser in the tree
     */
    private CommandParser findCommandParser() {
        CommandParser localParser = null;
        Window inputWindow = this;
        while (!(inputWindow instanceof InputWindow) && inputWindow != null
                && inputWindow.getContainer().getParent() != null) {
            inputWindow = controller.getWindowFactory().getSwingWindow(
                    inputWindow.getContainer().getParent());
        }
        if (inputWindow instanceof InputWindow) {
            localParser = ((InputWindow) inputWindow).getContainer()
                    .getCommandParser();
        }

        if (localParser == null) {
            localParser = GlobalCommandParser.getGlobalCommandParser();
        }

        return localParser;
    }

    /** {@inheritDoc} */
    @Override
    public void activateFrame() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                frameParent.windowActivated();
            }
        });
    }

    /** Closes this frame. */
    @Override
    public void close() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                frameParent.handleWindowClosing();
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated MDI has been removed, these methods are useless
     */
    @Deprecated
    @Override
    public void minimise() {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated MDI has been removed, these methods are useless
     */
    @Deprecated
    @Override
    public void maximise() {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated MDI has been removed, these methods are useless
     */
    @Deprecated
    @Override
    public void restore() {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated MDI has been removed, these methods are useless
     */
    @Deprecated
    @Override
    public void toggleMaximise() {
        //Ignore
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use corresponding methods in {@link FrameContainer} instead
     */
    @Override
    @Deprecated
    public final void addLine(final String line, final boolean timestamp) {
        frameParent.addLine(line, timestamp);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use corresponding methods in {@link FrameContainer} instead
     */
    @Override
    @Deprecated
    public final void addLine(final String messageType, final Object... args) {
        frameParent.addLine(messageType, args);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use corresponding methods in {@link FrameContainer} instead
     */
    @Override
    @Deprecated
    public final void addLine(final StringBuffer messageType,
            final Object... args) {
        frameParent.addLine(messageType, args);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Call {@link IRCDocument#clear()} via
     * {@link FrameContainer#getDocument()}
     */
    @Override
    @Deprecated
    public final void clear() {
        UIUtilities.invokeLater(new Runnable() {

            /** {@inheritDoc} */
            @Override
            public void run() {
                getTextPane().clear();
            }
        });
    }

    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        setTextPane(new TextPane(this));

        searchBar = new SwingSearchBar(this);
        searchBar.setVisible(false);

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
                "pageUpAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
                "pageDownAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "searchAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                UIUtilities.getCtrlDownMask()), "searchAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
                UIUtilities.getCtrlDownMask()), "homeAction");

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                put(KeyStroke.getKeyStroke(KeyEvent.VK_END,
                UIUtilities.getCtrlDownMask()), "endAction");

        getSearchBar().getTextField().getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, UIUtilities.getCtrlMask()), "textpaneCopy");
        getSearchBar().getTextField().getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, UIUtilities.getCtrlMask()
                & KeyEvent.SHIFT_DOWN_MASK), "textpaneCopy");
        getSearchBar().getTextField().getActionMap().put("textpaneCopy",
                new InputFieldCopyAction(getTextPane(),
                getSearchBar().getTextField()));

        getActionMap().put("pageUpAction",
                new TextPanePageUpAction(getTextPane()));
        getActionMap().put("pageDownAction",
                new TextPanePageDownAction(getTextPane()));
        getActionMap().put("searchAction", new SearchAction(searchBar));
        getActionMap().put("homeAction", new TextPaneHomeAction(getTextPane()));
        getActionMap().put("endAction", new TextPaneEndAction(getTextPane()));
    }

    /** {@inheritDoc} */
    @Override
    public FrameContainer<?> getContainer() {
        return frameParent;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link FrameContainer#getConfigManager()}
     */
    @Deprecated
    @Override
    public ConfigManager getConfigManager() {
        return getContainer().getConfigManager();
    }

    /**
     * Returns the text pane for this frame.
     *
     * @return Text pane for this frame
     */
    public final TextPane getTextPane() {
        return textPane;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link FrameContainer#getTranscoder()} instead
     */
    @Override
    @Deprecated
    public StringTranscoder getTranscoder() {
        return transcoder;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        if (frameParent == null) {
            return "";
        }

        return frameParent.toString();
    }

    /**
     * Sets the frames text pane.
     *
     * @param newTextPane new text pane to use
     */
    protected final void setTextPane(final TextPane newTextPane) {
        this.textPane = newTextPane;
    }

    /** {@inheritDoc} */
    @Override
    public void mouseClicked(final ClickTypeValue clickType,
            final MouseEventType eventType, final MouseEvent event) {
        if (event.isPopupTrigger()) {
            showPopupMenuInternal(clickType, event.getPoint());
        }
        if (eventType == MouseEventType.CLICK
                && event.getButton() == MouseEvent.BUTTON1) {
            handleLinkClick(clickType);
        }
    }

    /**
     * Handles clicking of a link in a textpane.
     *
     * @param clickType Details of link clicked
     */
    private void handleLinkClick(final ClickTypeValue clickType) {
        switch (clickType.getType()) {
            case CHANNEL:
                if (frameParent.getServer() != null && ActionManager.
                        processEvent(CoreActionType.LINK_CHANNEL_CLICKED,
                        null, this, clickType.getValue())) {
                    frameParent.getServer().join(
                            new ChannelJoinRequest(clickType.getValue()));
                }
                break;
            case HYPERLINK:
                if (ActionManager.processEvent(
                        CoreActionType.LINK_URL_CLICKED, null, this,
                        clickType.getValue())) {
                    controller.getURLHandler().launchApp(clickType.getValue());
                }
                break;
            case NICKNAME:
                if (frameParent.getServer() != null && ActionManager
                        .processEvent(CoreActionType.LINK_NICKNAME_CLICKED,
                        null, this, clickType.getValue())) {
                    getContainer().getServer().getQuery(clickType.getValue())
                            .activateFrame();
                }
                break;
            default:
                break;
        }
    }

    /**
     * What popup type should be used for popup menus for nicknames.
     *
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getNicknamePopupType();

    /**
     * What popup type should be used for popup menus for channels.
     *
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getChannelPopupType();

    /**
     * What popup type should be used for popup menus for hyperlinks.
     *
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getHyperlinkPopupType();

    /**
     * What popup type should be used for popup menus for normal clicks.
     *
     * @return Appropriate popuptype for this frame
     */
    public abstract PopupType getNormalPopupType();

    /**
     * A method called to add custom popup items.
     *
     * @param popupMenu Popup menu to add popup items to
     */
    public abstract void addCustomPopupItems(final JPopupMenu popupMenu);

    /**
     * Shows a popup menu at the specified point for the specified click type.
     *
     * @param type ClickType Click type
     * @param point Point Point of the click
     */
    private void showPopupMenuInternal(final ClickTypeValue type,
            final Point point) {
        final JPopupMenu popupMenu;

        final String[] parts = type.getValue().split("\n");
        final Object[][] arguments = new Object[parts.length][1];

        int i = 0;
        for (String part : parts) {
            arguments[i++][0] = part;
        }

        switch (type.getType()) {
            case CHANNEL:
                popupMenu = getPopupMenu(getChannelPopupType(), arguments);
                popupMenu.add(new ChannelCopyAction(type.getValue()));
                if (popupMenu.getComponentCount() > 1) {
                    popupMenu.addSeparator();
                }

                break;
            case HYPERLINK:
                popupMenu = getPopupMenu(getHyperlinkPopupType(), arguments);
                popupMenu.add(new HyperlinkCopyAction(type.getValue()));
                if (popupMenu.getComponentCount() > 1) {
                    popupMenu.addSeparator();
                }

                break;
            case NICKNAME:
                popupMenu = getPopupMenu(getNicknamePopupType(), arguments);
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.addSeparator();
                }

                popupMenu.add(new NicknameCopyAction(type.getValue()));
                break;
            default:
                popupMenu = getPopupMenu(null, arguments);
                break;
        }

        popupMenu.add(new TextPaneCopyAction(getTextPane()));
        popupMenu.add(new TextPaneControlCodeCopyAction(textPane));

        addCustomPopupItems(popupMenu);

        popupMenu.show(this, (int) point.getX(), (int) point.getY());
    }

    /**
     * Shows a popup menu at the specified point for the specified click type.
     *
     * @param type ClickType Click type
     * @param point Point Point of the click (Must be screen coords)
     */
    public void showPopupMenu(final ClickTypeValue type,
            final Point point) {
        SwingUtilities.convertPointFromScreen(point, this);
        showPopupMenuInternal(type, point);
    }

    /**
     * Builds a popup menu of a specified type.
     *
     * @param type type of menu to build
     * @param arguments Arguments for the command
     *
     * @return PopupMenu
     */
    public JPopupMenu getPopupMenu(final PopupType type,
            final Object[][] arguments) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (type != null) {
            popupMenu = (JPopupMenu) populatePopupMenu(popupMenu,
                    PopupManager.getMenu(type, getConfigManager()),
                    arguments);
        }

        return popupMenu;
    }

    /**
     * Populates the specified popupmenu.
     *
     * @param menu Menu component
     * @param popup Popup to get info from
     * @param arguments Arguments for the command
     *
     * @return Populated popup
     */
    private JComponent populatePopupMenu(final JComponent menu,
            final PopupMenu popup,
            final Object[][] arguments) {
        for (PopupMenuItem menuItem : popup.getItems()) {
            if (menuItem.isDivider()) {
                menu.add(new JSeparator());
            } else if (menuItem.isSubMenu()) {
                menu.add(populatePopupMenu(new JMenu(menuItem.getName()),
                        menuItem.getSubMenu(), arguments));
            } else {
                menu.add(new JMenuItem(new CommandAction(commandParser, this,
                        menuItem.getName(), menuItem.getCommand(arguments))));
            }

        }
        return menu;
    }

    /**
     * Gets the search bar.
     *
     * @return the frames search bar
     */
    public final SwingSearchBar getSearchBar() {
        return searchBar;
    }

    /** {@inheritDoc} */
    @Override
    public void configChanged(final String domain, final String key) {
        if (getContainer().getConfigManager() == null
                || getTextPane() == null) {
            return;
        }

        if ("ui".equals(domain) && ("foregroundcolour".equals(key)
                || "backgroundcolour".equals(key))) {
            updateColours();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing(final FrameContainer<?> window) {
        setVisible(false);
    }

    /** {@inheritDoc} */
    @Override
    public SwingController getController() {
        return controller;
    }

    /**
     * Updates colour settings from their config values.
     */
    private void updateColours() {
        getTextPane().setForeground(getConfigManager().
                        getOptionColour("ui", "foregroundcolour"));
        getTextPane().setBackground(getConfigManager().
                        getOptionColour("ui", "backgroundcolour"));
    }



    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link FrameContainer#getTitle()} instead
     */
    @Deprecated
    @Override
    public String getTitle() {
        return getContainer().getTitle();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated MDI is no longer implemented, windows are always maximised.
     */
    @Override
    @Deprecated
    public boolean isMaximum() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Should use {@link FrameContainer#setTitle(java.lang.String)}
     */
    @Deprecated
    @Override
    public void setTitle(final String title) {
        getContainer().setTitle(title);
    }

    /** {@inheritDoc} */
    @Override
    public void open() {
        //Yay, we're open.
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Should not be used outside the UI, can be removed when the
     * interface method is removed
     */
    @Deprecated
    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Should not be used outside the UI, can be removed when the
     * interface method is removed
     */
    @Deprecated
    @Override
    public boolean isVisible() {
        return super.isVisible();
    }
}

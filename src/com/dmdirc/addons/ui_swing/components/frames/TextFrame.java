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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

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
    protected final FrameContainer frameParent;
    /** Frame output pane. */
    private TextPane textPane;
    /** search bar. */
    private SwingSearchBar searchBar;
    /** Command parser for popup commands. */
    private final CommandParser commandParser;
    /** Swing controller. */
    private final SwingController controller;
    /** Boolean to determine if this frame should be popped out of main client. */
    private boolean popout;
    /**
     * DesktopWindowFrame to use for this TextFrame if it is to be popped out of
     * the client.
     */
    private DesktopWindowFrame popoutFrame;

    /**
     * Creates a new instance of Frame.
     *
     * @param owner FrameContainer owning this frame.
     * @param controller Swing controller
     */
    public TextFrame(final FrameContainer owner,
            final SwingController controller) {
        super();
        this.controller = controller;
        this.frameParent = owner;

        final ConfigManager config = owner.getConfigManager();

        owner.addCloseListener(this);
        owner.setTitle(frameParent.getTitle());

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

    /**
     * Determines if this frame should be popped out of the client or not. Once
     * this is set to true it will pop out of the client as a free floating
     * Desktop window.
     *
     * If this is set to false then the desktop window for this frame is
     * disposed of and this frame is returned to the client.
     *
     * @param popout Should this frame pop out?
     */
    public void setPopout(final boolean popout) {
        this.popout = popout;
        if (popout) {
            createPopoutFrame();
        } else if (popoutFrame != null) {
            popoutFrame.setVisible(false);
            popoutFrame.dispose();
            popoutFrame = null;
        }
        // Call setActiveFrame again so the contents of the frame manager
        // are updated.
        if (equals(controller.getMainFrame()
                .getActiveFrame())) {
            controller.getMainFrame().setActiveFrame(this);
        }
    }

    /**
     * Returns the frame for the free floating desktop window associated with this
     * TextFrame. If one does not exist then null is returned.
     *
     * @return Desktop window frame or null if does not exist
     */
    public DesktopWindowFrame getPopoutFrame() {
        return popoutFrame;
    }

    /**
     * Sets the frame that has is to be used as our free floating window.
     *
     * @param popoutFrame frame that is to be used for free floating window
     */
    public void setPopoutFrame(final DesktopWindowFrame popoutFrame) {
        this.popoutFrame = popoutFrame;
    }

    /**
     * Creates a free floating window frame for us to use. This method will create
     * a place holder frame to be used in the client in place of the original frame.
     */
    private void createPopoutFrame() {
        if (popoutFrame == null) {
            popoutFrame = new DesktopWindowFrame(TextFrame.this,
                    new DesktopPlaceHolderFrame());
            popoutFrame.add(TextFrame.this, "grow");
            popoutFrame.pack();
            popoutFrame.setVisible(true);
            setPopoutFrame(popoutFrame);
        }
     }

    /**
     * Checks if this frame should be popped out of the client or not. Returns
     * our place holder frame if it is to be used or this TextFrame if it is
     * not to be popped out.
     *
     * @return JPanel to use by the client in the window pane
     */
    public JPanel getDisplayFrame() {
        if (popout) {
            if (popoutFrame == null) {
                createPopoutFrame();
            }
            return popoutFrame.getPlaceHolder();
        } else {
            return this;
        }
    }

    /**
     * Called when the frame has been selected in the UI.
     */
    public void activateFrame() {
        UIUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                frameParent.clearNotification();
            }
        });
    }

    /**
     * Initialises the components for this frame.
     */
    private void initComponents() {
        setTextPane(new TextPane(getController(), this));

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
    public FrameContainer getContainer() {
        return frameParent;
    }

    /**
     * Returns the text pane for this frame.
     *
     * @return Text pane for this frame
     */
    public final TextPane getTextPane() {
        return textPane;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        if (frameParent == null) {
            return "";
        }

        return frameParent.getName();
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
    public void mouseClicked(final ClickTypeValue clicktype,
            final MouseEventType eventType, final MouseEvent event) {
        if (event.isPopupTrigger()) {
            showPopupMenuInternal(clicktype, event.getPoint());
        }
        if (eventType == MouseEventType.CLICK
                && event.getButton() == MouseEvent.BUTTON1) {
            handleLinkClick(clicktype);
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
                if (frameParent.getServer() != null && ActionManager
                        .getActionManager().triggerEvent(
                        CoreActionType.LINK_CHANNEL_CLICKED, null, this,
                        clickType.getValue())) {
                    frameParent.getServer().join(
                            new ChannelJoinRequest(clickType.getValue()));
                }
                break;
            case HYPERLINK:
                if (ActionManager.getActionManager().triggerEvent(
                        CoreActionType.LINK_URL_CLICKED, null, this,
                        clickType.getValue())) {
                    controller.getURLHandler().launchApp(clickType.getValue());
                }
                break;
            case NICKNAME:
                if (frameParent.getServer() != null && ActionManager
                        .getActionManager().triggerEvent(
                        CoreActionType.LINK_NICKNAME_CLICKED, null, this,
                        clickType.getValue())) {
                    getController().requestWindowFocus(getController()
                            .getWindowFactory().getSwingWindow(getContainer()
                            .getServer().getQuery(clickType.getValue())));
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
                    PopupManager.getMenu(type, getContainer()
                    .getConfigManager()), arguments);
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
    public void windowClosing(final FrameContainer window) {
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
        getTextPane().setForeground(getContainer().getConfigManager()
                .getOptionColour("ui", "foregroundcolour"));
        getTextPane().setBackground(getContainer().getConfigManager()
                .getOptionColour("ui", "backgroundcolour"));
    }

    /** Disposes of this window, removing any listeners. */
    public void dispose() {
        frameParent.getConfigManager().removeListener(this);
        frameParent.removeCloseListener(this);
    }
}

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

import com.dmdirc.Query;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.CommandParser;
import com.dmdirc.ui.interfaces.QueryWindow;

import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * The QueryFrame is the MDI window that shows query messages to the user.
 */
public final class QueryFrame extends InputTextFrame implements QueryWindow {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 8;

    /**
     * Creates a new QueryFrame.
     *
     * @param owner Parent Frame container
     * @param controller Swing controller
     */
    public QueryFrame(final SwingController controller, final Query owner) {
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
        setLayout(new MigLayout("ins 0, fill, hidemode 3, wrap 1"));
        add(getTextPane(), "grow, push");
        add(getSearchBar(), "growx, pushx");
        add(inputPanel, "growx, pushx");
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNicknamePopupType() {
        return PopupType.QUERY_NICK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getChannelPopupType() {
        return PopupType.QUERY_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getHyperlinkPopupType() {
        return PopupType.QUERY_HYPERLINK;
    }

    /** {@inheritDoc} */
    @Override
    public PopupType getNormalPopupType() {
        return PopupType.QUERY_NORMAL;
    }

    /** {@inheritDoc} */
    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        //Add no custom popup items
    }
}

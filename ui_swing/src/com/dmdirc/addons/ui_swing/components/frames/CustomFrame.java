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

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.FrameContainer;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.commandparser.parsers.CommandParser;

import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * A very basic custom frame.
 */
public class CustomFrame extends TextFrame {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;

    /**
     * Creates a new instance of CustomFrame.
     *
     * @param deps          The dependencies required by text frames.
     * @param commandParser The parser to use for any input.
     * @param owner         The frame container that owns this frame
     */
    public CustomFrame(
            final TextFrameDependencies deps,
            final CommandParser commandParser,
            final FrameContainer owner) {
        super(owner, commandParser, deps);

        initComponents();
    }

    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        setLayout(new MigLayout("ins 0, fill, hidemode 3, wrap 1"));
        add(getTextPane(), "grow, push");
        add(getSearchBar(), "growx, pushx");
    }

    @Override
    public PopupType getNicknamePopupType() {
        return null;
    }

    @Override
    public PopupType getChannelPopupType() {
        return null;
    }

    @Override
    public PopupType getHyperlinkPopupType() {
        return null;
    }

    @Override
    public PopupType getNormalPopupType() {
        return null;
    }

    @Override
    public void addCustomPopupItems(final JPopupMenu popupMenu) {
        //Add no custom popup items
    }

}

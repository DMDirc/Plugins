/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.components.frames;

import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.interfaces.WindowModel;

import javax.inject.Provider;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * A custom frame that includes an input field (for use with writable containers).
 */
public class CustomInputFrame extends InputTextFrame {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;

    /**
     * Creates a new instance of CustomInputFrame.
     *
     * @param deps               The dependencies required by text frames.
     * @param inputFieldProvider The provider to use to create a new input field.
     * @param owner              The frame container that owns this frame
     */
    public CustomInputFrame(
            final TextFrameDependencies deps,
            final Provider<SwingInputField> inputFieldProvider,
            final InputTextFramePasteActionFactory inputTextFramePasteActionFactory,
            final WindowModel owner) {
        super(deps, inputFieldProvider, inputTextFramePasteActionFactory, owner);

        initComponents();
    }

    /**
     * Initialises the instance, adding any required listeners.
     */
    @Override
    public void init() {
        // TODO: Move adding listeners and things to here
        super.init();
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

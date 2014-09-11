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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.FrameContainer;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputField;
import com.dmdirc.commandparser.PopupType;
import com.dmdirc.util.URLBuilder;

import javax.inject.Provider;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

/**
 * A component frame that includes an input field (for use with writable containers).
 */
public class ComponentInputFrame extends InputTextFrame {

    /** A version number for this class. */
    private static final long serialVersionUID = 2;
    /** URL builder to use when making components. */
    private final URLBuilder urlBuilder;
    /** Parent frame container. */
    private final FrameContainer owner;
    /** Parent controller. */
    private final SwingController controller;
    /** The global event bus. */
    private final DMDircMBassador eventBus;

    /**
     * Creates a new instance of CustomInputFrame.
     *
     * @param eventBus           The global event bus
     * @param deps               The dependencies required by text frames.
     * @param inputFieldProvider The provider to use to create a new input field.
     * @param urlBuilder         URL builder to use when making components.
     * @param owner              The frame container that owns this frame
     */
    public ComponentInputFrame(
            final DMDircMBassador eventBus,
            final TextFrameDependencies deps,
            final Provider<SwingInputField> inputFieldProvider,
            final URLBuilder urlBuilder,
            final FrameContainer owner) {
        super(deps, inputFieldProvider, owner);
        this.eventBus = eventBus;
        this.controller = deps.controller;
        this.urlBuilder = urlBuilder;
        this.owner = owner;
        initComponents();
    }

    /**
     * Initialises components in this frame.
     */
    private void initComponents() {
        setLayout(new MigLayout("fill"));
        for (JComponent comp : new ComponentCreator()
                .initFrameComponents(this, controller, eventBus, urlBuilder, owner)) {
            add(comp, "wrap, grow");
        }
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

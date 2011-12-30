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

package com.dmdirc.addons.dcc;

import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.config.ConfigManager;

import java.awt.Dialog.ModalityType;
import java.util.Arrays;

/**
 * Creates a placeholder DCC Frame.
 */
public class PlaceholderContainer extends FrameContainer {

    /** The plugin which owns this placeholder. */
    private final DCCPlugin plugin;
    /** Parent swing controller. */
    private final SwingController controller;

    /**
     * Creates a placeholder DCC frame.
     *
     * @param plugin The plugin which owns this placeholder
     * @param config Config manager
     * @param controller Swing controller
     */
    public PlaceholderContainer(final DCCPlugin plugin,
            final ConfigManager config, final SwingController controller) {
        super("dcc", "DCCs", "DCCs", config, Arrays.asList(
                "com.dmdirc.addons.dcc.ui.PlaceholderPanel"));
        this.plugin = plugin;
        this.controller = controller;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        int dccs = 0;
        for (FrameContainer window : getChildren()) {
            if ((window instanceof TransferContainer
                    && ((TransferContainer) window).getDCC().isActive())
                    || (window instanceof ChatContainer
                    && ((ChatContainer) window).getDCC().isActive())) {
                dccs++;
            }
        }

        if (dccs > 0) {
            new StandardQuestionDialog(controller, ModalityType.MODELESS,
                    "Close confirmation",
                    "Closing this window will cause all existing DCCs "
                    + "to terminate, are you sure you want to do this?") {
                /**
                 * A version number for this class. It should be changed
                 * whenever the class structure is changed (or anything else
                 * that would prevent serialized objects being unserialized
                 * with the new class).
                 */
                private static final long serialVersionUID = 1;

                /** {@inheritDoc} */
                @Override
                public boolean save() {
                    PlaceholderContainer.super.close();
                    return true;
                }

                /** {@inheritDoc} */
                @Override
                public void cancelled() {
                    // Don't close!
                }
            }.display();
        } else {
            super.close();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Server getServer() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosing() {
        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
        plugin.removeContainer();
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
    }

    /** {@inheritDoc} */
    @Override
    public void removeChild(final FrameContainer child) {
        super.removeChild(child);

        if (getChildren().isEmpty()) {
            close();
        }
    }
}

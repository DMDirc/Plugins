/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.addons.dcc.ui.PlaceholderWindow;
import com.dmdirc.addons.ui_swing.MainFrame;
import com.dmdirc.addons.ui_swing.dialogs.StandardQuestionDialog;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.interfaces.Window;
import java.awt.Dialog.ModalityType;

/**
 * Creates a placeholder DCC Frame.
 */
public class PlaceholderContainer extends FrameContainer<PlaceholderWindow> {

    /** The plugin which owns this placeholder. */
    private final DCCPlugin plugin;

    /**
     * Creates a placeholder dcc frame.
     *
     * @param plugin The plugin which owns this placeholder
     */
    public PlaceholderContainer(final DCCPlugin plugin) {
        super("dcc", "DCCs", "DCCs", PlaceholderWindow.class, IdentityManager.getGlobalConfig());

        this.plugin = plugin;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        int dccs = 0;
        for (FrameContainer<?> window : getChildren()) {
            if (window instanceof TransferContainer) {
                if (((TransferContainer) window).getDCC().isActive()) {
                    dccs++;
                }
            } else if (window instanceof ChatContainer) {
                if (((ChatContainer) window).getDCC().isActive()) {
                    dccs++;
                }
            }
        }

        if (dccs > 0) {
            new StandardQuestionDialog(
                    (MainFrame) Main.getUI().getMainWindow(),
                    ModalityType.MODELESS, "Close confirmation",
                    "Closing this window will cause all existing DCCs " +
                    "to terminate, are you sure you want to do this?") {
                /**
                 * A version number for this class. It should be changed whenever the class
                 * structure is changed (or anything else that would prevent serialized
                 * objects being unserialized with the new class).
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
        // 1: Make the window non-visible
        for (Window window : getWindows()) {
            window.setVisible(false);
        }

        // 2: Remove any callbacks or listeners
        // 3: Trigger any actions neccessary
        // 4: Trigger action for the window closing
        // 5: Inform any parents that the window is closing
        plugin.removeContainer();

        // 6: Remove the window from the window manager
        WindowManager.removeWindow(this);
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed() {
        // 7: Remove any references to the window and parents
    }

    /** {@inheritDoc} */
    @Override
    public void removeChild(final FrameContainer<?> child) {
        super.removeChild(child);

        if (getChildren().isEmpty()) {
            close();
        }
    }
}
/*
 * Copyright (c) 2006-2012 DMDirc Developers
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

package com.dmdirc.addons.ui_swing.components.statusbar;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingUpdaterDialog;
import com.dmdirc.interfaces.ui.StatusBarComponent;
import com.dmdirc.ui.IconManager;
import com.dmdirc.updater.UpdateChecker;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.updater.manager.UpdateManagerListener;
import com.dmdirc.updater.manager.UpdateManagerStatus;

import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Updater label is responsible for handling the display of updates in the
 * status bar.
 */
public class UpdaterLabel extends StatusbarPopupPanel<JLabel> implements
        StatusBarComponent, UpdateManagerListener {

    /**
     * A version number for this class. It should be changed whenever the class
     * structure is changed (or anything else that would prevent serialized
     * objects being unserialized with the new class).
     */
    private static final long serialVersionUID = 1;
    /** Swing controller. */
    private final SwingController controller;

    /**
     * Instantiates a new updater label, handles showing updates on the status
     * bar.
     *
     * @param controller Swing controller
     */
    public UpdaterLabel(final SwingController controller) {
        super(new JLabel());

        this.controller = controller;
        setBorder(BorderFactory.createEtchedBorder());
        UpdateChecker.getManager().addUpdateManagerListener(this);
        setVisible(false);
        label.setText(null);
    }

    /**
     * {@inheritDoc}
     *
     * @param mouseEvent Mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            if (UpdateChecker.getManager().getManagerStatus()
                    == UpdateManagerStatus.IDLE_RESTART_NEEDED) {
                closeDialog();
                controller.showDialog(SwingRestartDialog.class);
            } else {
                controller.showDialog(SwingUpdaterDialog.class,
                        UpdateChecker.getManager());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected StatusbarPopupWindow getWindow() {
        return new UpdaterPopup(controller, this, controller.getMainFrame());
    }

    /** {@inheritDoc} */
    @Override
    public void updateManagerStatusChanged(final UpdateManager manager,
            final UpdateManagerStatus status) {
        if (status == UpdateManagerStatus.IDLE) {
            setVisible(false);
        } else {
            setVisible(true);
        }

        if (status == UpdateManagerStatus.WORKING) {
            label.setIcon(new IconManager(controller.getGlobalConfig())
                    .getIcon("hourglass"));
        } else if (status == UpdateManagerStatus.IDLE_UPDATE_AVAILABLE) {
            label.setIcon(new IconManager(controller.getGlobalConfig())
                    .getIcon("update"));
        } else if (status == UpdateManagerStatus.IDLE_RESTART_NEEDED) {
            label.setIcon(new IconManager(controller.getGlobalConfig())
                    .getIcon("restart-needed"));
        }
    }
}

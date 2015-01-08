/*
 * Copyright (c) 2006-2015 DMDirc Developers
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

import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
import com.dmdirc.addons.ui_swing.dialogs.updater.SwingUpdaterDialog;
import com.dmdirc.addons.ui_swing.injection.DialogModule.ForUpdates;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.ui.IconManager;
import com.dmdirc.updater.manager.CachingUpdateManager;
import com.dmdirc.updater.manager.UpdateManager;
import com.dmdirc.updater.manager.UpdateManagerListener;
import com.dmdirc.updater.manager.UpdateManagerStatus;

import java.awt.Window;
import java.awt.event.MouseEvent;

import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * Updater label is responsible for handling the display of updates in the status bar.
 */
public class UpdaterLabel extends StatusbarPopupPanel<JLabel> implements UpdateManagerListener {

    /** A version number for this class. */
    private static final long serialVersionUID = 1;
    /** The manager to use to retrieve icons. */
    private final IconManager iconManager;
    /** The parent that will own any popup windows. */
    private final Window parentWindow;
    /** The update manager to use to retrieve information. */
    private final CachingUpdateManager updateManager;
    /** Provider of updater dialogs. */
    private final DialogProvider<SwingUpdaterDialog> updaterDialogProvider;
    /** Provider of restart dialogs. */
    private final DialogProvider<SwingRestartDialog> restartDialogProvider;

    /**
     * Instantiates a new updater label, handles showing updates on the status bar.
     *
     * @param iconManager           The manager to use to retrieve icons.
     * @param parentWindow          The parent that will own any popup windows.
     * @param updateManager         The manager to use to retrieve information.
     * @param updaterDialogProvider Provider of updater dialogs.
     * @param restartDialogProvider Provider of restart dialogs.
     */
    @Inject
    public UpdaterLabel(
            final IconManager iconManager,
            @MainWindow final Window parentWindow,
            final CachingUpdateManager updateManager,
            final DialogProvider<SwingUpdaterDialog> updaterDialogProvider,
            @ForUpdates final DialogProvider<SwingRestartDialog> restartDialogProvider) {
        super(new JLabel());

        this.iconManager = iconManager;
        this.parentWindow = parentWindow;
        this.updateManager = updateManager;
        this.updaterDialogProvider = updaterDialogProvider;
        this.restartDialogProvider = restartDialogProvider;
        setBorder(BorderFactory.createEtchedBorder());
        updateManager.addUpdateManagerListener(this);
        setVisible(false);
        label.setText(null);
    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            if (updateManager.getManagerStatus() == UpdateManagerStatus.IDLE_RESTART_NEEDED) {
                closeDialog();
                restartDialogProvider.displayOrRequestFocus();
            } else {
                updaterDialogProvider.displayOrRequestFocus();
            }
        }
    }

    @Override
    protected StatusbarPopupWindow getWindow() {
        return new UpdaterPopup(updateManager, this, parentWindow);
    }

    @Override
    public void updateManagerStatusChanged(final UpdateManager manager,
            final UpdateManagerStatus status) {
        if (status == UpdateManagerStatus.IDLE) {
            setVisible(false);
        } else {
            setVisible(true);
        }

        if (status == UpdateManagerStatus.WORKING) {
            label.setIcon(iconManager.getIcon("hourglass"));
        } else if (status == UpdateManagerStatus.IDLE_UPDATE_AVAILABLE) {
            label.setIcon(iconManager.getIcon("update"));
        } else if (status == UpdateManagerStatus.IDLE_RESTART_NEEDED) {
            label.setIcon(iconManager.getIcon("restart-needed"));
        }
    }

}

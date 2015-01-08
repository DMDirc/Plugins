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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.addons.ui_swing.EdtHandlerInvocation;
import com.dmdirc.addons.ui_swing.components.menubar.JMenuItemBuilder;
import com.dmdirc.addons.ui_swing.dialogs.errors.ErrorsDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.events.NonFatalProgramErrorEvent;
import com.dmdirc.events.ProgramErrorDeletedEvent;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.ErrorManager;
import com.dmdirc.logger.ProgramError;
import com.dmdirc.ui.IconManager;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import net.engio.mbassy.listener.Handler;

/**
 * Shows error status in the status bar.
 */
@Singleton
public class ErrorPanel extends StatusbarPopupPanel<JLabel> {

    /** Serial version UID. */
    private static final long serialVersionUID = 2;
    /** non error state image icon. */
    private final Icon defaultIcon;
    /** Parent window that will own popups. */
    private final Window parentWindow;
    /** The manager to use to retrieve icons. */
    private final IconManager iconManager;
    /** Error manager. */
    private final ErrorManager errorManager;
    /** Dismiss menu. */
    private final JPopupMenu menu;
    /** Error list dialog provider. */
    private final DialogProvider<ErrorsDialog> errorListDialogProvider;
    /** The event bus to listen to error changes on .*/
    private final DMDircMBassador eventBus;
    /** Currently showing error level. */
    private ErrorLevel errorLevel;
    private boolean hasBeenVisible;

    /**
     * Creates a new ErrorPanel for the specified status bar.
     *
     * @param iconManager             The manager to use to retrieve icons.
     * @param parentWindow            Main frame
     * @param errorListDialogProvider Error list dialog provider.
     */
    @Inject
    public ErrorPanel(
            final IconManager iconManager,
            @MainWindow final Window parentWindow,
            final DialogProvider<ErrorsDialog> errorListDialogProvider,
            final ErrorManager errorManager,
            final DMDircMBassador eventBus) {
        super(new JLabel());
        this.parentWindow = parentWindow;
        this.iconManager = iconManager;
        this.errorListDialogProvider = errorListDialogProvider;
        this.errorManager = errorManager;
        this.eventBus = eventBus;
        defaultIcon = iconManager.getIcon("normal");

        menu = new JPopupMenu();
        label.setIcon(defaultIcon);
        setVisible(errorManager.getErrorCount() > 0);
        menu.add(JMenuItemBuilder.create().setText("Open")
                .addActionListener(e -> errorListDialogProvider.displayOrRequestFocus()).build());
        menu.add(JMenuItemBuilder.create()
                .setText("Clear All")
                .addActionListener(e -> errorManager.getErrors().forEach(errorManager::deleteError))
                .build());
        checkErrors();
    }

    @Override
    protected StatusbarPopupWindow getWindow() {
        return new ErrorPopup(errorManager, iconManager, this, parentWindow);
    }

    @Override
    public void setVisible(final boolean visible) {
        if (!hasBeenVisible) {
            hasBeenVisible = true;
            eventBus.subscribe(this);
        }
        super.setVisible(visible);
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void errorAdded(final NonFatalProgramErrorEvent event) {
        checkErrors();
    }

    @Handler(invocation = EdtHandlerInvocation.class)
    public void errorDeleted(final ProgramErrorDeletedEvent event) {
        checkErrors();
    }

    /** Checks all the errors for the most significant error. */
    private void checkErrors() {
        label.setIcon(defaultIcon);
        errorLevel = null;
        final Set<ProgramError> errors = errorManager.getErrors();

        if (errors.isEmpty()) {
            setVisible(false);
        } else {
            errors.stream().filter(error -> errorLevel == null ||
                    !error.getLevel().moreImportant(errorLevel)).forEach(error -> {
                errorLevel = error.getLevel();
                label.setIcon(iconManager.getIcon(errorLevel.getIcon()));
            });
            setVisible(true);
        }
    }

    @Override
    public void mousePressed(final MouseEvent mouseEvent) {
        super.mousePressed(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    @Override
    public void mouseReleased(final MouseEvent mouseEvent) {
        super.mouseReleased(mouseEvent);
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            errorListDialogProvider.displayOrRequestFocus();
        }
        checkMouseEvent(mouseEvent);
    }

    @Override
    public void mouseEntered(final MouseEvent mouseEvent) {
        super.mouseEntered(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    @Override
    public void mouseExited(final MouseEvent mouseEvent) {
        super.mouseExited(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    @Override
    public void mouseClicked(final MouseEvent mouseEvent) {
        super.mouseClicked(mouseEvent);
        checkMouseEvent(mouseEvent);
    }

    /**
     * Checks a mouse event for a popup trigger.
     *
     * @param e Mouse event
     */
    private void checkMouseEvent(final MouseEvent e) {
        if (e.isPopupTrigger()) {
            menu.show(this, e.getX(), e.getY());
        }
    }

}

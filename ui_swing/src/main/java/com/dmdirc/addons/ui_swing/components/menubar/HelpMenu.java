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

package com.dmdirc.addons.ui_swing.components.menubar;

import com.dmdirc.addons.ui_swing.Apple;
import com.dmdirc.addons.ui_swing.dialogs.about.AboutDialog;
import com.dmdirc.addons.ui_swing.dialogs.feedback.FeedbackDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.interfaces.ConnectionManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JMenu;

/**
 * A menu providing help commands to the menu bar.
 */
@Singleton
public class HelpMenu extends JMenu {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Server manager to use to join dev chat. */
    private final ConnectionManager connectionManager;
    /** Provider of feedback dialogs. */
    private final DialogProvider<FeedbackDialog> feedbackDialogProvider;
    /** Provider of about dialogs. */
    private final DialogProvider<AboutDialog> aboutDialogProvider;

    /**
     * Instantiates a new help menu.
     *
     * @param connectionManager          The manager to use to join dev chat.
     * @param feedbackDialogProvider Provider of feedback dialogs.
     * @param aboutDialogProvider    Provider of about dialogs.
     */
    @Inject
    public HelpMenu(
            final ConnectionManager connectionManager,
            final DialogProvider<FeedbackDialog> feedbackDialogProvider,
            final DialogProvider<AboutDialog> aboutDialogProvider) {
        super("Help");
        this.connectionManager = connectionManager;
        this.feedbackDialogProvider = feedbackDialogProvider;
        this.aboutDialogProvider = aboutDialogProvider;
        setMnemonic('h');
        initHelpMenu();
    }

    /**
     * Initialises the help menu.
     */
    private void initHelpMenu() {
        add(JMenuItemBuilder.create()
                .setMnemonic('j')
                .setText("Join Dev channel")
                .addActionMethod(connectionManager::joinDevChat)
                .build());
        add(JMenuItemBuilder.create().setMnemonic('f')
                .setText("Send Feedback")
                .addActionMethod(feedbackDialogProvider::displayOrRequestFocus).build());
        if (!Apple.isAppleUI()) {
            add(JMenuItemBuilder.create()
                    .setMnemonic('a')
                    .setText("About")
                    .addActionMethod(aboutDialogProvider::displayOrRequestFocus).build());
        }
    }
}

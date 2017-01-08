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
import com.dmdirc.addons.ui_swing.dialogs.aliases.AliasManagerDialog;
import com.dmdirc.addons.ui_swing.dialogs.globalautocommand.GlobalAutoCommandDialog;
import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
import com.dmdirc.addons.ui_swing.dialogs.profile.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JMenu;

/**
 * A menu to add settings related commands to the menu bar.
 */
@Singleton
public class SettingsMenu extends JMenu {

    /** Serial version UID. */
    private static final long serialVersionUID = 1;
    /** Provider of profile manager dialogs. */
    private final DialogProvider<ProfileManagerDialog> profileDialogProvider;
    /** Provider of preferences dialogs. */
    private final DialogProvider<SwingPreferencesDialog> prefsDialogProvider;
    /** Provider of alias manager dialogs. */
    private final DialogProvider<AliasManagerDialog> aliasDialogProvider;
    private final DialogProvider<GlobalAutoCommandDialog> globalAutoCommandDialogDialogProvider;

    @Inject
    public SettingsMenu(
            final DialogProvider<ProfileManagerDialog> profileDialogProvider,
            final DialogProvider<SwingPreferencesDialog> prefsDialogProvider,
            final DialogProvider<AliasManagerDialog> aliasDialogProvider,
            final DialogProvider<GlobalAutoCommandDialog> globalAutoCommandDialogDialogProvider) {
        super("Settings");
        this.profileDialogProvider = profileDialogProvider;
        this.prefsDialogProvider = prefsDialogProvider;
        this.aliasDialogProvider = aliasDialogProvider;
        this.globalAutoCommandDialogDialogProvider = globalAutoCommandDialogDialogProvider;

        setMnemonic('e');
        initSettingsMenu();
    }

    /**
     * Initialises the settings menu.
     */
    private void initSettingsMenu() {
        if (!Apple.isAppleUI()) {
            add(JMenuItemBuilder.create().setText("Preferences").setMnemonic('p')
                    .addActionMethod(prefsDialogProvider::displayOrRequestFocus)
                    .build());
        }
        add(JMenuItemBuilder.create()
                .setMnemonic('p')
                .addActionMethod(profileDialogProvider::displayOrRequestFocus)
                .setText("Profile Manager")
                .build());
        add(JMenuItemBuilder.create()
                .setMnemonic('a').setText("Alias Manager")
                .addActionMethod(aliasDialogProvider::displayOrRequestFocus)
                .build());
        add(JMenuItemBuilder.create()
                .setMnemonic('e').setText("Perform")
                .addActionMethod(globalAutoCommandDialogDialogProvider::displayOrRequestFocus)
                .build());
    }

}

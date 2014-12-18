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

package com.dmdirc.addons.ui_swing.wizard.firstrun;

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.ClientModule.UserConfig;
import com.dmdirc.actions.CoreActionExtractor;
import com.dmdirc.addons.ui_swing.dialogs.profile.ProfileManagerDialog;
import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.injection.MainWindow;
import com.dmdirc.addons.ui_swing.wizard.WizardDialog;
import com.dmdirc.addons.ui_swing.wizard.WizardListener;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.ui.FirstRunWizard;
import com.dmdirc.plugins.CorePluginExtractor;
import com.dmdirc.ui.IconManager;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

/** First run wizard, used to initially setup the client for the user. */
@Singleton
public class SwingFirstRunWizard implements WizardListener, FirstRunWizard {

    /** Wizard dialog. */
    private final WizardDialog wizardDialog;
    /** Global config. */
    private final ConfigProvider config;
    /** Extractor to use for core plugins. */
    private final CorePluginExtractor corePluginExtractor;
    /** Provider to use to obtain PMDs. */
    private final DialogProvider<ProfileManagerDialog> profileDialogProvider;
    /** Core Actions Extractor. */
    private final CoreActionExtractor coreActionExtractor;

    /**
     * Instantiate the wizard.
     *
     * @param parentWindow          Parent window
     * @param config                Global config
     * @param pluginExtractor       Plugin extractor to use.
     * @param iconManager           Manager to use to find icons.
     * @param profileDialogProvider Provider to use to obtain PMDs.
     */
    @Inject
    public SwingFirstRunWizard(@MainWindow final Window parentWindow,
            @UserConfig final ConfigProvider config,
            final CorePluginExtractor pluginExtractor, @GlobalConfig final IconManager iconManager,
            final DialogProvider<ProfileManagerDialog> profileDialogProvider,
            final CoreActionExtractor coreActionExtractor) {
        this.corePluginExtractor = pluginExtractor;
        this.config = config;
        this.profileDialogProvider = profileDialogProvider;
        this.coreActionExtractor = coreActionExtractor;

        wizardDialog = new WizardDialog("Setup wizard", new ArrayList<>(), parentWindow,
                ModalityType.APPLICATION_MODAL);
        wizardDialog.setIconImage(iconManager.getImage("icon"));
        wizardDialog.addWizardListener(this);
        wizardDialog.setMinimumSize(new Dimension(400, 500));
    }

    @Override
    public void wizardFinished() {
        if (((ExtractionStep) wizardDialog.getStep(0)).getPluginsState()) {
            extractPlugins();
        }
        if (((ExtractionStep) wizardDialog.getStep(0)).getActionsState()) {
            extractActions();
        }

        config.setOption("updater", "enable",
                ((CommunicationStep) wizardDialog.getStep(1)).checkUpdates());
        config.setOption("general", "submitErrors",
                ((CommunicationStep) wizardDialog.getStep(1)).checkErrors());

        if (((ProfileStep) wizardDialog.getStep(2)).getProfileManagerState()) {
            profileDialogProvider.displayOrRequestFocus();
        }
        wizardDialog.dispose();
    }

    @Override
    public void wizardCancelled() {
        wizardDialog.dispose();
    }

    @Override
    public void extractPlugins() {
        corePluginExtractor.extractCorePlugins(null);
    }

    @Override
    public void extractActions() {
        coreActionExtractor.extractCorePlugins();
    }

    @Override
    public void display() {
        wizardDialog.addStep(new FirstRunExtractionStep());
        wizardDialog.addStep(new CommunicationStep());
        wizardDialog.addStep(new ProfileStep());
        wizardDialog.display();
    }

    /**
     * Returns the dialog associated with this wizard.
     *
     * @return Associated wizard dialog
     */
    public WizardDialog getWizardDialog() {
        return wizardDialog;
    }

}

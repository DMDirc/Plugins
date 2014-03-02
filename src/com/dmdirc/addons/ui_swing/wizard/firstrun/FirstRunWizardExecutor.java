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

import com.dmdirc.addons.ui_swing.UIUtilities;
import com.dmdirc.addons.ui_swing.wizard.WizardListener;

import java.util.concurrent.Semaphore;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Utility to create and run a first-run wizard.
 */
public class FirstRunWizardExecutor {

    /** Semaphore used to signal when the dialog is complete. */
    private final Semaphore semaphore = new Semaphore(0);
    /** The provider of actual first-run wizards. */
    private final Provider<SwingFirstRunWizard> wizardProvider;

    /**
     * Creates a new instance of {@link FirstRunWizardExecutor}.
     *
     * @param wizardProvider The provider of first-run wizards to use.
     */
    @Inject
    public FirstRunWizardExecutor(final Provider<SwingFirstRunWizard> wizardProvider) {
        this.wizardProvider = wizardProvider;
    }

    /**
     * Shows the wizard and waits for it to finish.
     */
    public void showWizardAndWait() {
        UIUtilities.invokeLater(new FirstRunWizardRunnable());
        semaphore.acquireUninterruptibly();
    }

    /**
     * Creates a new wizard, adds a listener, and displays the wizard.
     */
    private class FirstRunWizardRunnable implements Runnable {

        @Override
        public void run() {
            final SwingFirstRunWizard wizard = wizardProvider.get();
            wizard.getWizardDialog().addWizardListener(new FirstRunWizardListener());
            wizard.display();
        }

    }

    /**
     * Listens for wizard finished events and releases the semaphore.
     */
    private class FirstRunWizardListener implements WizardListener {

        @Override
        public void wizardFinished() {
            semaphore.release();
        }

        @Override
        public void wizardCancelled() {
            semaphore.release();
        }

    }

}

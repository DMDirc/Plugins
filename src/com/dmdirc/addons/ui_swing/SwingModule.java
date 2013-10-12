/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.ClientModule;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides Swing-specific dependencies.
 */
@Module(addsTo = ClientModule.class, library = true)
public class SwingModule {

    /** The controller to return to clients. */
    private final SwingController controller;

    /**
     * Creates a new instance of {@link SwingModule}.
     *
     * @param controller The controller to return. This should be removed when SwingController
     * is separated from the plugin implementation.
     */
    public SwingModule(final SwingController controller) {
        this.controller = controller;
    }

    /**
     * Gets the swing controller to use.
     *
     * @return The swing controller.
     */
    @Provides
    public SwingController getController() {
        return controller;
    }

    /**
     * Gets the main DMDirc window.
     *
     * @return The main window.
     */
    @Provides
    public MainFrame getMainFrame() {
        return controller.getMainFrame();
    }

}

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

package com.dmdirc.addons.ui_dummy;

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Main;
import com.dmdirc.Server;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.ui.core.components.StatusBarManager;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.plugins.PluginManager;

import java.net.URI;

import lombok.Getter;

/**
 * Implements a dummy UI controller.
 */
public final class DummyController extends BasePlugin implements UIController {

    /** The main that owns us. */
    private final Main main;
    /** Plugin manager. */
    @Getter
    private final PluginManager pluginManager;

    /**
     * Creates a new instance of DummyController.
     *
     * @param main Main
     * @param pluginManager Plugin manager
     */
    public DummyController(final Main main, final PluginManager pluginManager) {
        super();
        this.main = main;
        this.pluginManager = pluginManager;
        StatusBarManager.getStatusBarManager().registerStatusBar(new DummyStatusBar());
    }

    /** {@inheritDoc} */
    @Override
    public Main getMain() {
        return main;
    }

    /** {@inheritDoc} */
    @Override
    public void showFirstRunWizard() {
        System.out.println("DummyController.showFirstRunWizard()");
    }

    /** {@inheritDoc} */
    @Override
    public void showChannelSettingsDialog(final Channel channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void showServerSettingsDialog(final Server server) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void showURLDialog(final URI url) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void showFeedbackNag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void showMessageDialog(final String title, final String message) {
        System.out.println(message);
    }

    /**
     * Returns an instance of DummyController. This method is exported for use
     * in other plugins.
     *
     * @return A reference to this DummyController.
     */
    public UIController getController() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void requestWindowFocus(final Window window) {
        // Ignore
    }

    /** {@inheritDoc} */
    @Override
    public void requestWindowFocus(final FrameContainer container) {
        // Ignore
    }
}

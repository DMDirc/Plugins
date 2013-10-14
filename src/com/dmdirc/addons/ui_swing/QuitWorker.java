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

import com.dmdirc.ServerManager;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;

import javax.inject.Inject;
import javax.swing.SwingWorker;

/**
 * Worker which handles quitting the application on behalf of a {@link MainFrame}.
 */
public class QuitWorker extends SwingWorker<Void, Void> {

    /** The identity to write settings to. */
    private final ConfigProvider globalIdentity;
    /** The config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** The server manager to use to disconnect all servers. */
    private final ServerManager serverManager;
    /** The main frame to interact with. */
    private final MainFrame mainFrame;

    /**
     * Creates a new {@link QuitWorker}.
     *
     * @param identityController The identity controller to use to read/write settings.
     * @param serverManager The server manager to use to disconnect all servers.
     * @param mainFrame The main frame to interact with.
     */
    @Inject
    public QuitWorker(
            final IdentityController identityController,
            final ServerManager serverManager,
            final MainFrame mainFrame) {
        this.globalIdentity = identityController.getUserSettings();
        this.globalConfig = identityController.getGlobalConfiguration();
        this.serverManager = serverManager;
        this.mainFrame = mainFrame;
    }

    /** {@inheritDoc} */
    @Override
    protected Void doInBackground() {
        ActionManager.getActionManager().triggerEvent(
                CoreActionType.CLIENT_CLOSING, null);
        serverManager.closeAll(globalConfig.getOption("general", "closemessage"));
        globalIdentity.setOption("ui", "frameManagerSize",
                String.valueOf(mainFrame.getFrameManagerSize()));
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected void done() {
        super.done();
        mainFrame.dispose();
    }

    /**
     * Execute this swing worker in the swing worker executor.
     */
    public void executeInExecutor() {
        SwingWorkerExecutor.queue(this);
    }

}

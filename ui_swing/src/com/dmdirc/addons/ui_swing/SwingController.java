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

package com.dmdirc.addons.ui_swing;

import com.dmdirc.addons.ui_swing.commands.ChannelSettings;
import com.dmdirc.addons.ui_swing.commands.Input;
import com.dmdirc.addons.ui_swing.commands.PopInCommand;
import com.dmdirc.addons.ui_swing.commands.PopOutCommand;
import com.dmdirc.addons.ui_swing.commands.ServerSettings;
import com.dmdirc.addons.ui_swing.framemanager.FrameManagerProvider;
import com.dmdirc.addons.ui_swing.injection.SwingModule;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.plugins.Exported;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import java.awt.GraphicsEnvironment;

import dagger.ObjectGraph;

/**
 * Controls the main swing UI.
 */
public class SwingController extends BaseCommandPlugin implements UIController {

    /** The manager we're using for dependencies. */
    private SwingManager swingManager;

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new SwingModule(this, pluginInfo, pluginInfo.getDomain())));
        getObjectGraph().validate();
        swingManager = getObjectGraph().get(SwingManager.class);

        registerCommand(ServerSettings.class, ServerSettings.INFO);
        registerCommand(ChannelSettings.class, ChannelSettings.INFO);
        registerCommand(Input.class, Input.INFO);
        registerCommand(PopOutCommand.class, PopOutCommand.INFO);
        registerCommand(PopInCommand.class, PopInCommand.INFO);
    }

    @Override
    public void onLoad() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Swing UI can't be run in a headless environment");
        }

        swingManager.load();

        UIUtilities.invokeAndWait(() -> getMainFrame().setVisible(true));

        super.onLoad();
    }

    @Override
    public void onUnload() {
        if (swingManager != null) {
            swingManager.unload();
        }

        super.onUnload();
    }

    /**
     * Returns an instance of SwingController. This method is exported for use in other plugins.
     *
     * @return A reference to this SwingController.
     */
    @Exported
    public UIController getController() {
        return this;
    }

    /**
     * Returns the exported tree manager provider.
     *
     * @return A tree manager provider.
     */
    @Exported
    public FrameManagerProvider getTreeManager() {
        return swingManager.getTreeProvider();
    }

    /**
     * Retrieves the main frame to use.
     *
     * @return The main frame to use.
     *
     * @deprecated Should be injected where needed.
     */
    @Deprecated
    public MainFrame getMainFrame() {
        return swingManager.getMainFrame();
    }

}

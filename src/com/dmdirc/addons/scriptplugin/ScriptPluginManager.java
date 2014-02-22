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

package com.dmdirc.addons.scriptplugin;

import com.dmdirc.actions.CoreActionType;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.interfaces.ActionController;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.actions.ActionType;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.script.ScriptEngineManager;

public class ScriptPluginManager implements ActionListener {

    private final ActionController actionController;
    private final String scriptDir;
    private final ScriptManager scriptManager;
    private final TypedProperties globalVariables;

    @Inject
    public ScriptPluginManager(final ActionController actionController,
            @Directory(ScriptModule.SCRIPTS) final String scriptDir,
            final ScriptManager scriptManager,
            final ScriptEngineManager scriptEngineManager) {
        this.actionController = actionController;
        this.scriptDir = scriptDir;
        this.scriptManager = scriptManager;
        globalVariables = (TypedProperties) scriptEngineManager.get("globalVariables");
    }

    public void onLoad() {
        // Register the plugin_loaded action initially, this will be called
        // after this method finishes for us to register the rest.
        actionController.registerListener(this, CoreActionType.PLUGIN_LOADED);

        // Make sure our scripts dir exists
        final File newDir = new File(scriptDir);
        if (!newDir.exists()) {
            newDir.mkdirs();
        }

        final File savedVariables = new File(scriptDir + "storedVariables");
        if (savedVariables.exists()) {
            try (FileInputStream fis = new FileInputStream(savedVariables)) {
                globalVariables.load(fis);
            } catch (IOException e) {
                Logger.userError(ErrorLevel.LOW, "Error reading savedVariables from '"
                        + savedVariables.getPath() + "': " + e.getMessage(), e);
            }
        }
    }

    public void onUnLoad() {
        actionController.unregisterListener(this);

        final File savedVariables = new File(scriptDir + "storedVariables");
        try (FileOutputStream fos = new FileOutputStream(savedVariables)) {
            globalVariables.store(fos, "# DMDirc Script Plugin savedVariables");
        } catch (IOException e) {
            Logger.userError(ErrorLevel.LOW, "Error reading savedVariables to '" + savedVariables.
                    getPath() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Register all the action types. This will unregister all the actions first.
     */
    private void registerAll() {
        actionController.unregisterListener(this);
        for (Map.Entry<String, List<ActionType>> entry
                : actionController.getGroupedTypes().entrySet()) {
            final List<ActionType> types = entry.getValue();
            actionController.registerListener(this, types.toArray(new ActionType[types.size()]));
        }
    }

    @Override
    public void processEvent(final ActionType type, final StringBuffer format,
            final Object... arguments) {
        // Plugins may to register/unregister action types, so lets reregister all
        // the action types, except Plugin loaded and plugin unloaded.
        if (type.equals(CoreActionType.PLUGIN_LOADED) || type.equals(CoreActionType.PLUGIN_UNLOADED)) {
            registerAll();
        }
        scriptManager.callFunctionAll("action_" + type.toString().toLowerCase(), arguments);
    }

}

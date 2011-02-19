/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.Main;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
import com.dmdirc.actions.interfaces.ActionType;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.util.validators.ValidationResponse;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.logger.ErrorLevel;
import com.dmdirc.logger.Logger;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.util.StreamUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineManager;

/**
 * This allows javascript scripts to be used in DMDirc.
 *
 * @author Shane 'Dataforce' McCormack
 */
public final class ScriptPlugin extends BasePlugin implements ActionListener {
    /** The ScriptCommand we created */
    private ScriptCommand command = null;

    /** Script Directory */
    private final String scriptDir = Main.getConfigDir() + "scripts/";

    /** Script Engine Manager */
    private ScriptEngineManager scriptFactory = new ScriptEngineManager();

    /** Instance of the javaScriptHelper class */
    private JavaScriptHelper jsHelper = new JavaScriptHelper();

    /** Store Script State Name,Engine */
    private Map<String, ScriptEngineWrapper> scripts = new HashMap<String, ScriptEngineWrapper>();

    /** Used to store permanent variables */
    protected TypedProperties globalVariables = new TypedProperties();

    /**
     * Creates a new instance of the Script Plugin.
     */
    public ScriptPlugin() {
        super();

        // Add the JS Helper to the scriptFactory
        getScriptFactory().put("globalHelper", getJavaScriptHelper());
        getScriptFactory().put("globalVariables", getGlobalVariables());
    }

    /**
     * Called when the plugin is loaded.
     */
    @Override
    public void onLoad() {
        // Register the plugin_loaded action initially, this will be called
        // after this method finishes for us to register the rest.
        ActionManager.getActionManager().registerListener(this,
                CoreActionType.PLUGIN_LOADED);
        command = new ScriptCommand(this);
        CommandManager.getCommandManager().registerCommand(command);

        // Make sure our scripts dir exists
        final File newDir = new File(scriptDir);
        if (!newDir.exists()) { newDir.mkdirs(); }

        final File savedVariables = new File(scriptDir+"storedVariables");
        if (savedVariables.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(savedVariables);
                globalVariables.load(fis);
            } catch (IOException e) {
                Logger.userError(ErrorLevel.LOW, "Error reading savedVariables from '"+savedVariables.getPath()+"': "+e.getMessage(), e);
            } finally {
                StreamUtil.close(fis);
            }
        }
    }

    /**
     * Called when this plugin is Unloaded
     */
    @Override
    public void onUnload() {
        ActionManager.getActionManager().unregisterListener(this);
        CommandManager.getCommandManager().unregisterCommand(command);

        final File savedVariables = new File(scriptDir+"storedVariables");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(savedVariables);
            globalVariables.store(fos, "# DMDirc Script Plugin savedVariables");
        } catch (IOException e) {
            Logger.userError(ErrorLevel.LOW, "Error reading savedVariables to '"+savedVariables.getPath()+"': "+e.getMessage(), e);
        } finally {
            StreamUtil.close(fos);
        }
    }

    /**
     * Register all the action types.
     * This will unregister all the actions first.
     */
    private void registerAll() {
        ActionManager.getActionManager().registerListener(this);
        for (Map.Entry<String, List<ActionType>> entry : ActionManager
                .getActionManager().getGroupedTypes().entrySet()) {
            final List<ActionType> types = entry.getValue();
            ActionManager.getActionManager().registerListener(this,
                    types.toArray(new ActionType[0]));
        }
    }

    /**
     * Process an event of the specified type.
     *
     * @param type The type of the event to process
     * @param format Format of messages that are about to be sent. (May be null)
     * @param arguments The arguments for the event
     */
    @Override
    public void processEvent(final ActionType type, final StringBuffer format, final Object... arguments) {
        // Plugins may to register/unregister action types, so lets reregister all
        // the action types. This
        if (type.equals(CoreActionType.PLUGIN_LOADED) || type.equals(CoreActionType.PLUGIN_UNLOADED)) {
            registerAll();
        }
        callFunctionAll("action_"+type.toString().toLowerCase(), arguments);
    }

    /**
     * Get a clone of the scripts map.
     *
     * @return a clone of the scripts map
     */
    protected Map<String, ScriptEngineWrapper> getScripts() { return new HashMap<String, ScriptEngineWrapper>(scripts); }

    /**
     * Get a reference to the scriptFactory.
     *
     * @return a reference to the scriptFactory
     */
    protected ScriptEngineManager getScriptFactory() { return scriptFactory; }

    /**
     * Get a reference to the JavaScriptHelper
     *
     * @return a reference to the JavaScriptHelper
     */
    protected JavaScriptHelper getJavaScriptHelper() { return jsHelper; }

    /**
     * Get a reference to the GlobalVariables Properties
     *
     * @return a reference to the GlobalVariables Properties
     */
    protected TypedProperties getGlobalVariables() { return globalVariables; }

    /**
     * Get the name of the directory where scripts should be stored.
     *
     * @return The name of the directory where scripts should be stored.
     */
    protected String getScriptDir() { return scriptDir; }

    /** Reload all scripts */
    public void rehash() {
        for (final ScriptEngineWrapper engine : scripts.values()) {
            engine.reload();
        }
        // Advise the Garbage collector that now would be a good time to run
        System.gc();
    }

    /**
     * Call a function in all scripts.
     *
     * @param functionName Name of function
     * @param args Arguments for function
     */
    private void callFunctionAll(final String functionName, final Object... args) {
        for (final ScriptEngineWrapper engine : scripts.values()) {
            engine.callFunction(functionName, args);
        }
    }

    /**
     * Unload a script file.
     *
     * @param scriptFilename Path to script
     */
    public void unloadScript(final String scriptFilename) {
        if (scripts.containsKey(scriptFilename)) {
            // Tell it that its about to be unloaded.
            (scripts.get(scriptFilename)).callFunction("onUnload");
            // Remove the script
            scripts.remove(scriptFilename);
            // Advise the Garbage collector that now would be a good time to run
            System.gc();
        }
    }

    /**
     * Load a script file into a new jsEngine
     *
     * @param scriptFilename Path to script
     * @return true for Success (or already loaded), false for fail. (Fail occurs if script already exists, or if it has errors)
     */
    public boolean loadScript(final String scriptFilename) {
        if (!scripts.containsKey(scriptFilename)) {
            try {
                final ScriptEngineWrapper wrapper = new ScriptEngineWrapper(this, scriptFilename);
                scripts.put(scriptFilename, wrapper);
            } catch (Exception e) {
                Logger.userError(ErrorLevel.LOW, "Error loading '"+scriptFilename+"': "+e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * Check any further Prerequisites for this plugin to load that can not be
     * checked using metainfo.
     *
     * @return ValidationResponse detailign if the plugin passes any extra checks
     *         that plugin.info can't handle
     */
    public ValidationResponse checkPrerequisites() {
        if (getScriptFactory().getEngineByName("JavaScript") == null) {
            return new ValidationResponse("JavaScript Scripting Engine not found.");
        } else {
            return new ValidationResponse();
        }
    }

    /**
     * Get the reason for checkPrerequisites failing.
     *
     * @return Human-Readble reason for checkPrerequisites failing.
     */
    public String checkPrerequisitesReason() {
        if (getScriptFactory().getEngineByName("JavaScript") == null) {
            return "JavaScript Scripting Engine not found.";
        } else {
            return "";
        }
    }
}


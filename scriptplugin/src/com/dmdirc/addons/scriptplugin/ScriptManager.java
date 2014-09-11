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

import com.dmdirc.DMDircMBassador;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptManager {

    /** Script engine manager. */
    private final ScriptEngineManager scriptEngineManager;
    /** Script directory. */
    private final String scriptDirectory;
    /** Store Script State Name,Engine */
    private final Map<String, ScriptEngineWrapper> scripts = new HashMap<>();
    /** The event bus to post events to. */
    private final DMDircMBassador eventBus;

    @Inject
    public ScriptManager(final ScriptEngineManager scriptEngineManager,
            @Directory(ScriptModule.SCRIPTS) final String scriptDirectory,
            final DMDircMBassador eventBus) {
        this.scriptEngineManager = scriptEngineManager;
        this.scriptDirectory = scriptDirectory;
        this.eventBus = eventBus;
    }

    /**
     * Get a clone of the scripts map.
     *
     * @return a clone of the scripts map
     */
    protected Map<String, ScriptEngineWrapper> getScripts() {
        return new HashMap<>(scripts);
    }

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
     * @param args         Arguments for function
     */
    public void callFunctionAll(final String functionName, final Object... args) {
        for (final ScriptEngineWrapper engine : scripts.values()) {
            engine.callFunction(functionName, args);
        }
    }

    /**
     * Load a script file into a new jsEngine
     *
     * @param scriptFilename Path to script
     *
     * @return true for Success (or already loaded), false for fail. (Fail occurs if script already
     *         exists, or if it has errors)
     */
    public boolean loadScript(final String scriptFilename) {
        if (!scripts.containsKey(scriptFilename)) {
            try {
                final ScriptEngineWrapper wrapper = new ScriptEngineWrapper(scriptEngineManager,
                        eventBus, scriptFilename);
                scripts.put(scriptFilename, wrapper);
            } catch (FileNotFoundException | ScriptException e) {
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, e,
                        "Error loading '" + scriptFilename + "': " + e.getMessage(), ""));
                return false;
            }
        }
        return true;
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
     * Retrieves a list of all installed scripts. Any file under the main plugin directory
     * (~/.DMDirc/scripts or similar) that matches *.js is deemed to be a valid script.
     *
     * @return A list of all installed scripts
     */
    public List<String> getPossibleScripts() {
        final List<String> res = new LinkedList<>();

        final LinkedList<File> dirs = new LinkedList<>();
        dirs.add(new File(scriptDirectory));

        while (!dirs.isEmpty()) {
            final File dir = dirs.pop();
            if (dir.isDirectory()) {
                dirs.addAll(Arrays.asList(dir.listFiles()));
            } else if (dir.isFile() && dir.getName().endsWith(".js")) {
                final String target = dir.getPath();
                res.add(target.substring(scriptDirectory.length(), target.length()));
            }
        }
        return res;
    }

}

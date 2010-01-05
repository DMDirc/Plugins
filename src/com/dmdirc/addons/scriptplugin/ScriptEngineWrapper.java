/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

import com.dmdirc.logger.Logger;
import com.dmdirc.logger.ErrorLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.script.ScriptEngine;
import javax.script.Invocable;
import javax.script.ScriptException;

public class ScriptEngineWrapper {
    /** The Script Engine this wrapper wraps */
    private ScriptEngine engine;

    /** The File this script is from */
    private final File file;
    
    /** Script-Local JS Helper */
    private JavaScriptHelper localHelper = new JavaScriptHelper();

    /** The script plugin that owns this wrapper. */
    private final ScriptPlugin plugin;

    /**
     * Create a new ScriptEngineWrapper
     *
     * @param plugin The script plugin that owns this wrapper
     * @param filename Filename of script
     */
    protected ScriptEngineWrapper(final ScriptPlugin plugin, final String filename) throws FileNotFoundException, ScriptException {
        this.plugin = plugin;
        file = (filename != null) ? new File(filename) : null;
        
        engine = createEngine();
        
        callFunction("onLoad");
    }
    
    /**
     * Get a reference to the ScriptEngine.
     *
     * @return a reference to the ScriptEngine
     */
    protected ScriptEngine getScriptEngine() { return engine; }
    
    /**
     * Get a reference to the JavaScriptHelper
     *
     * @return a reference to the JavaScriptHelper
     */
    protected JavaScriptHelper getJavaScriptHelper() { return localHelper; }
    
    /**
     * Get the file for this script
     *
     * @return The file for this script
     */
    protected File getFile() { return file; }
    
    /**
     * Create a new engine for this script
     */
    protected ScriptEngine createEngine() throws FileNotFoundException, ScriptException {
        final ScriptEngine result = plugin.getScriptFactory().getEngineByName("JavaScript");
        if (file != null) {
            result.eval(new FileReader(file));
        }
        
        result.put("localHelper", localHelper);
        result.put("thisEngine", this);
        
        return result;
    }
    
    /**
     * Call a function in this script.
     *
     * @param functionName Name of function
     * @param args Arguments for function
     */
    protected void callFunction(final String functionName, final Object... args) {
        try {
            // Call Function
            final Invocable invEngine = (Invocable) engine;
            invEngine.invokeFunction(functionName, args);
        } catch (NoSuchMethodException nsme) {
            // There is no "methodExists" function, so we catch NoSuchMethodException
            // and do nothing rather that add an erropr every time a method is called
            // that doesn't exist (such as the action_* methods)
        } catch (Exception e) {
            Logger.userError(ErrorLevel.LOW, "Error calling '"+functionName+"' in '"+file.getPath()+"': "+e.getMessage(), e);
        }
    }
    
    /**
     * Try to reload this script.
     */
    protected boolean reload() {
        // Tell the current engine that its about to be obliterated.
        callFunction("onPreRehash");
        
        try {
            // Try making a new engine
            engine = createEngine();
            // Tell it that it has been rehashed
            callFunction("onRehashSucess");
        } catch (Exception e) {
            Logger.userError(ErrorLevel.LOW, "Reloading '"+file.getPath()+"' failed: "+e.getMessage(), e);
            // Tell it that its rehash failed
            callFunction("onRehashFailed", e);
            return false;
        }
        
        return true;
    }
}
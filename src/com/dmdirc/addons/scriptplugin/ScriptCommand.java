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

import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.commandparser.CommandManager;
import com.dmdirc.commandparser.commands.GlobalCommand;
import com.dmdirc.ui.interfaces.InputWindow;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.ui.input.AdditionalTabTargets;
import com.dmdirc.ui.input.TabCompletionType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.Method;

/**
 * The Script Command allows controlling of the scriptplugin.
 *
 * @author Shane 'Dataforce' McCormack
 */
public final class ScriptCommand extends GlobalCommand implements IntelligentCommand {
    /** My Plugin */
    final ScriptPlugin myPlugin;

    /**
     * Creates a new instance of ScriptCommand.
     */
    public ScriptCommand(final ScriptPlugin plugin) {
        super();
        myPlugin = plugin;
        CommandManager.registerCommand(this);
    }
        
    /**
     * Executes this command.
     *
     * @param origin The frame in which this command was issued
     * @param server The server object that this command is associated with
     * @param isSilent Whether this command is silenced or not
     * @param args The user supplied arguments
     */
    @Override
    public void execute(final InputWindow origin, final boolean isSilent, final CommandArguments commandArgs) {
        final String[] args = commandArgs.getArguments();
    
        if (args.length > 0 && (args[0].equalsIgnoreCase("rehash") || args[0].equalsIgnoreCase("reload"))) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Reloading scripts");
            myPlugin.rehash();
        } else if (args.length > 0 && args[0].equalsIgnoreCase("load")) {
            if (args.length > 1) {
                final String filename = commandArgs.getArgumentsAsString(1);
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Loading: "+filename+" ["+myPlugin.loadScript(myPlugin.getScriptDir()+filename)+"]");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "You must specify a script to load");
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("unload")) {
            if (args.length > 1) {
                final String filename = commandArgs.getArgumentsAsString(1);
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Unloading: "+filename+" ["+myPlugin.loadScript(myPlugin.getScriptDir()+filename)+"]");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "You must specify a script to unload");
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("eval")) {
            if (args.length > 1) {
                final String script = commandArgs.getArgumentsAsString(1);
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Evaluating: "+script);
                try {
                    ScriptEngineWrapper wrapper;
                    if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "eval.baseFile")) {
                        final String baseFile = myPlugin.getScriptDir()+'/'+IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "eval.baseFile");
                        if ((new File(baseFile)).exists()) {
                            wrapper = new ScriptEngineWrapper(myPlugin, baseFile);
                        } else {
                            wrapper = new ScriptEngineWrapper(myPlugin, null);
                        }
                    } else {
                        wrapper = new ScriptEngineWrapper(myPlugin, null);
                    }
                    wrapper.getScriptEngine().put("cmd_origin", origin);
                    wrapper.getScriptEngine().put("cmd_isSilent", isSilent);
                    wrapper.getScriptEngine().put("cmd_args", args);
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Result: "+wrapper.getScriptEngine().eval(script));
                } catch (Exception e) {
                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Exception: "+e+" -> "+e.getMessage());
                    
                    if (IdentityManager.getGlobalConfig().getOptionBool(myPlugin.getDomain(), "eval.showStackTrace")) {
                        try {
                            final Class<?> logger = Class.forName("com.dmdirc.logger.Logger");
                            if (logger != null) {
                                final Method exceptionToStringArray = logger.getDeclaredMethod("exceptionToStringArray", new Class[]{Throwable.class});
                                exceptionToStringArray.setAccessible(true);
                                
                                final String[] stacktrace = (String[])exceptionToStringArray.invoke(null, e);
                                for (String line : stacktrace) {
                                    sendLine(origin, isSilent, FORMAT_OUTPUT, "Stack trace: "+line);
                                }
                            }
                        } catch (Exception ex) {
                            sendLine(origin, isSilent, FORMAT_OUTPUT, "Stack trace: Exception showing stack trace: "+ex+" -> "+ex.getMessage());
                        }
                    }
                    
                }
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "You must specify some script to eval.");
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("savetobasefile")) {
            if (args.length > 2) {
                final String[] bits = args[1].split("/");
                final String functionName = bits[0];
                final String script = commandArgs.getArgumentsAsString(2);
                sendLine(origin, isSilent, FORMAT_OUTPUT, "Saving as '"+functionName+"': "+script);
                if (IdentityManager.getGlobalConfig().hasOptionString(myPlugin.getDomain(), "eval.baseFile")) {
                    try {
                        final String baseFile = myPlugin.getScriptDir()+'/'+IdentityManager.getGlobalConfig().getOption(myPlugin.getDomain(), "eval.baseFile");
                        final FileWriter writer = new FileWriter(baseFile, true);
                        writer.write("function ");
                        writer.write(functionName);
                        writer.write("(");
                        for (int i = 1; i < bits.length; i++) {
                            writer.write(bits[i]);
                            writer.write(" ");
                        }
                        writer.write(") {\n");
                        writer.write(script);
                        writer.write("\n}\n");
                        writer.flush();
                        writer.close();
                    } catch (IOException ioe) {
                        sendLine(origin, isSilent, FORMAT_ERROR, "IOException: "+ioe.getMessage());
                    }
                } else {
                    sendLine(origin, isSilent, FORMAT_ERROR, "No baseFile specified, please /set "+myPlugin.getDomain()+" eval.baseFile filename (stored in scripts dir of profile)");
                }
            } else if (args.length > 1) {
                sendLine(origin, isSilent, FORMAT_ERROR, "You must specify some script to save.");
            } else {
                sendLine(origin, isSilent, FORMAT_ERROR, "You must specify a function name and some script to save.");
            }
        } else if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, "This command allows you to interact with the script plugin");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "-------------------");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "reload/rehash                  - Reload all loaded scripts");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "load <script>                  - load scripts/<script> (file name relative to scripts dir)");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "unload <script>                - unload <script> (full file name)");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "eval <script>                  - evaluate the code <script> and return the result");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "savetobasefile <name> <script> - save the code <script> to the eval basefile ("+myPlugin.getDomain()+".eval.basefile)");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "                                 as the function <name> (name/foo/bar will save it as 'name' with foo and");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "                                 bar as arguments.");
            sendLine(origin, isSilent, FORMAT_OUTPUT, "-------------------");
        } else {
            sendLine(origin, isSilent, FORMAT_ERROR, "Unknown subcommand.");
        }
    }

    /**
     * Returns a list of suggestions for the specified argument, given the list
     * of previous arguments.
     * @param arg The argument that is being completed
     * @param previousArgs The contents of the previous arguments, if any
     * @return A list of suggestions for the argument
     */
    @Override
    public AdditionalTabTargets getSuggestions(final int arg, final List<String> previousArgs) {
        final AdditionalTabTargets res = new AdditionalTabTargets();
        
        res.excludeAll();
        if (arg == 0) {
            res.add("help");
            res.add("rehash");
            res.add("reload");
            res.add("load");
            res.add("unload");
            res.add("eval");
            res.add("savetobasefile");
        } else if (arg == 1) {
            final Map<String,ScriptEngineWrapper> scripts = myPlugin.getScripts();
            if (previousArgs.get(0).equalsIgnoreCase("load")) {
                for (String filename : getPossibleScripts()) {
                    res.add(filename);
                }
            } else if (previousArgs.get(0).equalsIgnoreCase("unload")) {
                for (String filename : scripts.keySet()) {
                    res.add(filename);
                }
            }
        }
        
        return res;
    }
    
    /**
     * Retrieves a list of all installed scripts.
     * Any file under the main plugin directory (~/.DMDirc/scripts or similar)
     * that matches *.js is deemed to be a valid script.
     *
     * @return A list of all installed scripts
     */
    private List<String> getPossibleScripts() {
        final List<String> res = new LinkedList<String>();
        
        final LinkedList<File> dirs = new LinkedList<File>();
        dirs.add(new File(myPlugin.getScriptDir()));
        
        while (!dirs.isEmpty()) {
            final File dir = dirs.pop();
            if (dir.isDirectory()) {
                for (File file : dir.listFiles()) {
                    dirs.add(file);
                }
            } else if (dir.isFile() && dir.getName().endsWith(".js")) {
                final String target = dir.getPath();
                res.add(target.substring(myPlugin.getScriptDir().length(), target.length()));
            }
        }
        return res;
    }

    /**
     * Returns this command's name.
     *
     * @return The name of this command
     */
    @Override
    public String getName() { return "script"; }
    
    /**
     * Returns whether or not this command should be shown in help messages.
     *
     * @return True iff the command should be shown, false otherwise
     */
    @Override
    public boolean showInHelp() { return true; }
    
    /**
     * Returns a string representing the help message for this command.
     *
     * @return the help message for this command
     */
    @Override
    public String getHelp() { return "script - Allows controlling the script plugin"; }
}


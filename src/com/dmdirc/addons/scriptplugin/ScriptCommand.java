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

import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.FrameContainer;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.IntelligentCommand;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.ui.input.AdditionalTabTargets;

import net.engio.mbassy.bus.MBassador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * The Script Command allows controlling of the script plugin.
 */
public class ScriptCommand extends Command implements IntelligentCommand {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("script",
            "script - Allows controlling the script plugin", CommandType.TYPE_GLOBAL);
    /** Global config to read settings from. */
    private final AggregateConfigProvider globalConfig;
    /** Plugin settings domain. */
    private final String domain;
    /** Manager used to retrieve script engines. */
    private final ScriptEngineManager scriptEngineManager;
    /** Script directory. */
    private final String scriptDirectory;
    /** Script manager to handle scripts. */
    private final ScriptManager scriptManager;
    /** The event bus to post errors to. */
    private final MBassador eventBus;

    /**
     * Creates a new instance of this command.
     *
     * @param scriptManager       Used to manage scripts
     * @param eventBus            The event bus to post errors to
     * @param globalConfig        Global config
     * @param commandController   The controller to use for command information.
     * @param domain              This plugin's settings domain
     * @param scriptEngineManager Manager used to get script engines
     * @param scriptDirectory     Directory to store scripts
     */
    @Inject
    public ScriptCommand(final ScriptManager scriptManager,
            final MBassador eventBus,
            @Directory(ScriptModule.SCRIPTS) final String scriptDirectory,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final CommandController commandController,
            @PluginDomain(ScriptPlugin.class) final String domain,
            final ScriptEngineManager scriptEngineManager) {
        super(commandController);
        this.globalConfig = globalConfig;
        this.domain = domain;
        this.scriptEngineManager = scriptEngineManager;
        this.scriptDirectory = scriptDirectory;
        this.scriptManager = scriptManager;
        this.eventBus = eventBus;
    }

    @Override
    public void execute(@Nonnull final FrameContainer origin, final CommandArguments args,
            final CommandContext context) {
        final String[] sargs = args.getArguments();

        if (sargs.length > 0 && (sargs[0].equalsIgnoreCase("rehash") || sargs[0].equalsIgnoreCase(
                "reload"))) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Reloading scripts");
            scriptManager.rehash();
        } else if (sargs.length > 0 && sargs[0].equalsIgnoreCase("load")) {
            if (sargs.length > 1) {
                final String filename = args.getArgumentsAsString(1);
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Loading: " + filename + " ["
                        + scriptManager.loadScript(scriptDirectory + filename) + "]");
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR, "You must specify a script to load");
            }
        } else if (sargs.length > 0 && sargs[0].equalsIgnoreCase("unload")) {
            if (sargs.length > 1) {
                final String filename = args.getArgumentsAsString(1);
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Unloading: " + filename + " ["
                        + scriptManager.loadScript(scriptDirectory + filename) + "]");
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "You must specify a script to unload");
            }
        } else if (sargs.length > 0 && sargs[0].equalsIgnoreCase("eval")) {
            if (sargs.length > 1) {
                final String script = args.getArgumentsAsString(1);
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Evaluating: " + script);
                try {
                    ScriptEngineWrapper wrapper;
                    if (globalConfig.hasOptionString(domain, "eval.baseFile")) {
                        final String baseFile = scriptDirectory + '/'
                                + globalConfig.getOption(domain, "eval.baseFile");
                        if (new File(baseFile).exists()) {
                            wrapper = new ScriptEngineWrapper(scriptEngineManager, eventBus,
                                    baseFile);
                        } else {
                            wrapper = new ScriptEngineWrapper(scriptEngineManager, eventBus, null);
                        }
                    } else {
                        wrapper = new ScriptEngineWrapper(scriptEngineManager, eventBus, null);
                    }
                    wrapper.getScriptEngine().put("cmd_origin", origin);
                    wrapper.getScriptEngine().put("cmd_isSilent", args.isSilent());
                    wrapper.getScriptEngine().put("cmd_args", sargs);
                    sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Result: " + wrapper.
                            getScriptEngine().eval(script));
                } catch (FileNotFoundException | ScriptException e) {
                    sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Exception: " + e + " -> " + e.
                            getMessage());

                    if (globalConfig.getOptionBool(domain, "eval.showStackTrace")) {
                        final String[] stacktrace = getTrace(e);
                        for (String line : stacktrace) {
                            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Stack trace: " + line);
                        }
                    }

                }
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "You must specify some script to eval.");
            }
        } else if (sargs.length > 0 && sargs[0].equalsIgnoreCase("savetobasefile")) {
            if (sargs.length > 2) {
                final String[] bits = sargs[1].split("/");
                final String functionName = bits[0];
                final String script = args.getArgumentsAsString(2);
                sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Saving as '" + functionName
                        + "': " + script);
                if (globalConfig.hasOptionString(domain, "eval.baseFile")) {
                    try {
                        final String baseFile = scriptDirectory + '/'
                                + globalConfig.getOption(domain, "eval.baseFile");
                        try (FileWriter writer = new FileWriter(baseFile, true)) {
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
                        }
                    } catch (IOException ioe) {
                        sendLine(origin, args.isSilent(), FORMAT_ERROR, "IOException: " + ioe.
                                getMessage());
                    }
                } else {
                    sendLine(origin, args.isSilent(), FORMAT_ERROR,
                            "No baseFile specified, please /set " + domain
                            + " eval.baseFile filename (stored in scripts dir of profile)");
                }
            } else if (sargs.length > 1) {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "You must specify some script to save.");
            } else {
                sendLine(origin, args.isSilent(), FORMAT_ERROR,
                        "You must specify a function name and some script to save.");
            }
        } else if (sargs.length > 0 && sargs[0].equalsIgnoreCase("help")) {
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "This command allows you to interact with the script plugin");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "-------------------");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "reload/rehash                  - Reload all loaded scripts");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "load <script>                  - load scripts/<script> (file name relative to scripts dir)");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "unload <script>                - unload <script> (full file name)");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "eval <script>                  - evaluate the code <script> and return the result");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "savetobasefile <name> <script> - save the code <script> to the eval basefile ("
                    + domain + ".eval.basefile)");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "                                 as the function <name> (name/foo/bar will save it as 'name' with foo and");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT,
                    "                                 bar as arguments.");
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "-------------------");
        } else {
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unknown subcommand.");
        }
    }

    @Override
    public AdditionalTabTargets getSuggestions(final int arg,
            final IntelligentCommandContext context) {
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
            final Map<String, ScriptEngineWrapper> scripts = scriptManager.getScripts();
            if (context.getPreviousArgs().get(0).equalsIgnoreCase("load")) {
                for (String filename : scriptManager.getPossibleScripts()) {
                    res.add(filename);
                }
            } else if (context.getPreviousArgs().get(0).equalsIgnoreCase("unload")) {
                for (String filename : scripts.keySet()) {
                    res.add(filename);
                }
            }
        }

        return res;
    }

    /**
     * Converts an exception into a string array.
     *
     * @param throwable Exception to convert
     *
     * @return Exception string array
     */
    private static String[] getTrace(final Throwable throwable) {
        String[] trace;

        if (throwable == null) {
            trace = new String[0];
        } else {
            final StackTraceElement[] traceElements = throwable.getStackTrace();
            trace = new String[traceElements.length + 1];

            trace[0] = throwable.toString();

            for (int i = 0; i < traceElements.length; i++) {
                trace[i + 1] = traceElements[i].toString();
            }

            if (throwable.getCause() != null) {
                final String[] causeTrace = getTrace(throwable.getCause());
                final String[] newTrace = new String[trace.length + causeTrace.length];
                trace[0] = "\nWhich caused: " + trace[0];

                System.arraycopy(causeTrace, 0, newTrace, 0, causeTrace.length);
                System.arraycopy(trace, 0, newTrace, causeTrace.length, trace.length);

                trace = newTrace;
            }
        }

        return trace;
    }

}

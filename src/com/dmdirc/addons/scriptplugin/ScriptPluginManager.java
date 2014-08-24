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
import com.dmdirc.events.DMDircEvent;
import com.dmdirc.events.PluginLoadedEvent;
import com.dmdirc.events.PluginUnloadedEvent;
import com.dmdirc.events.UserErrorEvent;
import com.dmdirc.logger.ErrorLevel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.script.ScriptEngineManager;

import net.engio.mbassy.listener.Handler;

public class ScriptPluginManager {

    private final DMDircMBassador eventBus;
    private final String scriptDir;
    private final ScriptManager scriptManager;
    private final TypedProperties globalVariables;

    @Inject
    public ScriptPluginManager(final DMDircMBassador eventBus,
            @Directory(ScriptModule.SCRIPTS) final String scriptDir,
            final ScriptManager scriptManager,
            final ScriptEngineManager scriptEngineManager) {
        this.scriptDir = scriptDir;
        this.scriptManager = scriptManager;
        this.eventBus = eventBus;
        globalVariables = (TypedProperties) scriptEngineManager.get("globalVariables");
    }

    public void onLoad() {
        // Register the plugin_loaded action initially, this will be called
        // after this method finishes for us to register the rest.
        eventBus.subscribe(this);

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
                eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, e,
                        "Error reading savedVariables from '" + savedVariables.getPath() + "': "
                                + e.getMessage(), ""));
            }
        }
    }

    public void onUnLoad() {
        eventBus.unsubscribe(this);

        final File savedVariables = new File(scriptDir + "storedVariables");
        try (FileOutputStream fos = new FileOutputStream(savedVariables)) {
            globalVariables.store(fos, "# DMDirc Script Plugin savedVariables");
        } catch (IOException e) {
            eventBus.publishAsync(new UserErrorEvent(ErrorLevel.LOW, e,"Error reading savedVariables to '"
                    + savedVariables.getPath() + "': " + e.getMessage(), ""));
        }
    }

    @Handler
    public void handlePluginLoadEvent(final DMDircEvent event) throws ReflectiveOperationException {
        if (event instanceof PluginLoadedEvent || event instanceof PluginUnloadedEvent) {
            return;
        }
        final String name = event.getClass().getSimpleName()
                .replaceAll("Event$", "")
                .replaceAll("(.)([A-Z])", "$1_$2")
                .toUpperCase();
        final List<Object> arguments = new ArrayList<>();
        for (Method method : event.getClass().getMethods()) {
            if ((method.getName().startsWith("get") && method.getParameterTypes().length == 0)
                    && !method.getName().equals("getDisplayFormat")) {
                arguments.add(method.invoke(event));
            }
        }
        scriptManager.callFunctionAll("action_" + name, arguments.toArray());
    }
}

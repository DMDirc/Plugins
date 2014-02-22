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

import com.dmdirc.ClientModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;

import javax.inject.Singleton;
import javax.script.ScriptEngineManager;

import dagger.Module;
import dagger.Provides;

@Module(injects = {ScriptCommand.class, ScriptManager.class, ScriptPluginManager.class},
        addsTo = ClientModule.class)
public class ScriptModule {

    public static final String SCRIPTS = "scripts";
    private final PluginInfo pluginInfo;

    public ScriptModule(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Provides
    @PluginDomain(ScriptPlugin.class)
    public String getScriptSettingsDomain() {
        return pluginInfo.getDomain();
    }

    @Provides
    @Directory(SCRIPTS)
    public String getScriptsDirectory(@Directory(DirectoryType.BASE) final String baseDirectory) {
        return baseDirectory + "scripts/";
    }

    @Provides
    @Singleton
    public ScriptEngineManager getScriptEngineManager() {
        final JavaScriptHelper jsHelper = new JavaScriptHelper();
        final TypedProperties globalVariables = new TypedProperties();
        final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngineManager.put("globalHelper", jsHelper);
        scriptEngineManager.put("globalVariables", globalVariables);
        return scriptEngineManager;
    }

}

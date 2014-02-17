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

package com.dmdirc.addons.ui_web;

import com.dmdirc.ClientModule;
import com.dmdirc.plugins.PluginInfo;

import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.IOException;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(injects={WebInterfaceUI.class, StaticRequestHandler.class}, addsTo=ClientModule.class)
public class WebInterfaceModule {

    private final PluginInfo pluginInfo;

    public WebInterfaceModule(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Qualifier
    public @interface WebUIDomain {};

    @Provides
    @WebUIDomain
    public String getSettingsDomain() {
        return pluginInfo.getDomain();
    }

    @Singleton
    @Provides
    public ResourceManager getResourceManager() {
        try {
            return pluginInfo.getResourceManager();
        } catch (IOException ex) {
            throw new IllegalStateException("Die Horrible", ex);
        }
    }

}

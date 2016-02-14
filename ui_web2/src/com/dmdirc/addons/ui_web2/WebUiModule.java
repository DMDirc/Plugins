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

package com.dmdirc.addons.ui_web2;

import com.dmdirc.ClientModule;
import com.dmdirc.ClientModule.GlobalConfig;
import com.dmdirc.interfaces.config.AggregateConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides Web UI-specific dependencies.
 */
@Module(
        addsTo = ClientModule.class,
        injects = WebServer.class,
        library = true
)
@SuppressWarnings("TypeMayBeWeakened")
public class WebUiModule {

    private final PluginInfo pluginInfo;
    private final String domain;

    public WebUiModule(final PluginInfo pluginInfo, final String domain) {
        this.pluginInfo = pluginInfo;
        this.domain = domain;
    }

    @Provides
    @PluginDomain(WebUiPlugin.class)
    public String getSettingsDomain() {
        return domain;
    }

    @Provides
    @PluginDomain(WebUiPlugin.class)
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    @Provides
    @Singleton
    public WebServer getWebServer(
            @PluginDomain(WebUiPlugin.class) final String domain,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            final WebSocketController controller) {
        WebSocketHandler.setController(controller);
        final int port = globalConfig.getOptionInt(domain, "port");
        return new WebServer(port);
    }

}

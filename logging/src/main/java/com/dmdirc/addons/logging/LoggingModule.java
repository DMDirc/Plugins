/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.logging;

import com.dmdirc.ClientModule;
import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.config.GlobalConfig;
import com.dmdirc.config.UserConfig;
import com.dmdirc.config.provider.AggregateConfigProvider;
import com.dmdirc.config.provider.ConfigProvider;
import com.dmdirc.plugins.PluginDomain;
import com.dmdirc.plugins.PluginInfo;
import dagger.Module;
import dagger.Provides;
import java.io.File;

/**
 * Dependency injection module for the logging plugin.
 */
@Module(addsTo = ClientModule.class, injects = {LoggingManager.class, LoggingCommand.class})
public class LoggingModule {

    public static final String LOGS_DIRECTORY = "logs";

    private final PluginInfo pluginInfo;

    public LoggingModule(final PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    @Provides
    @PluginDomain(LoggingPlugin.class)
    public String getDomain() {
        return pluginInfo.getDomain();
    }

    @Provides
    @Directory(LOGS_DIRECTORY)
    @SuppressWarnings("TypeMayBeWeakened")
    public String getLogsDirectory(
            @UserConfig final ConfigProvider userConfig,
            @GlobalConfig final AggregateConfigProvider globalConfig,
            @Directory(DirectoryType.BASE) final String baseDirectory,
            @PluginDomain(LoggingPlugin.class) final String domain) {
        if (!userConfig.hasOptionString(domain, "general.directory")) {
            userConfig.setOption(domain, "general.directory",
                    baseDirectory + "logs" + File.separator);
        }

        return globalConfig.getOptionString(domain, "general.directory");
    }

    @Provides
    @PluginDomain(LoggingPlugin.class)
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

}

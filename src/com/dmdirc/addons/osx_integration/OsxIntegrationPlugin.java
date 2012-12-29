package com.dmdirc.addons.osx_integration;

import com.dmdirc.addons.ui_swing.SwingController;
import com.dmdirc.config.IdentityManager;
import com.dmdirc.plugins.BasePlugin;
import com.dmdirc.plugins.PluginInfo;

public class OsxIntegrationPlugin extends BasePlugin
{
    public OsxIntegrationPlugin(final SwingController controller,
                                final PluginInfo pluginInfo,
                                final IdentityManager identityManager
    ) {
        registerCommand(new DockBounceCommand(controller), DockBounceCommand.INFO);
    }
}

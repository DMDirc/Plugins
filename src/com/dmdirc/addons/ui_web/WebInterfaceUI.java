/*
 * Copyright (c) 2006-2013 DMDirc Developers
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

import com.dmdirc.Channel;
import com.dmdirc.FrameContainer;
import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_web.uicomponents.WebStatusBar;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.interfaces.ui.Window;
import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.PluginManager;
import com.dmdirc.ui.WindowManager;
import com.dmdirc.ui.core.components.StatusBarManager;

import java.net.URI;

import lombok.Getter;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.SecurityHandler;

/**
 * Creates and manages the web server and handles UI-wide events.
 */
@SuppressWarnings("PMD.UnusedPrivateField")
public class WebInterfaceUI implements UIController {

    /** The web server we're using. */
    private final org.mortbay.jetty.Server webServer;

    /** The window manager for this UI. */
    @Getter
    private final WebWindowManager windowManager;

    /** The dynamic request handler in use. */
    @Getter
    private final DynamicRequestHandler handler;

    /** The PluginInfo object for this plugin. */
    @Getter
    private final PluginInfo pluginInfo;

    /** The plugin manager used to find other plugins. */
    @Getter
    private final PluginManager pluginManager;

    /**
     * Creates a new WebInterfaceUI belonging to the specified plugin.
     *
     * @param domain The domain to retrieve config settings from
     * @param identityController The controller to read/write settings with.
     * @param serverManager The manager to use to find and create servers
     * @param pluginManager The manager to use to find other plugins
     * @param pluginInfo The information object for this UI's plugin.
     * @param coreWindowManager Window management
     */
    public WebInterfaceUI(
            final String domain,
            final IdentityController identityController,
            final ServerManager serverManager,
            final PluginManager pluginManager,
            final PluginInfo pluginInfo,
            final WindowManager coreWindowManager) {
        super();

        this.pluginManager = pluginManager;
        this.pluginInfo = pluginInfo;

        final SecurityHandler sh = new SecurityHandler();
        final Constraint constraint = new Constraint();
        final ConstraintMapping cm = new ConstraintMapping();
        handler = new DynamicRequestHandler(this, identityController, serverManager);

        constraint.setName("DMDirc Web UI");
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        sh.setUserRealm(new WebUserRealm(identityController, domain));
        sh.setConstraintMappings(new ConstraintMapping[]{cm});

        webServer = new org.mortbay.jetty.Server(5978);

        webServer.setHandlers(new Handler[]{
            sh,
            new RootRequestHandler(),
            new StaticRequestHandler(this),
            new DMDircRequestHandler(),
            handler,
        });

        try {
            webServer.start();
        } catch (Exception ex) {
            // Break horribly!
        }

        windowManager = new WebWindowManager(this, coreWindowManager);

        StatusBarManager.getStatusBarManager().registerStatusBar(new WebStatusBar(handler));
    }

    /**
     * Adds the specified handler to the webserver.
     *
     * @param newHandler The handler to add.
     */
    public void addWebHandler(final Handler newHandler) {
        webServer.addHandler(newHandler);
    }

    /** {@inheritDoc} */
    @Override
    public void showFirstRunWizard() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void showChannelSettingsDialog(final Channel channel) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void showServerSettingsDialog(final Server server) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void showURLDialog(final URI url) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void showFeedbackNag() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void showMessageDialog(final String title, final String message) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void requestWindowFocus(final Window window) {
        // TODO: Tell clients to focus
    }

    /** {@inheritDoc} */
    @Override
    public void requestWindowFocus(final FrameContainer container) {
        // TODO: Tell clients to focus
    }

}

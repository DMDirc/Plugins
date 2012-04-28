/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
import com.dmdirc.addons.ui_web.uicomponents.WebStatusBar;
import com.dmdirc.interfaces.ui.UIController;
import com.dmdirc.interfaces.ui.Window;
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
public class WebInterfaceUI implements UIController {

    /** The web server we're using. */
    private final org.mortbay.jetty.Server webServer;

    /** The window manager for this UI. */
    @Getter
    private final WebWindowManager windowManager;

    /** The dynamic request handler in use. */
    @Getter
    private final DynamicRequestHandler handler;

    /**
     * Creates a new WebInterfaceUI belonging to the specified plugin.
     *
     * @param domain The domain to retrieve config settings from
     */
    public WebInterfaceUI(final String domain) {
        super();

        final SecurityHandler sh = new SecurityHandler();
        final Constraint constraint = new Constraint();
        final ConstraintMapping cm = new ConstraintMapping();
        handler = new DynamicRequestHandler(this);

        constraint.setName("DMDirc Web UI");
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        cm.setConstraint(constraint);
        cm.setPathSpec("/*");

        sh.setUserRealm(new WebUserRealm(domain));
        sh.setConstraintMappings(new ConstraintMapping[]{cm});

        webServer = new org.mortbay.jetty.Server(5978);

        webServer.setHandlers(new Handler[]{
            sh,
            new RootRequestHandler(),
            new StaticRequestHandler(),
            new DMDircRequestHandler(),
            handler,
        });

        try {
            webServer.start();
        } catch (Exception ex) {
            // Break horribly!
        }

        windowManager = new WebWindowManager(this);

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

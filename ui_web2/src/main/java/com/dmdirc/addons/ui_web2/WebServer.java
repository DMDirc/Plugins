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

import com.dmdirc.util.LogUtils;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web server used by the web UI.
 */
public class WebServer {

    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);

    private final int port;
    private Server server;

    public WebServer(final int port) {
        this.port = port;
    }

    public void start() {
        server = new Server(port);

        // Override the context classloader, so that Jetty uses the plugin classloader not the main DMDirc loader.
        final Thread currentThread = Thread.currentThread();
        final ClassLoader classLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(getClass().getClassLoader());

        try {
            final ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
            resourceHandler.setBaseResource(Resource.newClassPathResource("/www"));

            final ServletContextHandler wsHandler = new ServletContextHandler();
            wsHandler.setContextPath("/");
            wsHandler.addServlet(WebUiWebSocketServlet.class, "/ws");

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { resourceHandler, wsHandler, new DefaultHandler() });
            server.setHandler(handlers);

            server.start();
        } catch (Exception ex) {
            LOG.error(LogUtils.USER_ERROR, "Unable to start web server", ex);
            server = null;
        } finally {
            // Restore the usual context class loader.
            currentThread.setContextClassLoader(classLoader);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception ex) {
            LOG.error(LogUtils.USER_ERROR, "Unable to stop web server", ex);
        }

        server = null;
    }

    /**
     * Web Socket Servlet that creates a {@link WebSocketHandler} for each connection.
     */
    public static class WebUiWebSocketServlet extends WebSocketServlet {

        @Override
        public void configure(final WebSocketServletFactory factory) {
            factory.register(WebSocketHandler.class);
        }

    }
}

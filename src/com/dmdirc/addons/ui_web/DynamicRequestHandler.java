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
import com.dmdirc.ServerManager;
import com.dmdirc.addons.ui_web.uicomponents.WebInputHandler;
import com.dmdirc.addons.ui_web.uicomponents.WebInputWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.parser.common.ChannelJoinRequest;
import com.dmdirc.parser.interfaces.ChannelClientInfo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;
import org.mortbay.util.ajax.JSON;
import org.mortbay.util.ajax.JSONObjectConvertor;

/**
 * Handles requests for dynamic resources (prefixed with /dynamic/).
 */
@Slf4j
public class DynamicRequestHandler extends AbstractHandler {

    /** Number of milliseconds before a client is timed out. */
    private static final long TIMEOUT = 1000 * 60 * 2; // Two minutes

    /** The last time each client was seen. */
    private final Map<String, Client> clients = new HashMap<>();

    /** The controller which owns this request handler. */
    private final WebInterfaceUI controller;

    /** The server manager to use to connect to new servers. */
    private final ServerManager serverManager;

    /** The controller to read/write settings with. */
    private final IdentityController identityController;

    /**
     * Creates a new instance of DynamicRequestHandler. Registers object
     * convertors with the JSON serialiser.
     *
     * @param controller The controller that this request handler is for.
     * @param identityController The controller to read/write settings with.
     * @param serverManager The server manager to use to connect to new servers.
     */
    public DynamicRequestHandler(
            final WebInterfaceUI controller,
            final IdentityController identityController,
            final ServerManager serverManager) {
        super();

        this.controller = controller;
        this.identityController = identityController;
        this.serverManager = serverManager;

        JSON.registerConvertor(Event.class, new JSONObjectConvertor());
        JSON.registerConvertor(WebWindow.class, new JSONObjectConvertor());
        JSON.registerConvertor(Message.class, new JSONObjectConvertor());
        JSON.registerConvertor(Client.class, new JSONObjectConvertor());

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (clients) {
                    for (Map.Entry<String, Client> entry
                            : new HashMap<>(clients).entrySet()) {
                        if (entry.getValue().getTime() > TIMEOUT) {
                            clients.remove(entry.getKey());
                        }
                    }
                }
            }
        }, TIMEOUT, TIMEOUT / 2);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException If unable to write response
     */
    @Override
    public void handle(final String target, final HttpServletRequest request,
            final HttpServletResponse response, final int dispatch)
            throws IOException {

        if (request.getParameter("clientID") != null) {
            final String clientID = request.getParameter("clientID");

            if (!clients.containsKey(clientID)) {
                clients.put(clientID, new Client(controller,
                        request.getRemoteHost()));
            }

            synchronized (clients) {
                clients.get(clientID).touch();
            }
        }

        if (((request instanceof Request) ? (Request) request : HttpConnection
                .getCurrentConnection().getRequest()).isHandled()) {
            return;
        }
        switch (target) {
            case "/dynamic/feed":
                doFeed(request, response);
                handled(request);
                break;
            case "/dynamic/getprofiles":
                doProfiles(response);
                handled(request);
                break;
            case "/dynamic/newserver":
                doNewServer(request);
                handled(request);
                break;
            case "/dynamic/windowrefresh":
                doWindowRefresh(request, response);
                handled(request);
                break;
            case "/dynamic/input":
                doInput(request);
                handled(request);
                break;
            case "/dynamic/nicklistrefresh":
                doNicklist(request, response);
                handled(request);
                break;
            case "/dynamic/tab":
                doTab(request);
                handled(request);
                break;
            case "/dynamic/keyup":
            case "/dynamic/keydown":
                doKeyUpDown(target.equals("/dynamic/keyup"), request);
                handled(request);
                break;
            case "/dynamic/key":
                doKey(request);
                handled(request);
                break;
            case "/dynamic/clients":
                doClients(response);
                handled(request);
                break;
            case "/dynamic/joinchannel":
                doJoinChannel(request);
                handled(request);
                break;
            case "/dynamic/openquery":
                doOpenQuery(request);
                handled(request);
                break;
        }
    }

    /**
     * Handles a request for the event feed.
     *
     * @param request The servlet request that is being handled
     * @param response The servlet response object to write to
     * @throws IOException If unable to write the response
     */
    private void doFeed(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        final Client client = clients.get(request.getParameter("clientID"));

        synchronized (client.getMutex()) {
            List<Event> myEvents = client.retrieveEvents();

            if (myEvents.isEmpty()) {
                final Continuation continuation = ContinuationSupport
                        .getContinuation(request, client.getMutex());
                client.setContinuation(continuation);
                continuation.suspend(30000L);

                myEvents = client.retrieveEvents();
            }

            client.setContinuation(null);

            final String json = toJSON(myEvents.toArray());
            response.getWriter().write(json);
        }
    }

    private void doInput(final HttpServletRequest request) throws IOException {
        final WebWindow window = controller.getWindowManager().getWindow(
                request.getParameter("window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;
            final Client client = clients.get(request.getParameter("clientID"));

            wiw.getInputHandler(client).enterPressed(request.getParameter("input"));
        }
    }

    private void doKey(final HttpServletRequest request) throws IOException {
        final WebWindow window = controller.getWindowManager().getWindow(
                request.getParameter("window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;
            final Client client = clients.get(request.getParameter("clientID"));

            try {
                wiw.getInputHandler(
                        client,
                        request.getParameter("input"),
                        request.getParameter("selstart"),
                        request.getParameter("selend")).handleKeyPressed(
                        request.getParameter("input"),
                        Integer.parseInt(request.getParameter("key")), 0,
                        Boolean.parseBoolean(request.getParameter("shift")),
                        Boolean.parseBoolean(request.getParameter("ctrl")));
            } catch (NumberFormatException ex) {
                // Do nothing
            }
        }
    }

    private void doTab(final HttpServletRequest request) throws IOException {
        final WebWindow window = controller.getWindowManager().getWindow(
                request.getParameter("window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;
            final Client client = clients.get(request.getParameter("clientID"));

            wiw.getInputHandler(client,
                    request.getParameter("input"), request.getParameter(
                    "selstart"),
                    request.getParameter("selend")).doTabCompletion(false);
        }
    }

    private void doKeyUpDown(final boolean up, final HttpServletRequest request)
            throws IOException {
        final WebWindow window = controller.getWindowManager().getWindow(
                request.getParameter("window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;
            final Client client = clients.get(request.getParameter("clientID"));
            final WebInputHandler wih = wiw.getInputHandler(
                    client,
                    request.getParameter("input"),
                    request.getParameter("selstart"),
                    request.getParameter("selend"));

            if (up) {
                wih.doBufferUp();
            } else {
                wih.doBufferDown();
            }
        }
    }

    private void doNewServer(final HttpServletRequest request)
            throws IOException {
        try {
            serverManager.connectToAddress(
                    new URI("irc://" + request.getParameter("password") + "@"
                        + request.getParameter("server") + ":"
                        + request.getParameter("port")),
                    findProfile(request.getParameter("profile")));
        } catch (URISyntaxException ex) {
            // Ugh.
        }
    }

    private void doNicklist(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        final List<Event> nickEvents = new ArrayList<>();

        nickEvents.add(new Event("clearnicklist", false));

        for (ChannelClientInfo cci : ((Channel) (controller.getWindowManager()
                .getWindow(request.getParameter("window"))).getContainer())
                .getChannelInfo().getChannelClients()) {
            nickEvents.add(new Event("addnicklist",
                    cci.getClient().getNickname()));
        }

        response.getWriter().write(toJSON(nickEvents.toArray()));
    }

    private void doProfiles(final HttpServletResponse response) throws
            IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        final List<Event> profileEvents = new ArrayList<>();

        profileEvents.add(new Event("clearprofiles", null));

        for (ConfigProvider identity : identityController.getProvidersByType("profile")) {
            profileEvents.add(new Event("addprofile", identity.getName()));
        }

        response.getWriter().write(toJSON(profileEvents.toArray()));
    }

    private void doWindowRefresh(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        final List<Event> windowEvents = new ArrayList<>();

        final WebWindow window = controller.getWindowManager().getWindow(
                request.getParameter("window"));

        windowEvents.add(new Event("clearwindow", window.getId()));

        for (String line : window.getMessages()) {
            windowEvents.add(new Event("lineadded", new Message(line, window)));
        }

        response.getWriter().write(toJSON(windowEvents.toArray()));
    }

    private void doClients(final HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(toJSON(clients.values().toArray()));
    }

    private void doJoinChannel(final HttpServletRequest request)
            throws IOException {
        final String windowID = request.getParameter("source");
        final WebWindow window = controller.getWindowManager().getWindow(windowID);
        window.getContainer().getServer().join(new ChannelJoinRequest(request.
                getParameter("channel")));
    }

    private void doOpenQuery(final HttpServletRequest request)
            throws IOException {
        final String windowID = request.getParameter("source");
        final WebWindow window = controller.getWindowManager().getWindow(windowID);
        window.getContainer().getServer().getQuery(request.getParameter(
                "target"));
    }

    private ConfigProvider findProfile(final String parameter) {
        for (ConfigProvider identity : identityController.getProvidersByType("profile")) {
            if (identity.getName().equals(parameter)) {
                return identity;
            }
        }

        return null;
    }

    private void handled(final HttpServletRequest request) {
        ((request instanceof Request) ? (Request) request
                : HttpConnection.getCurrentConnection().getRequest())
                .setHandled(true);
    }

    public void addEvent(final Event event) {
        synchronized (clients) {
            for (Client client : clients.values()) {
                client.addEvent(event);
            }
        }
    }

    /**
     * Utility method to convert an object into a JSON string. Performs the
     * conversion using {@link JSON#toString(java.lang.Object)}, and then
     * post-processes the output to encode certain control characters.
     *
     * @param object The object to be JSON-ified.
     * @return A JSON representation of the specified object.
     */
    private static String toJSON(final Object object) {
        try {
            return JSON.toString(object).replace("\001", "\\u0001");
        } catch (RuntimeException ex) {
            log.error("Unable to encode JSON: {}", object, ex);
            throw ex;
        }
    }

}

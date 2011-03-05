/*
 * Copyright (c) 2006-2011 DMDirc Developers
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
import com.dmdirc.Server;
import com.dmdirc.addons.ui_web.uicomponents.WebInputHandler;
import com.dmdirc.addons.ui_web.uicomponents.WebInputWindow;
import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;
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

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;
import org.mortbay.util.ajax.JSON;
import org.mortbay.util.ajax.JSONObjectConvertor;

/**
 * Handles requests for dynamic resources (prefixed with /dynamic/).
 *
 * @author chris
 */
public class DynamicRequestHandler extends AbstractHandler {

    /** Number of milliseconds before a client is timed out. */
    private static final long TIMEOUT = 1000 * 60 * 2; // Two minutes

    /** The last time each client was seen. */
    private static final Map<String, Client> CLIENTS
            = new HashMap<String, Client>();

    /** The controller which owns this request handler. */
    private final WebInterfaceUI controller;

    /**
     * Creates a new instance of DynamicRequestHandler. Registers object
     * convertors with the JSON serialiser.
     */
    public DynamicRequestHandler(final WebInterfaceUI controller) {
        super();

        this.controller = controller;

        JSON.registerConvertor(Event.class, new JSONObjectConvertor());
        JSON.registerConvertor(WebWindow.class, new JSONObjectConvertor());
        JSON.registerConvertor(Message.class, new JSONObjectConvertor());
        JSON.registerConvertor(Client.class, new JSONObjectConvertor());

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (CLIENTS) {
                    for (Map.Entry<String, Client> entry
                            : new HashMap<String, Client>(CLIENTS).entrySet()) {
                        if (entry.getValue().getTime() > TIMEOUT) {
                            CLIENTS.remove(entry.getKey());
                        }
                    }
                }
            }
        }, ERROR, ERROR);
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

            if (!CLIENTS.containsKey(clientID)) {
                CLIENTS.put(clientID, new Client(controller,
                        request.getRemoteHost()));
            }

            synchronized (CLIENTS) {
                CLIENTS.get(clientID).touch();
            }
        }

        if (((request instanceof Request) ? (Request) request : HttpConnection
                .getCurrentConnection().getRequest()).isHandled()) {
            return;
        }

        if (target.equals("/dynamic/feed")) {
            doFeed(request, response);
            handled(request);
        } else if (target.equals("/dynamic/getprofiles")) {
            doProfiles(response);
            handled(request);
        } else if (target.equals("/dynamic/newserver")) {
            doNewServer(request);
            handled(request);
        } else if (target.equals("/dynamic/windowrefresh")) {
            doWindowRefresh(request, response);
            handled(request);
        } else if (target.equals("/dynamic/input")) {
            doInput(request);
            handled(request);
        } else if (target.equals("/dynamic/nicklistrefresh")) {
            doNicklist(request, response);
            handled(request);
        } else if (target.equals("/dynamic/tab")) {
            doTab(request);
            handled(request);
        } else if (target.equals("/dynamic/keyup")
                || target.equals("/dynamic/keydown")) {
            doKeyUpDown(target.equals("/dynamic/keyup"), request);
            handled(request);
        } else if (target.equals("/dynamic/key")) {
            doKey(request);
            handled(request);
        } else if (target.equals("/dynamic/clients")) {
            doClients(response);
            handled(request);
        } else if (target.equals("/dynamic/joinchannel")) {
            doJoinChannel(request);
            handled(request);
        } else if (target.equals("/dynamic/openquery")) {
            doOpenQuery(request);
            handled(request);
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

        final Client client = CLIENTS.get(request.getParameter("clientID"));

        synchronized (client.getMutex()) {
            List<Event> myEvents = client.retrieveEvents();

            if (myEvents.isEmpty()) {
                Continuation continuation = ContinuationSupport
                        .getContinuation(request, client.getMutex());
                client.setContinuation(continuation);
                continuation.suspend(30000L);

                myEvents = client.retrieveEvents();
            }

            client.setContinuation(null);

            final String json = JSON.toString(myEvents.toArray());
            response.getWriter().write(json);
        }
    }

    private void doInput(final HttpServletRequest request) throws IOException {
        final WebWindow window = WebWindow.getWindow(
                request.getParameter("window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;
            wiw.getInputHandler(request.getParameter("clientID")).enterPressed(
                    request.getParameter("input"));
        }
    }

    private void doKey(final HttpServletRequest request) throws IOException {
        final WebWindow window = WebWindow.getWindow(
                request.getParameter("window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;

            try {
                ((WebInputHandler) wiw.getInputHandler(
                        request.getParameter("clientID"),
                        request.getParameter("input"),
                        request.getParameter("selstart"),
                        request.getParameter("selend"))).handleKeyPressed(
                        request.getParameter("input"),
                        Integer.parseInt(request.getParameter("key")),
                        Boolean.parseBoolean(request.getParameter("shift")),
                        Boolean.parseBoolean(request.getParameter("ctrl")));
            } catch (NumberFormatException ex) {
                // Do nothing
            }
        }
    }

    private void doTab(final HttpServletRequest request) throws IOException {
        final WebWindow window = WebWindow.getWindow(request.getParameter(
                "window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;
            ((WebInputHandler) wiw.getInputHandler(request.getParameter(
                    "clientID"),
                    request.getParameter("input"), request.getParameter(
                    "selstart"),
                    request.getParameter("selend"))).doTabCompletion(false);
        }
    }

    private void doKeyUpDown(final boolean up, final HttpServletRequest request)
            throws IOException {
        final WebWindow window = WebWindow.getWindow(request.getParameter(
                "window"));

        if (window instanceof WebInputWindow) {
            final WebInputWindow wiw = (WebInputWindow) window;
            final WebInputHandler wih = ((WebInputHandler) wiw.getInputHandler(
                    request.getParameter("clientID"),
                    request.getParameter("input"),
                    request.getParameter("selstart"),
                    request.getParameter("selend")));

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
            new Server(new URI("irc://" + request.getParameter("password") + "@"
                    + request.getParameter("server") + ":"
                    + request.getParameter("port")),
                    findProfile(request.getParameter("profile"))).connect();
        } catch (URISyntaxException ex) {
            // Ugh.
        }
    }

    private void doNicklist(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        final List<Event> nickEvents = new ArrayList<Event>();

        nickEvents.add(new Event("clearnicklist", false));

        for (ChannelClientInfo cci : ((Channel) (WebWindow.getWindow(
                request.getParameter("window"))).getContainer())
                .getChannelInfo().getChannelClients()) {
            nickEvents.add(new Event("addnicklist",
                    cci.getClient().getNickname()));
        }

        response.getWriter().write(JSON.toString(nickEvents.toArray()));
    }

    private void doProfiles(final HttpServletResponse response) throws
            IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        final List<Event> profileEvents = new ArrayList<Event>();

        profileEvents.add(new Event("clearprofiles", null));

        for (Identity identity : IdentityManager.getCustomIdentities(
                "profile")) {
            profileEvents.add(new Event("addprofile", identity.getName()));
        }

        response.getWriter().write(JSON.toString(profileEvents.toArray()));
    }

    private void doWindowRefresh(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        final List<Event> windowEvents = new ArrayList<Event>();

        final WebWindow window = WebWindow.getWindow(request.getParameter(
                "window"));

        windowEvents.add(new Event("clearwindow", window.getId()));

        for (String line : window.getMessages()) {
            windowEvents.add(new Event("lineadded", new Message(line, window)));
        }

        response.getWriter().write(JSON.toString(windowEvents.toArray()));
    }

    private void doClients(final HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(JSON.toString(CLIENTS.values().toArray()));
    }

    private void doJoinChannel(final HttpServletRequest request)
            throws IOException {
        final String windowID = request.getParameter("source");
        final WebWindow window = WebWindow.getWindow(windowID);
        window.getContainer().getServer().join(new ChannelJoinRequest(request.
                getParameter("channel")));
    }

    private void doOpenQuery(final HttpServletRequest request)
            throws IOException {
        final String windowID = request.getParameter("source");
        final WebWindow window = WebWindow.getWindow(windowID);
        window.getContainer().getServer().getQuery(request.getParameter(
                "target"));
    }

    private Identity findProfile(final String parameter) {
        for (Identity identity : IdentityManager.getCustomIdentities(
                "profile")) {
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

    public static void addEvent(final Event event) {
        synchronized (CLIENTS) {
            for (Client client : CLIENTS.values()) {
                client.addEvent(event);
            }
        }
    }

    public static void addEvent(final String clientID, final Event event) {
        CLIENTS.get(clientID).addEvent(event);
    }

}

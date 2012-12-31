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

import com.dmdirc.util.resourcemanager.ResourceManager;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * Handles requests for DMDirc resources (URLs starting with /dmdirc/).
 */
public class DMDircRequestHandler extends AbstractHandler {

    /** The resource manager used for DMDirc resources. */
    private ResourceManager rm;

    /**
     * {@inheritDoc}
     *
     * @throws IOException If unable to write the response
     */
    @Override
    public void handle(final String target, final HttpServletRequest request,
            final HttpServletResponse response, final int dispatch)
            throws IOException {

        if (rm == null) {
            rm = ResourceManager.getResourceManager();
        }

        if (((request instanceof Request) ? (Request) request
                : HttpConnection.getCurrentConnection().getRequest()).isHandled()) {
            return;
        }

        if (target.startsWith("/dmdirc/")) {
            final String path = "com/dmdirc/res/" + target.substring(8);

            if (rm.resourceExists(path)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getOutputStream().write(rm.getResourceBytes(path));
                ((request instanceof Request) ? (Request) request
                        : HttpConnection.getCurrentConnection().getRequest())
                        .setHandled(true);
            }
        }
    }
}

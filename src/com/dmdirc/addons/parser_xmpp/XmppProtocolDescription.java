/*
 * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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

package com.dmdirc.addons.parser_xmpp;

import com.dmdirc.parser.interfaces.ProtocolDescription;

import java.net.URI;

/**
 * A description of the XMPP protocol.
 */
public class XmppProtocolDescription implements ProtocolDescription {

    /** {@inheritDoc} */
    @Override
    public int getDefaultPort() {
        return 5222;
    }

    /** {@inheritDoc} */
    @Override
    public String[] parseHostmask(final String hostmask) {
        final String[] parts = hostmask.split("/");
        return new String[] { parts[0], parts.length > 1 ? parts[1] : "unknown", "" };
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSecure(final URI uri) {
        return uri.getScheme().endsWith("s");
    }

}

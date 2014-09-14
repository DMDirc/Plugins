/*
 * Copyright (c) 2006-2014 DMDirc Developers
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

package com.dmdirc.addons.serverlists.io;

import com.dmdirc.addons.serverlists.ServerEntry;
import com.dmdirc.addons.serverlists.ServerGroup;
import com.dmdirc.interfaces.ConnectionManager;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityController;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Facilitates loading of a {@link com.dmdirc.addons.serverlists.ServerEntry} from a DMDirc
 * {@link com.dmdirc.interfaces.config.ConfigProvider}.
 *
 * @since 0.6.4
 */
public class ServerEntryReader {

    /** ServerManager that ServerEntrys use to create servers */
    private final ConnectionManager connectionManager;
    /** The controller to read/write settings with. */
    private final IdentityController identityController;
    /** The identity to read entries from. */
    private final ConfigProvider identity;

    public ServerEntryReader(final ConnectionManager connectionManager,
            final IdentityController identityController, final ConfigProvider identity) {
        this.connectionManager = connectionManager;
        this.identityController = identityController;
        this.identity = identity;
    }

    /**
     * Attempts to read the details of the specified server from this reader's identity.
     *
     * @param group The group that owns this server
     * @param name  The name of the server to be read
     *
     * @return A corresponding ServerEntry
     *
     * @throws URISyntaxException       If the server doesn't specify a valid URI
     * @throws IllegalArgumentException If the server doesn't define a name or address
     */
    public ServerEntry read(final ServerGroup group, final String name) throws URISyntaxException,
            IllegalArgumentException {
        if (!identity.hasOptionString(name, "name")
                || !identity.hasOptionString(name, "address")) {
            throw new IllegalArgumentException("Server does not specify name or address: "
                    + name);
        }

        final String serverName = identity.getOption(name, "name");
        final URI serverURI = new URI(identity.getOption(name, "address"));

        return new ServerEntry(identityController, connectionManager, group, serverName, serverURI, null);
    }

}

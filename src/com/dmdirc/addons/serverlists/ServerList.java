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

package com.dmdirc.addons.serverlists;

import com.dmdirc.Precondition;
import com.dmdirc.ServerManager;
import com.dmdirc.addons.serverlists.io.ServerGroupReader;
import com.dmdirc.addons.serverlists.io.ServerGroupWriter;
import com.dmdirc.addons.serverlists.service.ServerListServiceProvider;
import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.ConfigProviderListener;
import com.dmdirc.interfaces.config.IdentityController;
import com.dmdirc.interfaces.config.IdentityFactory;
import com.dmdirc.plugins.PluginManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Maintains a list of top level {@link ServerGroup}s and handles reading and writing of the lists
 * to disk.
 *
 * @since 0.6.4
 */
public class ServerList implements ConfigProviderListener {

    /** A list of all known groups. */
    private final Map<ServerGroup, ServerGroupWriter> groups = new HashMap<>();
    /** ServerManager that ServerEntrys use to create servers */
    private final ServerManager serverManager;
    /** The controller to read/write settings with. */
    private final IdentityController identityController;
    /** The factory to create new identities with. */
    private final IdentityFactory identityFactory;

    /**
     * Creates a new ServerList and loads groups and servers.
     *
     * @param pluginManager      Plugin Manager to use.
     * @param serverManager      Server Manager to use.
     * @param identityController The controller to read/write settings with.
     * @param identityFactory    The factory to create new identities with.
     */
    @Inject
    public ServerList(
            final PluginManager pluginManager,
            final ServerManager serverManager,
            final IdentityController identityController,
            final IdentityFactory identityFactory) {
        this.serverManager = serverManager;
        this.identityController = identityController;
        this.identityFactory = identityFactory;

        identityController.registerIdentityListener("servergroup", this);

        for (ConfigProvider identity : identityController.getProvidersByType("servergroup")) {
            configProviderAdded(identity);
        }

        new ServerListServiceProvider(pluginManager, this).register();
    }

    /**
     * Adds a server group to the master server list.
     *
     * @param group  The group to be added
     * @param writer The writer to use to write the group to disk
     */
    public void addServerGroup(final ServerGroup group, final ServerGroupWriter writer) {
        groups.put(group, writer);
    }

    /**
     * Adds a server group to the master server list, and creates a new writer which will write the
     * group to an identity.
     *
     * @param group The group to be added
     *
     * @throws IOException if the new identity cannot be written
     */
    public void addServerGroup(final ServerGroup group) throws IOException {
        final ServerGroupWriter writer = new ServerGroupWriter(
                identityFactory.createCustomConfig(group.getName(), "servergroup"));
        group.setModified(true);
        addServerGroup(group, writer);
    }

    /**
     * Saves all entries in this list.
     */
    public void save() {
        for (Map.Entry<ServerGroup, ServerGroupWriter> pair : groups.entrySet()) {
            if (pair.getKey().isModified()) {
                pair.getValue().write(pair.getKey());
            }
        }
    }

    /**
     * Saves the specified group.
     *
     * @param group The group to be saved
     */
    @Precondition("Specified group is a known top-level group in this list")
    public void save(final ServerGroup group) {
        groups.get(group).write(group);
    }

    /**
     * Retrieves a list of all known server groups.
     *
     * @return An immutable list of server groups.
     */
    public Collection<ServerGroup> getServerGroups() {
        return Collections.unmodifiableCollection(groups.keySet());
    }

    /**
     * Retrieves a ServerGroup with the specified name, if one exists. This method ignores the case
     * of group's name when comparing.
     *
     * @param name The name of the group to be retrieved
     *
     * @return A correspondingly named server group, or null if none exists
     */
    public ServerGroup getGroupByName(final String name) {
        for (ServerGroup group : getServerGroups()) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }

        return null;
    }

    @Override
    public void configProviderAdded(final ConfigProvider configProvider) {
        try {
            final ServerGroupReader reader
                    = new ServerGroupReader(serverManager, identityController, configProvider);
            addServerGroup(reader.read(), reader.getWriter());
        } catch (IllegalArgumentException ex) {
            // Silently ignore
            // TODO: Raise error if the identity isn't a server group being
            //       currently added by addServerGroup()
        }
    }

    @Override
    public void configProviderRemoved(final ConfigProvider configProvider) {
        // TODO: Remove server group
    }

}

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

package com.dmdirc.addons.dns;

import com.dmdirc.plugins.PluginInfo;
import com.dmdirc.plugins.implementations.BaseCommandPlugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import dagger.ObjectGraph;

/**
 * DNS plugin.
 */
public final class DNSPlugin extends BaseCommandPlugin {

    @Override
    public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
        super.load(pluginInfo, graph);

        setObjectGraph(graph.plus(new DNSModule()));
        registerCommand(DNSCommand.class, DNSCommand.INFO);
    }

    /**
     * Returns the IP(s) for a hostname.
     *
     * @param hostname Hostname to resolve.
     *
     * @return Resolved IP(s)
     */
    public static String getIPs(final String hostname) {
        Collection<String> results = new ArrayList<>();

        try {
            final InetAddress[] ips = InetAddress.getAllByName(hostname);

            for (InetAddress ip : ips) {
                results.add(ip.getHostAddress());
            }

        } catch (UnknownHostException ex) {
            results = new ArrayList<>();
        }

        return results.toString();
    }

    /**
     * Returns the hostname for an ip.
     *
     * @param ip IP to resolve
     *
     * @return Resolved hostname
     */
    public static String getHostname(final String ip) {
        try {
            return InetAddress.getByName(ip).getHostName();
        } catch (UnknownHostException ex) {
            return "";
        }
    }

}
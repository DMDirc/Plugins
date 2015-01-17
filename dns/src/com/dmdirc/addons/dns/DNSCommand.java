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

package com.dmdirc.addons.dns;

import com.dmdirc.commandparser.BaseCommandInfo;
import com.dmdirc.commandparser.CommandArguments;
import com.dmdirc.commandparser.CommandInfo;
import com.dmdirc.commandparser.CommandType;
import com.dmdirc.commandparser.commands.Command;
import com.dmdirc.commandparser.commands.context.CommandContext;
import com.dmdirc.interfaces.CommandController;
import com.dmdirc.interfaces.WindowModel;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Performs DNS lookups for nicknames, hostnames or IPs.
 */
public class DNSCommand extends Command {

    /** A command info object for this command. */
    public static final CommandInfo INFO = new BaseCommandInfo("dns",
            "dns <IP|hostname> - Performs DNS lookup of the specified ip/hostname/nickname",
            CommandType.TYPE_GLOBAL);

    /**
     * Creates a new instance of this command.
     *
     * @param controller The controller to use for command information.
     */
    @Inject
    public DNSCommand(final CommandController controller) {
        super(controller);
    }

    @Override
    public void execute(@Nonnull final WindowModel origin,
            final CommandArguments args, final CommandContext context) {
        if (args.getArguments().length == 0) {
            showUsage(origin, args.isSilent(), "dns", "<IP|hostname>");
            return;
        }

        sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Resolving: " + args.getArguments()[0]);
        new Timer("DNS Command Timer").schedule(new TimerTask() {

            @Override
            public void run() {
                resolve(origin, args.isSilent(), args.getArguments()[0]);
            }
        }, 0);
    }

    private void resolve(@Nonnull final WindowModel origin, final boolean isSilent,
            final String arg) {
        try {
            final InetAddress address = InetAddresses.forString(arg);
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Resolved: "
                    + arg + ": " + address.getCanonicalHostName());
        } catch (IllegalArgumentException ex) {
            sendLine(origin, isSilent, FORMAT_OUTPUT, "Resolved: "
                    + arg + ": " + getIPs(arg));
        }
    }

    /**
     * Returns the IP(s) for a hostname.
     *
     * @param hostname Hostname to resolve.
     *
     * @return Resolved IP(s)
     */
    private String getIPs(final String hostname) {
        final Collection<String> results = new ArrayList<>();

        try {
            final InetAddress[] ips = InetAddress.getAllByName(hostname);

            for (InetAddress ip : ips) {
                results.add(ip.getHostAddress());
            }

        } catch (UnknownHostException ex) {
            return "[]";
        }

        return results.toString();
    }

}

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

package com.dmdirc.addons.identd;

import com.dmdirc.Server;
import com.dmdirc.ServerManager;
import com.dmdirc.config.ConfigManager;
import com.dmdirc.parser.irc.IRCClientInfo;
import com.dmdirc.parser.irc.IRCParser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IdentClientTest {

    protected IdentClient getClient() {
        IdentdPlugin plugin = mock(IdentdPlugin.class);
        ServerManager sm = mock(ServerManager.class);
        Server server = mock(Server.class);
        IRCParser parser = mock(IRCParser.class);
        IRCClientInfo client = mock(IRCClientInfo.class);
        final List<Server> servers = new ArrayList<Server>();
        servers.add(server);

        when(plugin.getDomain()).thenReturn("plugin-Identd");
        when(sm.getServers()).thenReturn(servers);
        when(server.getParser()).thenReturn(parser);
        when(parser.getLocalPort()).thenReturn(60);
        when(parser.getLocalClient()).thenReturn(client);
        when(client.getNickname()).thenReturn("nickname");
        when(client.getUsername()).thenReturn("username");

        return new IdentClient(null, null, plugin, sm);
    }

    @Test
    public void testInvalidIdent() {
        final ConfigManager cm = mock(ConfigManager.class);
        final String response = getClient().getIdentResponse("invalid request!", cm);

        assertContains("Illegal requests must result in an ERROR response",
                response, "ERROR");
    }

    @Test
    public void testQuoting() {
        final ConfigManager cm = mock(ConfigManager.class);
        final String response = getClient().getIdentResponse("in\\valid:invalid", cm);

        assertStartsWith("Special chars in illegal requests must be quoted",
                response, "in\\\\valid\\:invalid");
    }

    @Test
    public void testQuoting2() {
        final ConfigManager cm = mock(ConfigManager.class);
        final String response = getClient().getIdentResponse("in\\\\valid\\ inv\\:alid", cm);

        assertStartsWith("Escaped characters in illegal requests shouldn't be doubly-escaped",
                response, "in\\\\valid\\ inv\\:alid");
    }

    @Test
    public void testNonNumericPort() {
        final ConfigManager cm = mock(ConfigManager.class);
        final String response = getClient().getIdentResponse("abc, def", cm);

        assertContains("Non-numeric ports must result in an ERROR response",
                response, "ERROR");
        assertStartsWith("Specified ports must be returned in the response",
                response.replaceAll("\\s+", ""), "abc,def:");
    }

    private void doPortTest(final String ports) {
        final ConfigManager cm = mock(ConfigManager.class);
        final String response = getClient().getIdentResponse(ports, cm);

        assertContains("Illegal ports must result in an ERROR response",
                response, "ERROR");
        assertContains("Illegal ports must result in an INVALID-PORT response",
                response, "INVALID-PORT");
        assertStartsWith("Port numbers must be returned as part of the response",
                response.replaceAll("\\s+", ""), ports.replaceAll("\\s+", ""));
    }

    @Test
    public void testOutOfRangePorts() {
        doPortTest("0, 50");
        doPortTest("65536, 50");
        doPortTest("50, 0");
        doPortTest("50, 65536");
    }

    @Test
    public void testAlwaysOn() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(false);

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertContains("Unknown port requests must return an ERROR response",
                response, "ERROR");
        assertContains("Unknown port requests must return a NO-USER response",
                response, "NO-USER");
    }

    @Test
    public void testHidden() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(true);

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertContains("Hidden requests must return an ERROR response",
                response, "ERROR");
        assertContains("Hidden requests must return a HIDDEN-USER response",
                response, "HIDDEN-USER");
    }

    @Test
    public void testSystemNameQuoting() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(false);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(true);
        when(cm.getOption("plugin-Identd", "advanced.customSystem")).thenReturn("a:b\\c,d");
        when(cm.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(false);
        when(cm.getOption("plugin-Identd", "general.customName")).thenReturn("");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertContains("Special characters must be quoted in system names",
                response, "a\\:b\\\\c\\,d");
    }

    @Test
    public void testCustomNameQuoting() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(false);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        when(cm.getOption("plugin-Identd", "advanced.customSystem")).thenReturn("");
        when(cm.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(true);
        when(cm.getOption("plugin-Identd", "general.customName")).thenReturn("a:b\\c,d");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertContains("Special characters must be quoted in custom names",
                response, "a\\:b\\\\c\\,d");
    }

    @Test
    public void testCustomNames() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.isHiddenUser")).thenReturn(false);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(true);
        when(cm.getOption("plugin-Identd", "advanced.customSystem")).thenReturn("system");
        when(cm.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(true);
        when(cm.getOption("plugin-Identd", "general.customName")).thenReturn("name");

        final String response = getClient().getIdentResponse("50, 60", cm);
        final String[] bits = response.split(":");

        assertTrue("Responses must include port pair",
                bits[0].matches("\\s*50\\s*,\\s*60\\s*"));
        assertEquals("Positive response must include USERID",
                "USERID", bits[1].trim());
        assertEquals("Must use custom system name", "system", bits[2].trim());
        assertEquals("Must use custom name", "name", bits[3].trim());
    }

    @Test
    public void testOSWindows() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "windows");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : WIN32 : test", response);
    }

    @Test
    public void testOSMac() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "mac");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : MACOS : test", response);
    }

    @Test
    public void testOSLinux() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "linux");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : UNIX : test", response);
    }

    @Test
    public void testOSBSD() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "bsd");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : UNIX-BSD : test", response);
    }

    @Test
    public void testOSOS2() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "os/2");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : OS/2 : test", response);
    }

    @Test
    public void testOSUnix() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "unix");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : UNIX : test", response);
    }

    @Test
    public void testOSIrix() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "irix");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : IRIX : test", response);
    }

    @Test
    public void testOSUnknown() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "test");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : UNKNOWN : test", response);
    }

    @Test
    public void testNameSystem() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "advanced.useCustomSystem")).thenReturn(false);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "test");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : UNKNOWN : test", response);
    }

    @Test
    public void testNameCustom() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "advanced.alwaysOn")).thenReturn(true);
        when(cm.getOptionBool("plugin-Identd", "general.useCustomName")).thenReturn(true);
        when(cm.getOption("plugin-Identd", "general.customName")).thenReturn("name");
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "test");

        final String response = getClient().getIdentResponse("50, 50", cm);
        assertEquals("50 , 50 : USERID : UNKNOWN : name", response);
    }

    @Test
    public void testNameNickname() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "general.useNickname")).thenReturn(true);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "test");

        final String response = getClient().getIdentResponse("60, 50", cm);
        assertEquals("60 , 50 : USERID : UNKNOWN : nickname", response);
    }

    @Test
    public void testNameUsername() {
        final ConfigManager cm = mock(ConfigManager.class);
        when(cm.getOptionBool("plugin-Identd", "general.useUsername")).thenReturn(true);
        System.setProperty("user.name", "test");
        System.setProperty("os.name", "test");

        final String response = getClient().getIdentResponse("60, 50", cm);
        assertEquals("60 , 50 : USERID : UNKNOWN : username", response);
    }

    private static void assertContains(final String msg, final String haystack,
            final String needle) {
        assertTrue(msg, haystack.indexOf(needle) > -1);
    }

    private static void assertStartsWith(final String msg, final String haystack,
            final String needle) {
        assertTrue(msg, haystack.startsWith(needle));
    }

}

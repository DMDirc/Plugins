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

import com.dmdirc.interfaces.IdentityController;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;

/**
 * Describes the users allowed to access the web UI.
 */
@RequiredArgsConstructor
public class WebUserRealm implements UserRealm {

    /** A map of known principals. */
    private final Map<String, Principal> principals = new HashMap<>();

    /** The config source to retrieve user information from. */
    private final IdentityController identityController;

    /** The domain to use when retrieving configuration. */
    private final String domain;

    /** {@inheritDoc} */
    @Override
    public String getName() {
        if (identityController.getGlobalConfiguration().hasOptionString(domain, "users")) {
            return "DMDirc web UI";
        } else {
            return "DMDirc web UI first run -- "
                    + "enter the username and password you wish to use in "
                    + "the future";
        }
    }

    /** {@inheritDoc} */
    @Override
    public Principal getPrincipal(final String username) {
        return principals.get(username);
    }

    /** {@inheritDoc} */
    @Override
    public Principal authenticate(final String username,
            final Object credentials, final Request request) {
        if (!identityController.getGlobalConfiguration().hasOptionString(domain, "users")) {
            final List<String> users = new ArrayList<>();
            users.add(username + ":" + getHash(username, credentials));
            identityController.getGlobalConfigIdentity().setOption(domain, "users", users);
        }

        for (String userinfo : identityController.getGlobalConfiguration().getOptionList(domain, "users")) {
            if (userinfo.startsWith(username + ":")) {
                final String pass = userinfo.substring(username.length() + 1);

                if (pass.equals(getHash(username, credentials))) {
                    principals.put(username, new WebPrincipal(username));
                    return getPrincipal(username);
                }
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean reauthenticate(final Principal user) {
        return principals.containsValue(user);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUserInRole(final Principal user, final String role) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void disassociate(final Principal user) {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public Principal pushRole(final Principal user, final String role) {
        // Do nothing
        return user;
    }

    /** {@inheritDoc} */
    @Override
    public Principal popRole(final Principal user) {
        // Do nothing
        return user;
    }

    /** {@inheritDoc} */
    @Override
    public void logout(final Principal user) {
        principals.remove(user.getName());
    }

    private String getHash(final String username, final Object credentials) {
        final String target = username + "--" + (String) credentials;

        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");

            return new BigInteger(md.digest(target.getBytes())).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            // Don't hash
            return target;
        }
    }

}

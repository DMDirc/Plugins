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

package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.interfaces.config.ConfigProvider;
import com.dmdirc.interfaces.config.IdentityFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Profile wrapper class.
 */
public class Profile {

    /** Identity backing this profile. */
    private ConfigProvider configProvider;
    /** Profile Name, must be a sanitised filename. */
    private String name;
    /** Real name. */
    private String realname;
    /** Ident. */
    private String ident;
    /** Nicknames. */
    private List<String> nicknames;
    /** Has this profile been marked deleted? */
    private boolean deleted = false;
    /** Factory to use to create profiles when saving. */
    private final IdentityFactory identityFactory;

    /**
     * Creates a new profile.
     *
     * @param identityFactory The factory to use to create the profile's config file when saving.
     */
    public Profile(final IdentityFactory identityFactory) {
        this(identityFactory, null);
    }

    /**
     * Creates a new profile based off the specified Identity.
     *
     * @param identityFactory The factory to use to create the profile's config file when saving.
     * @param configProvider Provider to read existing profile from. If null, a blank profile is created.
     */
    public Profile(final IdentityFactory identityFactory, @Nullable final ConfigProvider configProvider) {
        this.identityFactory = identityFactory;
        this.configProvider = configProvider;

        if (configProvider == null) {
            name = "New Profile";
            nicknames = new ArrayList<>();
            realname = "";
            ident = "";
        } else {
            name = configProvider.getOption("identity", "name");
            nicknames = configProvider.getOptionList("profile", "nicknames");
            realname = configProvider.getOption("profile", "realname");
            ident = configProvider.getOption("profile", "ident");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(final String realname) {
        this.realname = realname;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(final String ident) {
        this.ident = ident;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(final List<String> nicknames) {
        this.nicknames = nicknames;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Adds a nickname to this profile.
     *
     * @param nickname A new nickname for the profile
     */
    public void addNickname(final String nickname) {
        if (!nicknames.contains(nickname)) {
            nicknames.add(nickname);
        }
    }

    /**
     * Adds a nickname to this profile.
     *
     * @param nickname A new nickname for the profile
     * @param position Position for the new alternate nickname
     */
    public void addNickname(final String nickname, final int position) {
        if (!nicknames.contains(nickname)) {
            nicknames.add(position, nickname);
        }
    }

    /**
     * Deletes a nickname from this profile.
     *
     * @param nickname An existing nickname from the profile
     */
    public void delNickname(final String nickname) {
        nicknames.remove(nickname);
    }

    /**
     * Edits a nickname in the list.
     *
     * @param nickname Nickname to edit
     * @param newNickname Edited nickname
     */
    public void editNickname(final String nickname, final String newNickname) {
        if (nickname.isEmpty() || newNickname.isEmpty()) {
            return;
        }
        if (!nickname.equals(newNickname)) {
            final int index = nicknames.indexOf(nickname);
            nicknames.remove(nickname);
            nicknames.add(index, newNickname);
        }
    }

    /** Saves this profile. */
    public void save() {
        if (configProvider == null) {
            configProvider = identityFactory.createProfileConfig(name);
        }

        configProvider.setOption("identity", "name", name);
        configProvider.setOption("profile", "nicknames", nicknames);
        configProvider.setOption("profile", "realname", realname);
        configProvider.setOption("profile", "ident", ident);
    }

    /**
     * Deletes the profile.
     */
    public void delete() {
        if (configProvider == null) {
            return;
        }
        configProvider.delete();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.realname);
        hash = 59 * hash + Objects.hashCode(this.ident);
        hash = 59 * hash + Objects.hashCode(this.nicknames);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Profile other = (Profile) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.realname, other.realname)) {
            return false;
        }
        if (!Objects.equals(this.ident, other.ident)) {
            return false;
        }
        if (!Objects.equals(this.nicknames, other.nicknames)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Profile{" + "name=" + name + ", realname=" + realname
                + ", ident=" + ident + ", nicknames=" + nicknames + ", deleted=" + deleted + '}';
    }
}

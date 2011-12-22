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

package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.config.Identity;
import com.dmdirc.config.IdentityManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Profile wrapper class. */
public class Profile {

    /** Old Name. */
    private String oldName;
    /** Name. */
    private String name;
    /** Real name. */
    private String realname;
    /** Ident. */
    private String ident;
    /** Nicknames. */
    private List<String> nicknames;
    /** Does this profile need saving? */
    private boolean modified;
    /** Has this profile been marked deleted? */
    private boolean deleted = false;

    /** Creates a new profile. */
    public Profile() {
        this("");
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     */
    public Profile(final String name) {
        this(name, null);
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     */
    public Profile(final String name, final String nickname) {
        this(name, nickname, "");
    }

    /**
     *
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     * @param realname Profile's realname
     */
    public Profile(final String name, final String nickname,
            final String realname) {
        this(name, nickname, realname, "");
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nickname Profile's nickname
     * @param realname Profile's realname
     * @param ident Profile's ident
     */
    public Profile(final String name, final String nickname,
            final String realname, final String ident) {
        this(name, (nickname == null) ? new ArrayList<String>()
                : Arrays.asList(new String[]{nickname, }), realname, ident);
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nicknames Profile's nicknames
     * @param realname Profile's realname
     * @param ident Profile's ident
     */
    public Profile(final String name, final List<String> nicknames,
            final String realname, final String ident) {
        this(name, nicknames, realname, ident, true);
    }

    /**
     * Creates a new profile.
     *
     * @param name Profile's name
     * @param nicknames Profile's nickname
     * @param realname Profile's realname
     * @param ident Profile's ident
     * @param modified Has this profile been modified
     */
    public Profile(final String name, final List<String> nicknames,
            final String realname, final String ident,
            final boolean modified) {
        this.oldName = name;
        this.name = name;
        this.nicknames = new ArrayList<String>(nicknames);
        this.realname = realname;
        this.ident = ident;
        this.modified = modified;
    }

    /**
     * Returns the name of this profile.
     *
     * @return Profile's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this profile.
     *
     * @param name Profile's new name
     */
    public void setName(final String name) {
        if (!this.name.equals(name)) {
            this.oldName = this.name;
            this.name = name;
            setModified(true);
        }
    }

    /**
     * Gets the nicknames list for this profile.
     *
     * @return Profile's nicknames list
     */
    public List<String> getNicknames() {
        return new ArrayList<String>(nicknames);
    }

    /**
     * Sets the nicknames list for this profile.
     *
     * @param nicknames Profile's new nicknames list
     */
    public void setNicknames(final List<String> nicknames) {
        if (!this.nicknames.equals(nicknames)) {
            this.nicknames = new ArrayList<String>(nicknames);
            setModified(true);
        }
    }

    /**
     * Adds a nickname to this profile.
     *
     * @param nickname A new nickname for the profile
     */
    public void addNickname(final String nickname) {
        if (!nicknames.contains(nickname)) {
            nicknames.add(nickname);
            setModified(true);
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
            setModified(true);
        }
    }

    /**
     * Deletes a nickname from this profile.
     *
     * @param nickname An existing nickname from the profile
     */
    public void delNickname(final String nickname) {
        if (nicknames.remove(nickname)) {
            setModified(true);
        }
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
            setModified(true);
        }
    }

    /**
     * Gets the specified nickname for this profile
     *
     * @param index Index of the nickname to retrieve
     *
     * @return Profile's nickname
     */
    public String getNickname(final int index) {
        return nicknames.get(index);
    }

    /**
     * Gets the realname for this profile.
     *
     * @return Profile's realname
     */
    public String getRealname() {
        return realname;
    }

    /**
     * Sets the realname for this profile.
     *
     * @param realname Profile's new realname
     */
    public void setRealname(final String realname) {
        if (!this.realname.equals(realname)) {
            this.realname = realname;
            setModified(true);
        }
    }

    /**
     * Gets the ident for this profile.
     *
     * @return Profile's ident
     */
    public String getIdent() {
        return ident;
    }

    /**
     * Sets the ident for this profile.
     *
     * @param ident Profile's new ident
     */
    public void setIdent(final String ident) {
        if (this.ident == null || !this.ident.equals(ident)) {
            this.ident = ident;
            setModified(true);
        }
    }

    /**
     * Has this profile been modified?
     *
     * @return true iif the profile has been modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Sets whether the profile has been modified.
     *
     * @param modified Modified state for the profile
     */
    public void setModified(final boolean modified) {
        this.modified = modified;
    }

    /**
     * Has this profile been marked deleted?
     *
     * @return true iif the profile has been marked deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Sets whether the profile has been deleted.
     *
     * @param deleted Marks this profile deletion
     */
    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    /** Saves this profile. */
    public void save() {
        if (modified) {
            final String profileString = "profile";
            final List<Identity> identities = IdentityManager
                    .getCustomIdentities("profile");
            Identity profile = null;

            for (Identity identity : identities) {
                if (identity.getName().equalsIgnoreCase(oldName)) {
                    profile = identity;
                    break;
                }
            }

            if (profile == null) {
                try {
                    profile = Identity.buildProfile(name);
                } catch (IOException ex) {
                    // TODO: ??
                }
            }

            profile.setOption("identity", "name", name);
            profile.setOption(profileString, "nicknames", nicknames);
            profile.setOption(profileString, "realname", realname);
            profile.setOption(profileString, "ident", ident);
            modified = false;
            this.oldName = name;
        }
    }

    /** Deletes the profile. */
    public void delete() {
        final List<Identity> identities = IdentityManager.getCustomIdentities(
                "profile");
        Identity profile = null;

        for (Identity identity : identities) {
            if (identity.getName().equals(name)) {
                profile = identity;
                break;
            }
        }

        if (profile == null) {
            return;
        }
        profile.delete();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Profile other = (Profile) obj;

        if (!this.name.equals(other.name)) {
            return false;
        }
        if (!this.nicknames.equals(other.nicknames)) {
            return false;
        }
        if (!this.realname.equals(other.realname)) {
            return false;
        }
        if (this.ident == null && other.ident != null) {
            return false;
        }
        if (this.ident != null && !this.ident.equals(other.ident)) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash +
                (this.nicknames != null ? this.nicknames.hashCode() : 0);
        hash = 79 * hash +
                (this.realname != null ? this.realname.hashCode() : 0);
        hash = 79 * hash + (this.ident != null ? this.ident.hashCode() : 0);

        return hash;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "[Profile: name='" + name + "', nickname='" + nicknames +
                "', realname='" + realname + "', ident='" + ident +
                "', modified='" + modified + "', deleted='" + deleted + "']";
    }
}

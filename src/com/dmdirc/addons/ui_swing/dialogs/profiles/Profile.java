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

package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.config.IdentityManager;
import com.dmdirc.interfaces.config.ConfigProvider;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Profile wrapper class.
 */
@EqualsAndHashCode(exclude = {"identity", "deleted"})
@ToString(exclude = "identity")
@SuppressWarnings("unused")
public class Profile {

    /** Identity backing this profile. */
    private ConfigProvider identity;
    /** Profile Name, must be a sanitised filename. */
    @Getter @Setter
    private String name;
    /** Real name. */
    @Getter @Setter
    private String realname;
    /** Ident. */
    @Getter @Setter
    private String ident;
    /** Nicknames. */
    @Getter @Setter
    private List<String> nicknames;
    /** Has this profile been marked deleted? */
    @Getter @Setter
    private boolean deleted = false;

    /** Creates a new profile. */
    public Profile() {
        this(null);
    }

    /**
     * Creates a new profile based off the specified Identity.
     *
     * @param identity Identity to create profile from
     */
    public Profile(final ConfigProvider identity) {
        this.identity = identity;
        if (identity == null) {
            name = "New Profile";
            nicknames = new ArrayList<>();
            realname = "";
            ident = "";
        } else {
            name = identity.getOption("identity", "name");
            nicknames = identity.getOptionList("profile", "nicknames");
            realname = identity.getOption("profile", "realname");
            ident = identity.getOption("profile", "ident");
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
        if (identity == null) {
            identity = IdentityManager.getIdentityManager().createProfileConfig(name);
        }

        identity.setOption("identity", "name", name);
        identity.setOption("profile", "nicknames", nicknames);
        identity.setOption("profile", "realname", realname);
        identity.setOption("profile", "ident", ident);
    }

    /**
     * Deletes the profile.
     */
    public void delete() {
        if (identity == null) {
            return;
        }
        identity.delete();
    }
}

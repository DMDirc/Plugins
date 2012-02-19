/*
 * Copyright (c) 2006-2012 DMDirc Developers
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.config.Identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test profile class.
 */
public class ProfileTest {

    private Profile createProfile() {
        final List<String> nicknames = new ArrayList<String>();
        nicknames.add("nickname1");
        nicknames.add("nickname2");
        final Identity identity = mock(Identity.class);
        when(identity.getName()).thenReturn("profile");
        when(identity.getOption("identity", "name")).thenReturn("profile");
        when(identity.getOptionList("profile", "nicknames")).thenReturn(nicknames);
        when(identity.getOption("profile", "realname")).thenReturn("realname");
        when(identity.getOption("profile", "ident")).thenReturn("ident");

        return new Profile(identity);
    }

    /**
     * Test null profile constructor.
     */
    @Test
    public void testEmptyConstructor() {
        Profile instance = new Profile();
        assertEquals("", instance.getIdent());
        assertEquals("New Profile", instance.getName());
        assertEquals(new ArrayList<String>(), instance.getNicknames());
        assertEquals("", instance.getRealname());
    }

    /**
     * Test null profile constructor.
     */
    @Test
    public void testIdentityConstructor() {
        Profile instance = createProfile();
        assertEquals("ident", instance.getIdent());
        assertEquals("profile", instance.getName());
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2", })
                .equals(instance.getNicknames()));
        assertEquals("realname", instance.getRealname());
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNickname_String() {
        Profile instance = createProfile();
        instance.addNickname("nickname3");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2",
            "nickname3"}).equals(instance.getNicknames()));
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNickname_String_Contains() {
        Profile instance = createProfile();
        instance.addNickname("nickname2");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNickname_String_int() {
        Profile instance = createProfile();
        instance.addNickname("nickname3", 0);
        assertTrue(Arrays.asList(new String[]{"nickname3", "nickname1",
            "nickname2"}).equals(instance.getNicknames()));
    }

    /**
     * Test of addNickname method, of class Profile.
     */
    @Test
    public void testAddNickname_String_int_Contains() {
        Profile instance = createProfile();
        instance.addNickname("nickname2", 0);
        assertTrue(Arrays.asList(new String[]{"nickname1","nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of delNickname method, of class Profile.
     */
    @Test
    public void testDelNickname() {
        Profile instance = createProfile();
        instance.delNickname("nickname2");
        assertTrue(Arrays.asList(new String[]{"nickname1"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of save method, of class Profile.
     */
    @Test
    public void testSave() {
        final List<String> nicknames = new ArrayList<String>();
        nicknames.add("nickname1");
        nicknames.add("nickname2");
        final Identity identity = mock(Identity.class);
        when(identity.getName()).thenReturn("profile");
        when(identity.getOption("identity", "name")).thenReturn("profile");
        when(identity.getOptionList("profile", "nicknames")).thenReturn(nicknames);
        when(identity.getOption("profile", "realname")).thenReturn("realname");
        when(identity.getOption("profile", "ident")).thenReturn("ident");

        Profile instance = new Profile(identity);
        instance.save();
        verify(identity).setOption("identity", "name", "profile");
        verify(identity).setOption("profile", "nicknames", nicknames);
        verify(identity).setOption("profile", "realname", "realname");
        verify(identity).setOption("profile", "ident", "ident");
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNicknameOldEmpty() {
        Profile instance = createProfile();
        instance.editNickname("", "nickname3");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNicknameNewEmpty() {
        Profile instance = createProfile();
        instance.editNickname("nickname2", "");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNicknameSame() {
        Profile instance = createProfile();
        instance.editNickname("nickname2", "nickname2");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname2"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of editNickname method, of class Profile.
     */
    @Test
    public void testEditNickname() {
        Profile instance = createProfile();
        instance.editNickname("nickname2", "nickname3");
        assertTrue(Arrays.asList(new String[]{"nickname1", "nickname3"})
                .equals(instance.getNicknames()));
    }

    /**
     * Test of delete method, of class Profile.
     */
    @Test
    public void testDelete() {
        final List<String> nicknames = new ArrayList<String>();
        nicknames.add("nickname1");
        nicknames.add("nickname2");
        final Identity identity = mock(Identity.class);
        when(identity.getName()).thenReturn("profile");
        when(identity.getOption("identity", "name")).thenReturn("profile");
        when(identity.getOptionList("profile", "nicknames")).thenReturn(nicknames);
        when(identity.getOption("profile", "realname")).thenReturn("realname");
        when(identity.getOption("profile", "ident")).thenReturn("ident");

        Profile instance = new Profile(identity);
        instance.delete();
        verify(identity).delete();
    }

    /**
     * Test of delete method, of class Profile.
     */
    @Test
    public void testDeleteNullIdentity() {
        Profile instance = new Profile(null);
        instance.delete();
    }
}

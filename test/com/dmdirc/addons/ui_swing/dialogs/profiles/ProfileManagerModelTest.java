/*
 * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
import com.dmdirc.util.validators.ValidationResponse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

/**
 *
 * @author greboid
 */
public class ProfileManagerModelTest {

    private static IdentityManager manager;
    private static List<Identity> identities;
    private static List<Profile> profiles;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setUpClass() throws Exception {
        final Identity identity1 = mock(Identity.class);
        when(identity1.getName()).thenReturn("1identity");
        when(identity1.getOptionList("profile", "nicknames"))
                .thenReturn(Arrays.asList(new String[]{"1nickname1", "1nickname2", "1nickname3", }));
        when(identity1.getOption("profile", "realname"))
                .thenReturn("1realname");
        when(identity1.getOption("profile", "ident"))
                .thenReturn("1ident");
        final Identity identity2 = mock(Identity.class);
        when(identity2.getName()).thenReturn("1identity");
        when(identity2.getOptionList("profile", "nicknames"))
                .thenReturn(Arrays.asList(new String[]{"2nickname1", "2nickname2", "2nickname3", }));
        when(identity2.getOption("profile", "realname"))
                .thenReturn("2realname");
        when(identity2.getOption("profile", "ident"))
                .thenReturn("2ident");
        final Identity identity3 = mock(Identity.class);
        when(identity3.getName()).thenReturn("1identity");
        when(identity3.getOptionList("profile", "nicknames"))
                .thenReturn(Arrays.asList(new String[]{"3nickname1", "3nickname2", "3nickname3", }));
        when(identity3.getOption("profile", "realname"))
                .thenReturn("3realname");
        when(identity3.getOption("profile", "ident"))
                .thenReturn("3ident");
        final List<Identity> localIdentities = new ArrayList<Identity>();
        localIdentities.add(identity1);
        localIdentities.add(identity2);
        localIdentities.add(identity3);
        identities = Collections.unmodifiableList(localIdentities);
        final List<Profile> localProfiles = new ArrayList<Profile>();
        for (Identity identity : identities) {
            localProfiles.add(new Profile(identity.getName(),
                    identity.getOptionList("profile", "nicknames"),
                    identity.getOption("profile", "realname"),
                    identity.getOption("profile", "ident"),
                    false));
        }
        profiles = Collections.unmodifiableList(localProfiles);
        manager = mock(IdentityManager.class);
        when(manager.getIdentitiesByType("profile")).thenReturn(identities);
    }

    /**
     * Test of setProfiles method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetProfiles() {
        final List<Profile> newProfiles = Arrays.asList(
                new Profile[]{
                    new Profile("test", Arrays.asList(new String[]{"test", }), "test", "test", false),
                    new Profile("test1", Arrays.asList(new String[]{"test1", }), "test1", "test1", false),
                });
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertEquals(profiles, instance.getProfiles());
        instance.setProfiles(newProfiles);
        assertEquals(newProfiles, instance.getProfiles());
    }

    /**
     * Test of addProfiles method, of class ProfileManagerModel.
     */
    @Test
    public void testAddProfiles() {
        Profile profile = new Profile("test", Arrays.asList(new String[]{"test", }), "test", "test", false);
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertEquals(profiles, instance.getProfiles());
        final List<Profile> newProfiles = new ArrayList<Profile>(profiles);
        newProfiles.add(profile);
        instance.addProfiles(profile);
        assertEquals(newProfiles, instance.getProfiles());
    }

    /**
     * Test of deleteProfile method, of class ProfileManagerModel.
     */
    @Test
    public void testDeleteProfile() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertEquals(profiles, instance.getProfiles());
        final List<Profile> newProfiles = new ArrayList<Profile>(profiles);
        newProfiles.remove(2);
        instance.deleteProfile(profiles.get(2));
        assertEquals(newProfiles, instance.getProfiles());
    }

    /**
     * Test of setSelectedProfile method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetSelectedProfile() {
        Object selectedProfile = profiles.get(2);
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertEquals(profiles.get(0), instance.getSelectedProfile());
        instance.setSelectedProfile(selectedProfile);
        assertEquals(selectedProfile, instance.getSelectedProfile());
    }

    /**
     * Test of getNicknames method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetNicknames() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        final List<String> expResult = profiles.get(0).getNicknames();
        final List<String> result = instance.getNicknames();
        assertEquals(expResult, result);
        final List<String> nicknames = Arrays.asList(new String[]{"foo", "bar", });
        instance.setNicknames(nicknames);
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of addNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testAddNickname() {
        String nickname = "foo";
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertEquals(profiles.get(0).getNicknames(), instance.getNicknames());
        instance.addNickname(nickname);
        final List<String> nicknames = new ArrayList<String>(profiles.get(0).getNicknames());
        nicknames.add(nickname);
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of deleteNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testDeleteNicknameObject() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        final List<String> nicknames = new ArrayList<String>(instance.getNicknames());
        final Object nickname = new Object();
        assertEquals(nicknames, instance.getNicknames());
        instance.deleteNickname(nickname);
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of deleteNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testDeleteNicknameString() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        final List<String> nicknames = new ArrayList<String>(instance.getNicknames());
        final String nickname = "1nickname1";
        assertEquals(nicknames, instance.getNicknames());
        instance.deleteNickname(nickname);
        nicknames.remove(nickname);
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of editNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testEditNickname() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        final List<String> nicknames = new ArrayList<String>(instance.getNicknames());
        final String nickname = "1nickname1";
        assertEquals(nicknames, instance.getNicknames());
        instance.editNickname(nickname, "foo");
        final int index = nicknames.indexOf(nickname);
        nicknames.remove(nickname);
        nicknames.add(index, "foo");
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of setSelectedNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetSelectedNickname() {
        final String selectedNickname = "1nickname1";
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertNull(instance.getSelectedNickname());
        instance.setSelectedNickname(selectedNickname);
        assertEquals(selectedNickname, instance.getSelectedNickname());
    }

    /**
     * Test of getName method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetName() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        final String name = "1identity";
        final String newName = "foo";
        assertEquals(name, instance.getName());
        instance.setName(newName);
        assertEquals(newName, ((Profile) instance.getSelectedProfile()).getName());
    }

    /**
     * Test of getRealname method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetRealname() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        final String name = "1realname";
        final String newName = "foo";
        assertEquals(name, instance.getRealname());
        instance.setRealname(newName);
        assertEquals(newName, ((Profile) instance.getSelectedProfile()).getRealname());
    }

    /**
     * Test of getIdent method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetIdent() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        final String name = "1ident";
        final String newName = "foo";
        assertEquals(name, instance.getIdent());
        instance.setIdent(newName);
        assertEquals(newName, ((Profile) instance.getSelectedProfile()).getIdent());
    }

    /**
     * Test of isManipulateProfileAllowed method, of class ProfileManagerModel.
     */
    @Test
    public void testIsManipulateProfileAllowed() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertTrue(instance.isManipulateProfileAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{}));
        assertFalse(instance.isManipulateProfileAllowed());
    }

    /**
     * Test of isManipulateNicknameAllowed method, of class ProfileManagerModel.
     */
    @Test
    public void testIsManipulateNicknameAllowed() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertFalse(instance.isManipulateNicknameAllowed());
        instance.setSelectedNickname("1nickname1");
        assertTrue(instance.isManipulateNicknameAllowed());
    }

    /**
     * Test of isOKAllowed method, of class ProfileManagerModel.
     */
    @Test
    public void testIsOKAllowed() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertTrue(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{}));
        assertFalse(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{
            new Profile("profile")
                , }));
        assertFalse(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{
            new Profile("profile", "nickname")
                , }));
        assertFalse(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{
            new Profile("profile", "nickname")
                , }));
        assertFalse(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{
            new Profile("profile", "nickname", "realname", ".")
                , }));
        assertFalse(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{
            new Profile("profile", "nickname", "realname", "ident")
                , }));
        assertTrue(instance.isOKAllowed());
    }

    /**
     * Test of isNameValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsNameValid() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        instance.setName(".");
        assertTrue(instance.isNameValid().isFailure());
        instance.setName("foo");
        assertFalse(instance.isNameValid().isFailure());
    }

    /**
     * Test of isNicknamesValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsNicknamesValid() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        instance.setNicknames(Arrays.asList(new String[]{}));
        assertTrue(instance.isNicknamesValid().isFailure());
        instance.setNicknames(Arrays.asList(new String[]{"foo"}));
        assertFalse(instance.isNicknamesValid().isFailure());
    }

    /**
     * Test of isRealnameValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsRealnameValid() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        instance.setRealname("");
        assertTrue(instance.isNicknamesValid().isFailure());
        instance.setRealname("foo");
        assertFalse(instance.isNicknamesValid().isFailure());
    }

    /**
     * Test of isIdentValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsIdentValid() {
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        instance.setRealname(".");
        assertTrue(instance.isNicknamesValid().isFailure());
        instance.setRealname("foo");
        assertFalse(instance.isNicknamesValid().isFailure());
    }
}

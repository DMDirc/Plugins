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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

/**
 * Test for ProfileManagerModel
 */
public class ProfileManagerModelTest {

    private static IdentityManager manager;
    private static Profile defaultProfile;

    private Profile createProfile(final String prefix) {
        final List<String> nicknames = new ArrayList<String>();
        nicknames.add(prefix + "nickname");

        final Identity identity = mock(Identity.class);
        when(identity.getName()).thenReturn(prefix + "profile");
        when(identity.getOption("identity", "name")).thenReturn(prefix + "profile");
        when(identity.getOptionList("profile", "nicknames")).thenReturn(nicknames);
        when(identity.getOption("profile", "realname")).thenReturn(prefix + "realname");
        when(identity.getOption("profile", "ident")).thenReturn(prefix + "ident");

        final Profile profile = new Profile(identity);
        return profile;
    }

    private ProfileManagerModel createModel() {
        defaultProfile = createProfile("1");
        final ProfileManagerModel model = new ProfileManagerModel(manager);
        model.addProfile(defaultProfile);
        return model;
    }

    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setUpClass() throws Exception {
        final List<Identity> identities = Collections.emptyList();
        manager = mock(IdentityManager.class);
        when(manager.getIdentitiesByType("profile")).thenReturn(identities);
    }

    /**
     * Test of setProfiles method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetProfiles() {
        final List<Profile> newProfiles = Arrays.asList(
                new Profile[]{createProfile("2"),});
        ProfileManagerModel instance = new ProfileManagerModel(manager);
        assertEquals(Collections.emptyList(), instance.getProfiles());
        instance.setProfiles(newProfiles);
        assertEquals(newProfiles, instance.getProfiles());
    }

    /**
     * Test of addProfiles method, of class ProfileManagerModel.
     */
    @Test
    public void testAddProfiles() {
        final Profile newProfile = createProfile("2");
        ProfileManagerModel instance = createModel();
        assertEquals(Arrays.asList(new Profile[]{defaultProfile,}), instance.getProfiles());
        final List<Profile> newProfiles = new ArrayList<Profile>();
        newProfiles.add(defaultProfile);
        newProfiles.add(newProfile);
        instance.addProfile(newProfile);
        assertEquals(newProfiles, instance.getProfiles());
    }

    /**
     * Test of deleteProfile method, of class ProfileManagerModel.
     */
    @Test
    public void testDeleteProfile() {
        ProfileManagerModel instance = createModel();
        final List<Profile> newProfiles = new ArrayList<Profile>(instance.getProfiles());
        newProfiles.remove(defaultProfile);
        instance.deleteProfile(defaultProfile);
        assertEquals(newProfiles, instance.getProfiles());
    }

    /**
     * Test of setSelectedProfile method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetSelectedProfile() {
        Profile profile2 = createProfile("2");
        Profile profile3 = createProfile("3");
        Profile profile4 = createProfile("4");
        ProfileManagerModel instance = createModel();
        assertEquals(defaultProfile, instance.getSelectedProfile());
        instance.addProfile(profile2);
        assertEquals(profile2, instance.getSelectedProfile());
        instance.addProfile(profile3);
        instance.setSelectedProfile(profile2);
        assertEquals(profile2, instance.getSelectedProfile());
        instance.setSelectedProfile(profile4);
        assertNotSame(profile4, instance.getSelectedProfile());
    }

    /**
     * Test of getNicknames method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetNicknames() {
        ProfileManagerModel instance = createModel();
        final List<String> expResult = defaultProfile.getNicknames();
        List<String> result = instance.getNicknames();
        assertEquals(expResult, result);
        result = Arrays.asList(new String[]{"foo", "bar",});
        instance.setNicknames(result);
        assertEquals(result, instance.getNicknames());
    }

    /**
     * Test of addNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testAddNickname() {
        String nickname = "foo";
        ProfileManagerModel instance = createModel();
        assertEquals(defaultProfile.getNicknames(), instance.getNicknames());
        final List<String> nicknames = new ArrayList<String>(defaultProfile.getNicknames());
        instance.addNickname(nickname);
        nicknames.add(nickname);
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of deleteNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testDeleteNicknameObject() {
        ProfileManagerModel instance = createModel();
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
        ProfileManagerModel instance = createModel();
        final List<String> nicknames = new ArrayList<String>(instance.getNicknames());
        final String nickname = "1nickname1";
        assertEquals(nicknames, instance.getNicknames());
        instance.deleteNickname(nickname);
        nicknames.remove(nickname);
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of setSelectedNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetSelectedNickname() {
        final String selectedNickname = "1nickname";
        ProfileManagerModel instance = createModel();
        assertNull(instance.getSelectedNickname());
        instance.setSelectedNickname(selectedNickname);
        assertEquals(selectedNickname, instance.getSelectedNickname());
        instance.setSelectedNickname("foo");
        assertNotSame("foo", instance.getSelectedNickname());
    }



    /**
     * Test of editNickname method, of class ProfileManagerModel.
     */
    @Test
    public void testEditNickname() {
        ProfileManagerModel instance = createModel();
        final List<String> nicknames = new ArrayList<String>(instance.getNicknames());
        final String nickname = nicknames.get(0);
        assertEquals(nicknames, instance.getNicknames());
        instance.editNickname(nickname, "foo");
        final int index = nicknames.indexOf(nickname);
        nicknames.remove(nickname);
        nicknames.add(index, "foo");
        assertEquals(nicknames, instance.getNicknames());
    }

    /**
     * Test of getName method, of class ProfileManagerModel.
     */
    @Test
    public void testGetAndSetName() {
        ProfileManagerModel instance = createModel();
        final String name = defaultProfile.getName();
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
        ProfileManagerModel instance = createModel();
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
        ProfileManagerModel instance = createModel();
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
        ProfileManagerModel instance = createModel();
        assertTrue(instance.isManipulateProfileAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{}));
        assertFalse(instance.isManipulateProfileAllowed());
    }

    /**
     * Test of isManipulateNicknameAllowed method, of class ProfileManagerModel.
     */
    @Test
    public void testIsManipulateNicknameAllowed() {
        ProfileManagerModel instance = createModel();
        assertFalse(instance.isManipulateNicknameAllowed());
        instance.setSelectedNickname("1nickname");
        assertTrue(instance.isManipulateNicknameAllowed());
    }

    /**
     * Test of isOKAllowed method, of class ProfileManagerModel.
     */
    @Test
    public void testIsOKAllowed() {
        final Profile profile = new Profile(null);
        profile.setName("*");
        profile.setRealname("");
        profile.setIdent("*");
        ProfileManagerModel instance = createModel();
        assertTrue(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{}));
        assertFalse(instance.isOKAllowed());
        instance.setProfiles(Arrays.asList(new Profile[]{profile,}));
        assertFalse(instance.isOKAllowed());
        profile.setName("profile");
        assertFalse(instance.isOKAllowed());
        profile.addNickname("nickname");
        assertFalse(instance.isOKAllowed());
        profile.setRealname("realname");
        assertFalse(instance.isOKAllowed());
        profile.setIdent("ident");
        assertTrue(instance.isOKAllowed());
    }

    /**
     * Test of isNameValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsNameValid() {
        ProfileManagerModel instance = createModel();
        instance.setProfiles(Arrays.asList(defaultProfile));
        instance.setName("\\");
        assertTrue(instance.isNameValid().isFailure());
        instance.setName("profile");
        assertFalse(instance.isNameValid().isFailure());
    }

    /**
     * Test of isNicknamesValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsNicknamesValid() {
        ProfileManagerModel instance = createModel();
        instance.setProfiles(Arrays.asList(defaultProfile));
        instance.setNicknames(Arrays.asList(new String[]{}));
        assertTrue(instance.isNicknamesValid().isFailure());
        instance.setNicknames(Arrays.asList(new String[]{"nickname"}));
        assertFalse(instance.isNicknamesValid().isFailure());
    }

    /**
     * Test of isRealnameValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsRealnameValid() {
        ProfileManagerModel instance = createModel();
        instance.setProfiles(Arrays.asList(defaultProfile));
        instance.setRealname("");
        assertTrue(instance.isRealnameValid().isFailure());
        instance.setRealname("realname");
        assertFalse(instance.isRealnameValid().isFailure());
    }

    /**
     * Test of isIdentValid method, of class ProfileManagerModel.
     */
    @Test
    public void testIsIdentValid() {
        ProfileManagerModel instance = createModel();
        instance.setProfiles(Arrays.asList(defaultProfile));
        instance.setIdent("*");
        assertTrue(instance.isIdentValid().isFailure());
        instance.setIdent("ident");
        assertFalse(instance.isIdentValid().isFailure());
    }
}

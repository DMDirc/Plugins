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

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ProfileManagerController.
 */
public class ProfileManagerControllerTest {

    /**
     * Test of addProfile method, of class ProfileManagerController.
     */
    @Test
    public void testAddProfile() {
        final ProfileManagerDialog dialog = mock(ProfileManagerDialog.class);
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        ProfileManagerController instance = new ProfileManagerController(dialog, model);
        instance.addProfile();
        verify(model).addProfile(new Profile());
    }

    /**
     * Test of deleteProfile method, of class ProfileManagerController.
     */
    @Test
    public void testDeleteProfile() {
        final ProfileManagerDialog dialog = mock(ProfileManagerDialog.class);
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        final Profile selectedProfile = mock(Profile.class);
        when(model.getSelectedProfile()).thenReturn(selectedProfile);
        ProfileManagerController instance = new ProfileManagerController(dialog, model);
        instance.deleteProfile();
        verify(model).deleteProfile(selectedProfile);
    }

    /**
     * Test of addNickname method, of class ProfileManagerController.
     */
    @Test
    public void testAddNickname() {
        final ProfileManagerDialog dialog = mock(ProfileManagerDialog.class);
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        ProfileManagerController instance = new ProfileManagerController(dialog, model);
        instance.addNickname("test");
        verify(model).addNickname("test");
    }

    /**
     * Test of editNickname method, of class ProfileManagerController.
     */
    @Test
    public void testEditNickname() {
        final ProfileManagerDialog dialog = mock(ProfileManagerDialog.class);
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        when(model.getSelectedNickname()).thenReturn("test");
        ProfileManagerController instance = new ProfileManagerController(dialog, model);
        instance.editNickname("test2");
        verify(model).editNickname("test", "test2");
    }

    /**
     * Test of deleteNickname method, of class ProfileManagerController.
     */
    @Test
    public void testDeleteNickname() {
        final ProfileManagerDialog dialog = mock(ProfileManagerDialog.class);
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        when(model.getSelectedNickname()).thenReturn("test");
        ProfileManagerController instance = new ProfileManagerController(dialog, model);
        instance.deleteNickname();
        verify(model).deleteNickname((Object) "test");
    }

    /**
     * Test of closeDialog method, of class ProfileManagerController.
     */
    @Test
    public void testCloseDialog() {
        final ProfileManagerDialog dialog = mock(ProfileManagerDialog.class);
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        ProfileManagerController instance = new ProfileManagerController(dialog, model);
        instance.closeDialog();
        verify(dialog).dispose();
    }

    /**
     * Test of saveAndCloseDialog method, of class ProfileManagerController.
     */
    @Test
    public void testSaveAndCloseDialog() {
        final ProfileManagerDialog dialog = mock(ProfileManagerDialog.class);
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        ProfileManagerController instance = new ProfileManagerController(dialog, model);
        instance.saveAndCloseDialog();
        verify(model).save();
        verify(dialog).dispose();
    }
}

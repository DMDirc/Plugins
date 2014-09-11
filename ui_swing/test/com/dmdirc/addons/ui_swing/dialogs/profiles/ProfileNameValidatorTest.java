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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dmdirc.addons.ui_swing.dialogs.profiles;

import com.dmdirc.actions.wrappers.Profile;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * ProfileNameValidator tests.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileNameValidatorTest {

    @Mock private Profile other;
    @Mock private Profile selected;
    @Mock private ProfileManagerModel model;
    private List<Profile> profiles;

    @Before
    public void setup() {
        when(other.getName()).thenReturn("other");
        when(selected.getName()).thenReturn("selected");
        profiles = new ArrayList<>(2);
        profiles.add(selected);
        profiles.add(other);
        when(model.getProfiles()).thenReturn(profiles);
        when(model.getSelectedProfile()).thenReturn(selected);
    }

    /**
     * Test of validate method, of class ProfileNameValidator.
     */
    @Test
    public void testValidateNoDupes() {
        ProfileRenameValidator instance = new ProfileRenameValidator(model);
        assertFalse(instance.validate("Random").isFailure());
    }

    /**
     * Test of validate method, of class ProfileNameValidator.
     */
    @Test
    public void testValidateNonSelectedDupe() {
        ProfileRenameValidator instance = new ProfileRenameValidator(model);
        assertTrue(instance.validate("other").isFailure());
    }

    /**
     * Test of validate method, of class ProfileNameValidator.
     */
    @Test
    public void testValidateSelectedDupe() {
        ProfileRenameValidator instance = new ProfileRenameValidator(model);
        assertFalse(instance.validate("selected").isFailure());
    }
}

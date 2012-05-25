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

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests the nickname validator.
 */
public class AddNicknameValidatorTest {

    /**
     * Test of validate method, of class AddNicknameValidator.
     */
    @Test
    public void testValidateValid() {
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        when(model.getNicknames()).thenReturn(Arrays.asList(
                new String[]{"nickname1", "nickname2",}));
        AddNicknameValidator instance = new AddNicknameValidator(model);
        assertTrue(instance.validate("nickname1").isFailure());
    }

    /**
     * Test of validate method, of class AddNicknameValidator.
     */
    @Test
    public void testValidateInvalid() {
        final ProfileManagerModel model = mock(ProfileManagerModel.class);
        when(model.getNicknames()).thenReturn(Arrays.asList(
                new String[]{"nickname1", "nickname2",}));
        AddNicknameValidator instance = new AddNicknameValidator(model);
        assertFalse(instance.validate("nickname").isFailure());
    }
}

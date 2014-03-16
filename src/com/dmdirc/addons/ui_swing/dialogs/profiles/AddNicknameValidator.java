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

import com.dmdirc.util.validators.NicknameValidator;
import com.dmdirc.util.validators.ValidationResponse;

/**
 * Validates new nicknames being added to a profile.
 */
public class AddNicknameValidator extends NicknameValidator {

    /** Model used to query for validation. */
    private final ProfileManagerModel model;

    /**
     * Creates a new add nickname validator.
     *
     * @param model Model used to query for information
     */
    public AddNicknameValidator(final ProfileManagerModel model) {
        this.model = model;
    }

    @Override
    public ValidationResponse validate(final String object) {
        if (model.getNicknames().contains(object)) {
            return new ValidationResponse("Duplicate nickname exists");
        }
        return super.validate(object);
    }

}